package com.intellibranch.neurogate;

import com.intellibranch.core.*;
import com.intellibranch.routing.*;
import com.intellibranch.training.DataSample;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NeuroGate coordinates a single shared neural backbone with a geometric 3-head zero-allocation gate.
 * Head 1: Geometric L2 Cosine Out-of-Domain (OOD) Guard.
 * Head 2: Bitwise Token Anchor Soft-Bias.
 * Head 3: Softmax, Margin, LogSumExp, Free Energy, and Shannon Entropy Gating.
 */
public class NeuroGate {
    public static final int MAX_GATE_CLASSES = 16;
    public static final int MAX_GATE_EMB_DIM = 64;

    private final AtomicReference<InferenceModel> modelRef = new AtomicReference<>();

    private final String[] labels = new String[MAX_GATE_CLASSES];
    private final RouteAction[] routes = new RouteAction[MAX_GATE_CLASSES];
    private int classCount = 0;
    private final Map<String, Integer> labelToIndex = new ConcurrentHashMap<>();

    private final Map<String, Long> anchorDict = new ConcurrentHashMap<>();
    private final Map<Integer, Long> anchorTokenMap = new ConcurrentHashMap<>();
    private final List<AnchorRule> anchorRules = new ArrayList<>();

    private final float[] domainCentroid = new float[MAX_GATE_EMB_DIM];
    private volatile boolean hasCentroid = false;
    private volatile float minCosineSim = 0.25f;

    private volatile DispatchPolicy policy = DispatchPolicy.defaultPolicy();
    private final Map<String, PipelineAction> pipelines = new ConcurrentHashMap<>();
    private volatile AmbiguousAction ambiguous;
    private volatile RouteAction fallback = (ctx, payload) -> {};

    // Zero-allocation thread-local scratch buffers for hot-path evaluation
    private static class GateScratch {
        final float[] pooled = new float[MAX_GATE_EMB_DIM];
        final float[] rawLogits = new float[MAX_GATE_CLASSES];
        final float[] normPooled = new float[MAX_GATE_EMB_DIM];
        final float[] adjustedLogits = new float[MAX_GATE_CLASSES];
        final float[] probs = new float[MAX_GATE_CLASSES];
    }

    private final ThreadLocal<GateScratch> scratchHolder = ThreadLocal.withInitial(GateScratch::new);

    public NeuroGate(InferenceModel model) {
        this.modelRef.set(model);
        if (model != null) {
            for (int i = 0; i < model.getLabels().size(); i++) {
                if (i >= MAX_GATE_CLASSES) {
                    break;
                }
                String lbl = model.getLabels().get(i);
                labels[i] = lbl;
                labelToIndex.put(lbl, i);
                classCount++;
            }
            computeBaselineCentroid(model);
        }
    }

    public static NeuroGate load(Path modelPath) throws IOException {
        InferenceModel model = BinaryModel.load(modelPath);
        return new NeuroGate(model);
    }

    private void computeBaselineCentroid(InferenceModel model) {
        int embDim = model.getHeader().embeddingDim();
        if (embDim > MAX_GATE_EMB_DIM || embDim == 0 || model.getWeights().getEmbedding() == null) {
            return;
        }

        double[] sum = new double[MAX_GATE_EMB_DIM];
        int validTokens = 0;
        List<String> vocab = model.getVocab();
        float[] embTable = model.getWeights().getEmbedding();

        for (int tokID = 0; tokID < vocab.size(); tokID++) {
            String word = vocab.get(tokID);
            if (BPETokenizer.PAD_TOKEN.equals(word) || BPETokenizer.UNK_TOKEN.equals(word)) {
                continue;
            }
            int offset = tokID * embDim;
            if (offset + embDim <= embTable.length) {
                for (int d = 0; d < embDim; d++) {
                    sum[d] += (double) embTable[offset + d];
                }
                validTokens++;
            }
        }

        if (validTokens > 0) {
            double inv = 1.0 / (double) validTokens;
            float[] raw = new float[MAX_GATE_EMB_DIM];
            for (int d = 0; d < embDim; d++) {
                raw[d] = (float) (sum[d] * inv);
            }
            Ops.l2Normalize(raw, embDim, domainCentroid);
            hasCentroid = true;
        }
    }

