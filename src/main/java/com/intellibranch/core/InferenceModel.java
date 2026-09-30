package com.intellibranch.core;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * In-memory neural backbone executing microsecond embedded inference over Little-Endian weights.
 */
public class InferenceModel {
    public static final int MAX_INPUT_BYTES = 512;
    public static final int MAX_SEQUENCE_TOKENS = 128;

    private final Header header;
    private final List<String> labels;
    private final List<String> vocab;
    private final List<MergeRule> mergeRules;
    private final Weights weights;
    private float temperature = 1.0f;
    private final BPETokenizer tokenizer;
    private final ThreadLocal<InferenceBuffer> bufferHolder;

    public InferenceModel(Header header, List<String> labels, List<String> vocab, List<MergeRule> mergeRules, Weights weights) {
        this.header = header;
        this.labels = List.copyOf(labels);
        this.vocab = List.copyOf(vocab);
        this.mergeRules = List.copyOf(mergeRules);
        this.weights = weights;
        this.tokenizer = new BPETokenizer(vocab, mergeRules);
        this.bufferHolder = ThreadLocal.withInitial(() ->
                new InferenceBuffer(header.embeddingDim(), header.hiddenDim(), header.numClasses()));
    }

    public Header getHeader() {
        return header;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<String> getVocab() {
        return vocab;
    }

    public List<MergeRule> getMergeRules() {
        return mergeRules;
    }

    public Weights getWeights() {
        return weights;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public BPETokenizer getTokenizer() {
        return tokenizer;
    }

    public InferenceBuffer getBuffer() {
        return bufferHolder.get();
    }

    /**
     * Internal forward pass executing directly inside the provided scratch buffer with zero heap allocations.
     */
    public void forwardInternal(int[] tokenIDs, int seqLen, float temp, InferenceBuffer buf) {
        if (seqLen <= 0) {
            throw new IllegalArgumentException("Input token slice cannot be empty");
        }

        // 1. Mean Pooling with Positional Encoding
        Ops.meanPoolingWithPos(tokenIDs, seqLen, weights.getEmbedding(), weights.getPositional(), header.embeddingDim(), buf.pooled);

        // 2. Layer 1 Linear: [EmbeddingDim] -> [HiddenDim]
        Ops.matMulVecAdd(buf.pooled, weights.getW1(), weights.getB1(), header.embeddingDim(), header.hiddenDim(), buf.hidden);

        // 3. GELU Activation In-Place
        Ops.geluInPlace(buf.hidden, header.hiddenDim());

        // 4. Layer 2 Linear: [HiddenDim] -> [NumClasses]
        Ops.matMulVecAdd(buf.hidden, weights.getW2(), weights.getB2(), header.hiddenDim(), header.numClasses(), buf.logits);

        // 5. Softmax with Temperature Scaling
        float t = (temp > 0.0f) ? temp : this.temperature;
        Ops.softmax(buf.logits, header.numClasses(), t, buf.probs);
    }

    /**
     * Executes forward pass and returns newly allocated probabilities array.
     */
    public float[] forward(int[] tokenIDs, float temp) {
        if (tokenIDs == null || tokenIDs.length == 0) {
            throw new IllegalArgumentException("Input token slice cannot be empty");
        }
        int len = Math.min(tokenIDs.length, MAX_SEQUENCE_TOKENS);
        InferenceBuffer buf = bufferHolder.get();
        forwardInternal(tokenIDs, len, temp, buf);
        return Arrays.copyOf(buf.probs, header.numClasses());
    }

    /**
     * Computes top-2 predictions and entropy with zero heap allocation using thread scratch buffer.
     */
    public StaticInferenceResult predictSlots(int[] tokenIDs, float temp) {
        if (tokenIDs == null || tokenIDs.length == 0) {
            return StaticInferenceResult.EMPTY;
        }
        int len = Math.min(tokenIDs.length, MAX_SEQUENCE_TOKENS);
        InferenceBuffer buf = bufferHolder.get();
        forwardInternal(tokenIDs, len, temp, buf);

        short top1Idx = -1, top2Idx = -1;
        float top1Prob = -1.0f, top2Prob = -1.0f;

        for (int i = 0; i < header.numClasses(); i++) {
            float p = buf.probs[i];
            short idx = (short) i;
            if (p > top1Prob) {
                top2Prob = top1Prob;
                top2Idx = top1Idx;
                top1Prob = p;
                top1Idx = idx;
            } else if (p > top2Prob) {
                top2Prob = p;
                top2Idx = idx;
            }
        }

        MatchSlot pSlot = (top1Idx >= 0) ? new MatchSlot(top1Idx, top1Prob) : MatchSlot.EMPTY;
        MatchSlot sSlot = (top2Idx >= 0) ? new MatchSlot(top2Idx, top2Prob) : MatchSlot.EMPTY;
        int total = (top1Idx >= 0 ? 1 : 0) + (top2Idx >= 0 ? 1 : 0);
        float entropy = Ops.computeEntropy(buf.probs, header.numClasses());

        return new StaticInferenceResult(pSlot, sSlot, entropy, total);
    }

    public record Prediction(String label, double confidence) {}

    public record DetailedResult(StaticInferenceResult slots, double unkRatio) {}

    /**
     * Predicts class label and confidence score from pre-tokenized IDs.
     */
    public Prediction predictTokens(int[] tokenIDs) {
        StaticInferenceResult res = predictSlots(tokenIDs, temperature);
        if (!res.hasPrimary()) {
            return new Prediction("", 0.0);
        }
        int idx = res.primary().index();
        if (idx < 0 || idx >= labels.size()) {
            return new Prediction("", 0.0);
        }
        return new Prediction(labels.get(idx), (double) res.primary().confidence());
    }

    /**
     * Executes inference with full OOV unknown-token penalty and length guards.
     */
    public DetailedResult predictDetailed(String text) {
        if (text == null || text.isEmpty()) {
            return new DetailedResult(StaticInferenceResult.EMPTY, 0.0);
        }
        text = truncateToUtf8Boundary(text, MAX_INPUT_BYTES);
        int[] tokenIDs = tokenizer.encode(text);
        if (tokenIDs == null || tokenIDs.length == 0) {
            return new DetailedResult(StaticInferenceResult.EMPTY, 0.0);
        }
        int len = Math.min(tokenIDs.length, MAX_SEQUENCE_TOKENS);

        StaticInferenceResult res = predictSlots(tokenIDs, temperature);

        double unkRatio = 0.0;
        Integer unkID = tokenizer.getVocabMap().get(BPETokenizer.UNK_TOKEN);
        if (unkID != null && len > 0) {
            int unkCount = 0;
            for (int i = 0; i < len; i++) {
                if (tokenIDs[i] == unkID) {
                    unkCount++;
                }
            }
            unkRatio = (double) unkCount / (double) len;
            float decay = (float) (1.0 - unkRatio);
            MatchSlot newP = new MatchSlot(res.primary().index(), res.primary().confidence() * decay);
            MatchSlot newS = new MatchSlot(res.secondary().index(), res.secondary().confidence() * decay);
            res = new StaticInferenceResult(newP, newS, res.entropy(), res.total());
        }

        return new DetailedResult(res, unkRatio);
    }

    /**
     * Tokenizes raw text with subword BPE and returns top prediction with OOV penalty.
     */
    public Prediction predict(String text) {
        if (text == null || text.isEmpty()) {
            return new Prediction("", 0.0);
        }
        text = truncateToUtf8Boundary(text, MAX_INPUT_BYTES);
        int[] tokenIDs = tokenizer.encode(text);
        if (tokenIDs == null || tokenIDs.length == 0) {
            return new Prediction("", 0.0);
        }
        int len = Math.min(tokenIDs.length, MAX_SEQUENCE_TOKENS);

        Prediction pred = predictTokens(tokenIDs);
        Integer unkID = tokenizer.getVocabMap().get(BPETokenizer.UNK_TOKEN);
        if (unkID != null && len > 0) {
            int unkCount = 0;
            for (int i = 0; i < len; i++) {
                if (tokenIDs[i] == unkID) {
                    unkCount++;
                }
            }
            double unkRatio = (double) unkCount / (double) len;
            return new Prediction(pred.label(), pred.confidence() * (1.0 - unkRatio));
        }

        return pred;
    }

    /**
     * Fills the provided outPooled and outLogits buffers directly without allocations.
     */
    public void predictFeatures(int[] tokenIDs, float[] outPooled, float[] outLogits) {
        if (tokenIDs == null || tokenIDs.length == 0) {
            return;
        }
        int len = Math.min(tokenIDs.length, MAX_SEQUENCE_TOKENS);
        InferenceBuffer buf = bufferHolder.get();

        // 1. Mean Pooling with Positional Encoding
        Ops.meanPoolingWithPos(tokenIDs, len, weights.getEmbedding(), weights.getPositional(), header.embeddingDim(), buf.pooled);

        // 2. Layer 1 Linear
        Ops.matMulVecAdd(buf.pooled, weights.getW1(), weights.getB1(), header.embeddingDim(), header.hiddenDim(), buf.hidden);

        // 3. GELU Activation In-Place
        Ops.geluInPlace(buf.hidden, header.hiddenDim());

        // 4. Layer 2 Linear
        Ops.matMulVecAdd(buf.hidden, weights.getW2(), weights.getB2(), header.hiddenDim(), header.numClasses(), buf.logits);

        if (outPooled != null) {
            int pLen = Math.min(outPooled.length, header.embeddingDim());
            System.arraycopy(buf.pooled, 0, outPooled, 0, pLen);
        }
        if (outLogits != null) {
            int lLen = Math.min(outLogits.length, header.numClasses());
            System.arraycopy(buf.logits, 0, outLogits, 0, lLen);
        }
    }

    /**
     * Truncates text to at most maxBytes without slicing multi-byte UTF-8 code points.
     */
    public static String truncateToUtf8Boundary(String text, int maxBytes) {
        if (text == null) return "";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return text;
        }
        int idx = maxBytes;
        while (idx > 0 && (bytes[idx] & 0xC0) == 0x80) {
            idx--;
        }
        return new String(bytes, 0, idx, StandardCharsets.UTF_8);
    }
}