    /**
     * Calculates the true manifold center from sample dataset sentences.
     */
    public synchronized NeuroGate calibrateDomainCentroid(List<DataSample> samples) {
        InferenceModel model = modelRef.get();
        if (model == null || samples == null || samples.isEmpty()) {
            return this;
        }

        int embDim = Math.min(model.getHeader().embeddingDim(), MAX_GATE_EMB_DIM);
        double[] sum = new double[MAX_GATE_EMB_DIM];
        int validCount = 0;
        float[] pooled = new float[MAX_GATE_EMB_DIM];
        float[] dummyLogits = new float[MAX_GATE_CLASSES];

        for (DataSample s : samples) {
            int[] tokens = model.getTokenizer().encode(s.text());
            if (tokens == null || tokens.length == 0) {
                continue;
            }
            model.predictFeatures(tokens, pooled, dummyLogits);
            for (int d = 0; d < embDim; d++) {
                sum[d] += (double) pooled[d];
            }
            validCount++;
        }

        if (validCount > 0) {
            double inv = 1.0 / (double) validCount;
            float[] raw = new float[MAX_GATE_EMB_DIM];
            for (int d = 0; d < embDim; d++) {
                raw[d] = (float) (sum[d] * inv);
            }
            Ops.l2Normalize(raw, embDim, domainCentroid);
            hasCentroid = true;
        }
        return this;
    }

    public synchronized NeuroGate setDomainBoundary(float[] centroid, float minCosine) {
        int dim = Math.min(centroid.length, MAX_GATE_EMB_DIM);
        System.arraycopy(centroid, 0, domainCentroid, 0, dim);
        Ops.l2Normalize(domainCentroid, dim, domainCentroid);
        this.hasCentroid = true;
        this.minCosineSim = minCosine;
        return this;
    }

    public NeuroGate setMinCosineSim(float threshold) {
        this.minCosineSim = threshold;
        return this;
    }

    public NeuroGate setPolicy(DispatchPolicy policy) {
        this.policy = policy;
        return this;
    }

    public DispatchPolicy getPolicy() {
        return policy;
    }

    public synchronized GateRouteBuilder bind(String label, RouteAction handler) {
        Integer idx = labelToIndex.get(label);
        if (idx == null) {
            if (classCount >= MAX_GATE_CLASSES) {
                throw new IllegalStateException("NeuroGate: registered classes exceed MAX_GATE_CLASSES (" + MAX_GATE_CLASSES + ")");
            }
            idx = classCount;
            labels[idx] = label;
            labelToIndex.put(label, idx);
            classCount++;
        }
        routes[idx] = handler;
        return new GateRouteBuilder(this, idx, label);
    }

    synchronized void registerAnchor(int classIndex, float weight, String... keywords) {
        long mask = 0L;
        List<String> cleanKeywords = new ArrayList<>();
        for (String kw : keywords) {
            if (kw == null) continue;
            kw = kw.trim().toLowerCase(Locale.ROOT);
            if (kw.isEmpty()) continue;
            cleanKeywords.add(kw);

            Long m = anchorDict.get(kw);
            if (m == null) {
                if (anchorDict.size() < 64) {
                    m = 1L << anchorDict.size();
                    anchorDict.put(kw, m);
                } else {
                    m = 0L;
                }
            }
            mask |= m;
        }

        InferenceModel model = modelRef.get();
        if (model != null && model.getTokenizer() != null) {
            for (String kw : cleanKeywords) {
                int[] toks = model.getTokenizer().encode(kw);
                if (toks != null) {
                    for (int tid : toks) {
                        anchorTokenMap.merge(tid, mask, (a, b) -> a | b);
                    }
                }
            }
        }

        anchorRules.add(new AnchorRule(classIndex, mask, weight, cleanKeywords));
    }

    public NeuroGate bindPipeline(String primary, String secondary, PipelineAction handler) {
        pipelines.put(pipelineKey(primary, secondary), handler);
        return this;
    }

    public NeuroGate ambiguous(AmbiguousAction handler) {
        this.ambiguous = handler;
        return this;
    }

    public NeuroGate fallback(RouteAction handler) {
        this.fallback = handler;
        return this;
    }

    public void swapModel(InferenceModel newModel) {
        this.modelRef.set(newModel);
    }

    public InferenceModel getModel() {
        return modelRef.get();
    }

    private static String pipelineKey(String primary, String secondary) {
        return primary + "->" + secondary;
    }

    private record GateEvaluation(
            int primaryIdx,
            int secondaryIdx,
            boolean isFallback,
            boolean isPipeline,
            boolean isAmbiguous
    ) {
        static final GateEvaluation FALLBACK = new GateEvaluation(0, 1, true, false, false);
    }

    private GateEvaluation evaluateFast(String text) {
        InferenceModel model = modelRef.get();
        if (model == null) {
            return GateEvaluation.FALLBACK;
        }

        String safeText = InferenceModel.truncateToUtf8Boundary(text, InferenceModel.MAX_INPUT_BYTES);
        int[] tokens = model.getTokenizer().encode(safeText);
        if (tokens == null || tokens.length == 0) {
            return GateEvaluation.FALLBACK;
        }
        int len = Math.min(tokens.length, InferenceModel.MAX_SEQUENCE_TOKENS);

        int unkCount = 0;
        Integer unkID = model.getTokenizer().getVocabMap().get(BPETokenizer.UNK_TOKEN);
        for (int i = 0; i < len; i++) {
            if (unkID != null && tokens[i] == unkID) {
                unkCount++;
            }
        }
        double unkRatio = (len > 0) ? (double) unkCount / (double) len : 0.0;

        int embDim = Math.min(model.getHeader().embeddingDim(), MAX_GATE_EMB_DIM);
        int nClasses = classCount;

        GateScratch scratch = scratchHolder.get();
        model.predictFeatures(tokens, scratch.pooled, scratch.rawLogits);

        // [Head 1]: L2 Cosine OOD Guard
        Ops.l2Normalize(scratch.pooled, embDim, scratch.normPooled);
        if (hasCentroid) {
            float cosineSim = Ops.dotProduct(scratch.normPooled, domainCentroid, embDim);
            if (cosineSim < minCosineSim) {
                return GateEvaluation.FALLBACK;
            }
        }

        // [Head 2]: Anchor Bitmask & Symbolic Bias
        long textBitmask = 0L;
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Long> entry : anchorDict.entrySet()) {
            if (lowerText.contains(entry.getKey())) {
                textBitmask |= entry.getValue();
            }
        }

        System.arraycopy(scratch.rawLogits, 0, scratch.adjustedLogits, 0, nClasses);
        for (AnchorRule rule : anchorRules) {
            long matchedBits = textBitmask & rule.mask();
            if (matchedBits != 0L) {
                float count = (float) Long.bitCount(matchedBits);
                scratch.adjustedLogits[rule.classIndex()] += rule.weight() * count;
            }
        }

        // [Head 3]: Softmax, Margin, Entropy, and Energy
        Ops.softmax(scratch.adjustedLogits, nClasses, model.getTemperature(), scratch.probs);

        int bestIdx = 0, secondIdx = 1;
        float bestScore = -1.0f, secondScore = -1.0f;
        for (int i = 0; i < nClasses; i++) {
            float p = scratch.probs[i];
            if (p > bestScore) {
                secondScore = bestScore;
                secondIdx = bestIdx;
                bestScore = p;
                bestIdx = i;
            } else if (p > secondScore) {
                secondScore = p;
                secondIdx = i;
            }
        }

        double calibratedConfidence = (double) bestScore * (1.0 - unkRatio);
        double calibratedSecond = (double) secondScore * (1.0 - unkRatio);
        double margin = calibratedConfidence - calibratedSecond;
        double entropy = (double) Ops.computeEntropy(scratch.probs, nClasses);

        // LogSumExp evaluation
        float maxLogit = scratch.adjustedLogits[0];
        for (int i = 1; i < nClasses; i++) {
            if (scratch.adjustedLogits[i] > maxLogit) {
                maxLogit = scratch.adjustedLogits[i];
            }
        }
        double sumExp = 0.0;
        for (int i = 0; i < nClasses; i++) {
            sumExp += Math.exp((double) (scratch.adjustedLogits[i] - maxLogit));
        }
        double logSumExp = (double) maxLogit + Math.log(sumExp);

        if ((policy.minLogSumExp() > 0.0 && logSumExp < policy.minLogSumExp()) ||
                unkRatio >= 0.5 ||
                calibratedConfidence < policy.lowThreshold() ||
                entropy > policy.maxEntropy()) {
            return new GateEvaluation(bestIdx, secondIdx, true, false, false);
        }

        boolean isPipeline = false;
        boolean isAmbiguous = false;
        boolean isFallback = false;

        if (secondIdx >= 0 && secondIdx < nClasses) {
            String pipeKey = pipelineKey(labels[bestIdx], labels[secondIdx]);
            boolean hasPipe = pipelines.containsKey(pipeKey);
            boolean primaryAnchors = false, secondaryAnchors = false;
            for (AnchorRule rule : anchorRules) {
                if ((textBitmask & rule.mask()) != 0L) {
                    if (rule.classIndex() == bestIdx) primaryAnchors = true;
                    if (rule.classIndex() == secondIdx) secondaryAnchors = true;
                }
            }
            boolean hasBothAnchors = primaryAnchors && secondaryAnchors;
            if (calibratedSecond >= policy.pipelineThreshold() || (hasBothAnchors && hasPipe)) {
                isPipeline = true;
            }
        }

        if (calibratedConfidence < policy.highThreshold() || margin < policy.marginCutoff()) {
            isAmbiguous = true;
        }

        if (routes[bestIdx] == null) {
            isFallback = true;
        }

        return new GateEvaluation(bestIdx, secondIdx, isFallback, isPipeline, isAmbiguous);
    }

    private GateEvaluation evaluateFastTokens(int[] tokens) {
        InferenceModel model = modelRef.get();
        if (model == null || tokens == null || tokens.length == 0) {
            return GateEvaluation.FALLBACK;
        }
        int len = Math.min(tokens.length, InferenceModel.MAX_SEQUENCE_TOKENS);

        int unkCount = 0;
        Integer unkID = model.getTokenizer().getVocabMap().get(BPETokenizer.UNK_TOKEN);
        for (int i = 0; i < len; i++) {
            if (unkID != null && tokens[i] == unkID) {
                unkCount++;
            }
        }
        double unkRatio = (len > 0) ? (double) unkCount / (double) len : 0.0;

        int embDim = Math.min(model.getHeader().embeddingDim(), MAX_GATE_EMB_DIM);
        int nClasses = classCount;

        GateScratch scratch = scratchHolder.get();
        model.predictFeatures(tokens, scratch.pooled, scratch.rawLogits);

        // [Head 1]: L2 Cosine OOD Guard
        Ops.l2Normalize(scratch.pooled, embDim, scratch.normPooled);
        if (hasCentroid) {
            float cosineSim = Ops.dotProduct(scratch.normPooled, domainCentroid, embDim);
            if (cosineSim < minCosineSim) {
                return GateEvaluation.FALLBACK;
            }
        }

        // [Head 2]: 1-Cycle Bitwise Token Anchor Soft-Bias
        long textBitmask = 0L;
        for (int i = 0; i < len; i++) {
            Long m = anchorTokenMap.get(tokens[i]);
            if (m != null) {
                textBitmask |= m;
            }
        }

        System.arraycopy(scratch.rawLogits, 0, scratch.adjustedLogits, 0, nClasses);
        for (AnchorRule rule : anchorRules) {
            long matchedBits = textBitmask & rule.mask();
            if (matchedBits != 0L) {
                float count = (float) Long.bitCount(matchedBits);
                scratch.adjustedLogits[rule.classIndex()] += rule.weight() * count;
            }
        }

        // [Head 3]: Softmax, Margin, Entropy, Energy
        Ops.softmax(scratch.adjustedLogits, nClasses, model.getTemperature(), scratch.probs);

        int bestIdx = 0, secondIdx = 1;
        float bestScore = -1.0f, secondScore = -1.0f;
        for (int i = 0; i < nClasses; i++) {
            float p = scratch.probs[i];
            if (p > bestScore) {
                secondScore = bestScore;
                secondIdx = bestIdx;
                bestScore = p;
                bestIdx = i;
            } else if (p > secondScore) {
                secondScore = p;
                secondIdx = i;
            }
        }

        double calibratedConfidence = (double) bestScore * (1.0 - unkRatio);
        double calibratedSecond = (double) secondScore * (1.0 - unkRatio);
        double margin = calibratedConfidence - calibratedSecond;
        double entropy = (double) Ops.computeEntropy(scratch.probs, nClasses);

        float maxLogit = scratch.adjustedLogits[0];
        for (int i = 1; i < nClasses; i++) {
            if (scratch.adjustedLogits[i] > maxLogit) {
                maxLogit = scratch.adjustedLogits[i];
            }
        }
        double sumExp = 0.0;
        for (int i = 0; i < nClasses; i++) {
            sumExp += Math.exp((double) (scratch.adjustedLogits[i] - maxLogit));
        }
        double logSumExp = (double) maxLogit + Math.log(sumExp);

        if ((policy.minLogSumExp() > 0.0 && logSumExp < policy.minLogSumExp()) ||
                unkRatio >= 0.5 ||
                calibratedConfidence < policy.lowThreshold() ||
                entropy > policy.maxEntropy()) {
            return new GateEvaluation(bestIdx, secondIdx, true, false, false);
        }

        boolean isPipeline = false;
        boolean isAmbiguous = false;
        boolean isFallback = false;

        if (secondIdx >= 0 && secondIdx < nClasses) {
            String pipeKey = pipelineKey(labels[bestIdx], labels[secondIdx]);
            boolean hasPipe = pipelines.containsKey(pipeKey);
            boolean primaryAnchors = false, secondaryAnchors = false;
            for (AnchorRule rule : anchorRules) {
                if ((textBitmask & rule.mask()) != 0L) {
                    if (rule.classIndex() == bestIdx) primaryAnchors = true;
                    if (rule.classIndex() == secondIdx) secondaryAnchors = true;
                }
            }
            boolean hasBothAnchors = primaryAnchors && secondaryAnchors;
            if (calibratedSecond >= policy.pipelineThreshold() || (hasBothAnchors && hasPipe)) {
                isPipeline = true;
            }
        }

        if (calibratedConfidence < policy.highThreshold() || margin < policy.marginCutoff()) {
            isAmbiguous = true;
        }

        if (routes[bestIdx] == null) {
            isFallback = true;
        }

        return new GateEvaluation(bestIdx, secondIdx, isFallback, isPipeline, isAmbiguous);
    }

    /**
     * Evaluates input text across all 3 heads and returns detailed diagnostics without mutations.
     */
    public GateTrace inspect(String text) {
        long start = System.nanoTime();
        InferenceModel model = modelRef.get();
        if (model == null) {
            long lat = (System.nanoTime() - start) / 1000;
            return GateTrace.fallback(text, "model not loaded", lat);
        }

        String safeText = InferenceModel.truncateToUtf8Boundary(text, InferenceModel.MAX_INPUT_BYTES);
        int[] tokens = model.getTokenizer().encode(safeText);
        if (tokens == null || tokens.length == 0) {
            long lat = (System.nanoTime() - start) / 1000;
            return GateTrace.fallback(text, "empty input tokens", lat);
        }
        int len = Math.min(tokens.length, InferenceModel.MAX_SEQUENCE_TOKENS);
        tokens = Arrays.copyOf(tokens, len);

        String[] subwords = new String[len];
        int unkCount = 0;
        Integer unkID = model.getTokenizer().getVocabMap().get(BPETokenizer.UNK_TOKEN);
        for (int i = 0; i < len; i++) {
            int id = tokens[i];
            if (unkID != null && id == unkID) {
                unkCount++;
            }
            if (id < model.getVocab().size()) {
                subwords[i] = model.getVocab().get(id);
            }
        }
        double unkRatio = (len > 0) ? (double) unkCount / (double) len : 0.0;

        int embDim = Math.min(model.getHeader().embeddingDim(), MAX_GATE_EMB_DIM);
        int nClasses = classCount;

        GateScratch scratch = scratchHolder.get();
        model.predictFeatures(tokens, scratch.pooled, scratch.rawLogits);

        // [Head 1]: L2 Cosine OOD Guard
        Ops.l2Normalize(scratch.pooled, embDim, scratch.normPooled);
        float cosineSim = 1.0f;
        boolean isOOD = false;
        if (hasCentroid) {
            cosineSim = Ops.dotProduct(scratch.normPooled, domainCentroid, embDim);
            if (cosineSim < minCosineSim) {
                isOOD = true;
            }
        }

        // [Head 2]: Tokenizer Anchor Bitmask & Symbolic Bias
        long textBitmask = 0L;
        List<String> triggeredAnchors = new ArrayList<>();
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Long> entry : anchorDict.entrySet()) {
            if (lowerText.contains(entry.getKey())) {
                textBitmask |= entry.getValue();
                triggeredAnchors.add(entry.getKey());
            }
        }

        System.arraycopy(scratch.rawLogits, 0, scratch.adjustedLogits, 0, nClasses);
        for (AnchorRule rule : anchorRules) {
            long matchedBits = textBitmask & rule.mask();
            if (matchedBits != 0L) {
                float count = (float) Long.bitCount(matchedBits);
                scratch.adjustedLogits[rule.classIndex()] += rule.weight() * count;
            }
        }

        // [Head 3]: Softmax, Margin, Entropy, Energy
        Ops.softmax(scratch.adjustedLogits, nClasses, model.getTemperature(), scratch.probs);

        int bestIdx = 0, secondIdx = 1;
        float bestScore = -1.0f, secondScore = -1.0f;
        Map<String, Float> probMap = new LinkedHashMap<>();

        for (int i = 0; i < nClasses; i++) {
            String lbl = labels[i];
            float p = scratch.probs[i];
            probMap.put(lbl, p);
            if (p > bestScore) {
                secondScore = bestScore;
                secondIdx = bestIdx;
                bestScore = p;
                bestIdx = i;
            } else if (p > secondScore) {
                secondScore = p;
                secondIdx = i;
            }
        }

        double calibratedConfidence = (double) bestScore * (1.0 - unkRatio);
        double calibratedSecond = (double) secondScore * (1.0 - unkRatio);
        double margin = calibratedConfidence - calibratedSecond;
        double entropy = (double) Ops.computeEntropy(scratch.probs, nClasses);

        String bestLabel = labels[bestIdx];
        String secondLabel = (nClasses >= 2) ? labels[secondIdx] : "";

        float maxLogit = scratch.adjustedLogits[0];
        for (int i = 1; i < nClasses; i++) {
            if (scratch.adjustedLogits[i] > maxLogit) {
                maxLogit = scratch.adjustedLogits[i];
            }
        }
        double sumExp = 0.0;
        for (int i = 0; i < nClasses; i++) {
            sumExp += Math.exp((double) (scratch.adjustedLogits[i] - maxLogit));
        }
        double logSumExp = (double) maxLogit + Math.log(sumExp);
        double freeEnergy = -logSumExp;

        String oodReason = "";
        if (hasCentroid && cosineSim < minCosineSim) {
            isOOD = true;
            oodReason = String.format(Locale.US, "cosine similarity %.4f below domain threshold %.4f (OOD)", cosineSim, minCosineSim);
        } else if (policy.minLogSumExp() > 0.0 && logSumExp < policy.minLogSumExp()) {
            isOOD = true;
            oodReason = String.format(Locale.US, "free energy %.4f (logSumExp %.4f) below in-distribution threshold %.4f (OOD)", freeEnergy, logSumExp, policy.minLogSumExp());
        } else if (entropy > policy.maxEntropy()) {
            isOOD = true;
            oodReason = String.format(Locale.US, "prediction entropy %.4f exceeds limit %.4f (OOD)", entropy, policy.maxEntropy());
        } else if (unkRatio >= 0.5) {
            isOOD = true;
            oodReason = String.format(Locale.US, "excessive unknown tokens (%.2f >= 0.50)", unkRatio);
        }

        long latencyMicros = (System.nanoTime() - start) / 1000;

        boolean isFallback = false;
        String fallbackReason = "";
        boolean isPipeline = false;
        boolean isAmbiguous = false;

        if (isOOD) {
            isFallback = true;
            fallbackReason = oodReason;
        } else if (calibratedConfidence < policy.lowThreshold()) {
            isFallback = true;
            fallbackReason = String.format(Locale.US, "confidence %.4f below low threshold %.4f", calibratedConfidence, policy.lowThreshold());
        } else {
            if (!secondLabel.isEmpty()) {
                String pipeKey = pipelineKey(bestLabel, secondLabel);
                boolean hasPipeline = pipelines.containsKey(pipeKey);
                boolean primaryAnchors = false, secondaryAnchors = false;
                for (AnchorRule rule : anchorRules) {
                    if ((textBitmask & rule.mask()) != 0L) {
                        if (rule.classIndex() == bestIdx) primaryAnchors = true;
                        if (rule.classIndex() == secondIdx) secondaryAnchors = true;
                    }
                }
                boolean hasBothAnchors = primaryAnchors && secondaryAnchors;
                if (calibratedSecond >= policy.pipelineThreshold() || (hasBothAnchors && hasPipeline)) {
                    isPipeline = true;
                }
            }
            if (calibratedConfidence < policy.highThreshold() || margin < policy.marginCutoff()) {
                isAmbiguous = true;
            }
            if (routes[bestIdx] == null) {
                isFallback = true;
                fallbackReason = String.format("label '%s' has no bound route handler", bestLabel);
            }
        }

        return new GateTrace(
                text,
                tokens,
                subwords,
                unkRatio,
                cosineSim,
                isOOD,
                textBitmask,
                triggeredAnchors,
                probMap,
                bestLabel,
                secondLabel,
                calibratedConfidence,
                margin,
                entropy,
                logSumExp,
                freeEnergy,
                policy.highThreshold(),
                isAmbiguous,
                isPipeline,
                isFallback,
                fallbackReason,
                latencyMicros
        );
    }

    /**
     * Evaluates query and executes the appropriate handler with zero heap allocations on hot path.
     */
    public void filter(Object context, String text, Object payload) throws Exception {
        GateEvaluation eval = evaluateFast(text);

        // 1. Fallback Tier
        if (eval.isFallback()) {
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        // 2. Ambiguous Tier
        if (eval.isAmbiguous()) {
            if (ambiguous != null) {
                String pLabel = labels[eval.primaryIdx()];
                String sLabel = (eval.secondaryIdx() >= 0 && eval.secondaryIdx() < classCount) ? labels[eval.secondaryIdx()] : "";
                ambiguous.execute(context, pLabel, sLabel, payload);
                return;
            }
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        // 3. Definite Route Tier
        if (eval.primaryIdx() < 0 || eval.primaryIdx() >= classCount || routes[eval.primaryIdx()] == null) {
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        routes[eval.primaryIdx()].execute(context, payload);
    }

    /**
     * Evaluates query supporting multi-intent pipelines, ambiguous clarification, and fallback isolation.
     */
    public void filterPipeline(Object context, String text, Object payload) throws Exception {
        GateEvaluation eval = evaluateFast(text);

        // 1. Fallback Tier
        if (eval.isFallback()) {
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        String pLabel = labels[eval.primaryIdx()];
        String sLabel = (eval.secondaryIdx() >= 0 && eval.secondaryIdx() < classCount) ? labels[eval.secondaryIdx()] : "";

        // 2. Multi-Intent Pipeline Tier
        if (eval.isPipeline() && !sLabel.isEmpty()) {
            String pipeKey = pipelineKey(pLabel, sLabel);
            PipelineAction pipeAction = pipelines.get(pipeKey);
            if (pipeAction != null) {
                pipeAction.execute(context, pLabel, sLabel, payload);
                return;
            }
        }

        // 3. Ambiguous Tier
        if (eval.isAmbiguous()) {
            if (ambiguous != null) {
                ambiguous.execute(context, pLabel, sLabel, payload);
                return;
            }
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        // 4. Definite Route Tier
        if (eval.primaryIdx() < 0 || eval.primaryIdx() >= classCount || routes[eval.primaryIdx()] == null) {
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        routes[eval.primaryIdx()].execute(context, payload);
    }

    /**
     * Evaluates pre-tokenized inputs with strictly zero heap allocations on the hot path.
     */
    public void filterTokens(Object context, int[] tokens, Object payload) throws Exception {
        GateEvaluation eval = evaluateFastTokens(tokens);

        if (eval.isFallback()) {
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        if (eval.isAmbiguous()) {
            if (ambiguous != null) {
                String pLabel = labels[eval.primaryIdx()];
                String sLabel = (eval.secondaryIdx() >= 0 && eval.secondaryIdx() < classCount) ? labels[eval.secondaryIdx()] : "";
                ambiguous.execute(context, pLabel, sLabel, payload);
                return;
            }
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        if (eval.primaryIdx() < 0 || eval.primaryIdx() >= classCount || routes[eval.primaryIdx()] == null) {
            if (fallback != null) {
                fallback.execute(context, payload);
            }
            return;
        }

        routes[eval.primaryIdx()].execute(context, payload);
    }
}
