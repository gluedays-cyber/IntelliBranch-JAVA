package com.intellibranch.routing;

import com.intellibranch.core.BinaryModel;
import com.intellibranch.core.InferenceModel;
import com.intellibranch.core.StaticInferenceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Router coordinates in-memory inference routing with atomic hot-swap, 3-tier safety, and telemetry feedback.
 */
public class Router {
    private final AtomicReference<InferenceModel> modelRef = new AtomicReference<>();
    private volatile double threshold;
    private volatile DispatchPolicy policy;

    private final Map<String, RouteAction> routes = new ConcurrentHashMap<>();
    private final Map<String, PipelineAction> pipelines = new ConcurrentHashMap<>();
    private volatile PipelineAction defaultPipeline;
    private volatile AmbiguousAction ambiguous;
    private volatile RouteAction fallback = (ctx, payload) -> {};

    private volatile TelemetryRingBuffer telemetry = new TelemetryRingBuffer(1024);

    public Router(InferenceModel model, double defaultThreshold) {
        this.modelRef.set(model);
        this.threshold = defaultThreshold;
        DispatchPolicy defaultP = DispatchPolicy.defaultPolicy();
        if (defaultThreshold > 0.0) {
            this.policy = DispatchPolicy.builder()
                    .highThreshold(defaultThreshold)
                    .lowThreshold(defaultThreshold * 0.6)
                    .marginCutoff(defaultP.marginCutoff())
                    .maxEntropy(defaultP.maxEntropy())
                    .pipelineThreshold(defaultP.pipelineThreshold())
                    .minLogSumExp(defaultP.minLogSumExp())
                    .build();
        } else {
            this.policy = defaultP;
        }
    }

    public static Router load(Path modelPath, double defaultThreshold) throws IOException {
        InferenceModel model = BinaryModel.load(modelPath);
        return new Router(model, defaultThreshold);
    }

    public void reload(Path modelPath) throws IOException {
        InferenceModel newModel = BinaryModel.load(modelPath);
        modelRef.set(newModel);
    }

    public void swapModel(InferenceModel newModel) {
        modelRef.set(newModel);
    }

    public InferenceModel getModel() {
        return modelRef.get();
    }

    public Router enableTelemetry(int capacity) {
        this.telemetry = new TelemetryRingBuffer(capacity);
        return this;
    }

    public List<TelemetryEvent> drainTelemetry() {
        return telemetry != null ? telemetry.drain() : List.of();
    }

    private void recordTelemetry(String text, String primary, String secondary, double conf, double entropy,
                                 boolean isAmbiguous, boolean isPipeline, boolean isFallback) {
        if (telemetry != null && (isAmbiguous || isPipeline || isFallback)) {
            telemetry.push(new TelemetryEvent(text, primary, secondary, conf, entropy, isAmbiguous, isPipeline, isFallback));
        }
    }

    public Router setPolicy(DispatchPolicy policy) {
        this.policy = policy;
        return this;
    }

    public DispatchPolicy getPolicy() {
        return policy;
    }

    public Router bind(String label, RouteAction action) {
        routes.put(label, action);
        return this;
    }

    public Router bindPipeline(String primary, String secondary, PipelineAction action) {
        pipelines.put(pipelineKey(primary, secondary), action);
        return this;
    }

    public Router defaultPipeline(PipelineAction action) {
        this.defaultPipeline = action;
        return this;
    }

    public Router ambiguous(AmbiguousAction action) {
        this.ambiguous = action;
        return this;
    }

    public Router fallback(RouteAction action) {
        this.fallback = action;
        return this;
    }

    private static String pipelineKey(String primary, String secondary) {
        return primary + "->" + secondary;
    }

    /**
     * Executes microsecond inference and routes through a 3-tier decision pipeline (Definite / Ambiguous / Fallback).
     */
    public void dispatch(Object context, String text, Object payload) throws Exception {
        InferenceModel model = modelRef.get();
        if (model == null) {
            recordTelemetry(text, "", "", 0.0, 0.0, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        InferenceModel.DetailedResult detailed = model.predictDetailed(text);
        StaticInferenceResult res = detailed.slots();
        double unkRatio = detailed.unkRatio();

        if (res.total() == 0 || !res.hasPrimary()) {
            recordTelemetry(text, "", "", 0.0, 0.0, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        int primaryIdx = res.primary().index();
        if (primaryIdx < 0 || primaryIdx >= model.getLabels().size()) {
            recordTelemetry(text, "", "", 0.0, 0.0, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        String primaryLabel = model.getLabels().get(primaryIdx);
        double primaryConf = res.primary().confidence();

        String secondaryLabel = "";
        double secondaryConf = 0.0;
        if (res.hasSecondary()) {
            int secIdx = res.secondary().index();
            if (secIdx >= 0 && secIdx < model.getLabels().size()) {
                secondaryLabel = model.getLabels().get(secIdx);
                secondaryConf = res.secondary().confidence();
            }
        }

        double margin = primaryConf - secondaryConf;
        double entropy = res.entropy();

        // 1. Fallback Isolation
        if (primaryConf < policy.lowThreshold() || unkRatio >= 0.5 || entropy > policy.maxEntropy()) {
            recordTelemetry(text, primaryLabel, secondaryLabel, primaryConf, entropy, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        // 2. Ambiguous Route
        boolean isAmbiguous = primaryConf < policy.highThreshold() || margin < policy.marginCutoff();
        if (isAmbiguous) {
            recordTelemetry(text, primaryLabel, secondaryLabel, primaryConf, entropy, true, false, false);
            if (ambiguous != null) {
                ambiguous.execute(context, primaryLabel, secondaryLabel, payload);
                return;
            }
            fallback.execute(context, payload);
            return;
        }

        // 3. Definite Route
        RouteAction action = routes.get(primaryLabel);
        if (action == null) {
            recordTelemetry(text, primaryLabel, secondaryLabel, primaryConf, entropy, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        action.execute(context, payload);
    }

    /**
     * Routes requests with multi-intent support, executing pipeline handlers when both top-1 and top-2 are eligible.
     */
    public void dispatchPipeline(Object context, String text, Object payload) throws Exception {
        InferenceModel model = modelRef.get();
        if (model == null) {
            recordTelemetry(text, "", "", 0.0, 0.0, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        InferenceModel.DetailedResult detailed = model.predictDetailed(text);
        StaticInferenceResult res = detailed.slots();
        double unkRatio = detailed.unkRatio();

        if (res.total() == 0 || !res.hasPrimary()) {
            recordTelemetry(text, "", "", 0.0, 0.0, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        int primaryIdx = res.primary().index();
        if (primaryIdx < 0 || primaryIdx >= model.getLabels().size()) {
            recordTelemetry(text, "", "", 0.0, 0.0, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        String primaryLabel = model.getLabels().get(primaryIdx);
        double primaryConf = res.primary().confidence();

        String secondaryLabel = "";
        double secondaryConf = 0.0;
        if (res.hasSecondary()) {
            int secIdx = res.secondary().index();
            if (secIdx >= 0 && secIdx < model.getLabels().size()) {
                secondaryLabel = model.getLabels().get(secIdx);
                secondaryConf = res.secondary().confidence();
            }
        }

        double margin = primaryConf - secondaryConf;
        double entropy = res.entropy();

        // 1. Fallback Isolation
        if (primaryConf < policy.lowThreshold() || unkRatio >= 0.5 || entropy > policy.maxEntropy()) {
            recordTelemetry(text, primaryLabel, secondaryLabel, primaryConf, entropy, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        // 2. Multi-Intent Pipeline
        if (!secondaryLabel.isEmpty() && secondaryConf >= policy.pipelineThreshold()) {
            String pKey = pipelineKey(primaryLabel, secondaryLabel);
            PipelineAction pipeAction = pipelines.get(pKey);
            if (pipeAction != null) {
                recordTelemetry(text, primaryLabel, secondaryLabel, primaryConf, entropy, false, true, false);
                pipeAction.execute(context, primaryLabel, secondaryLabel, payload);
                return;
            } else if (defaultPipeline != null) {
                recordTelemetry(text, primaryLabel, secondaryLabel, primaryConf, entropy, false, true, false);
                defaultPipeline.execute(context, primaryLabel, secondaryLabel, payload);
                return;
            }
        }

        // 3. Ambiguous Route
        boolean isAmbiguous = primaryConf < policy.highThreshold() || margin < policy.marginCutoff();
        if (isAmbiguous) {
            recordTelemetry(text, primaryLabel, secondaryLabel, primaryConf, entropy, true, false, false);
            if (ambiguous != null) {
                ambiguous.execute(context, primaryLabel, secondaryLabel, payload);
                return;
            }
            fallback.execute(context, payload);
            return;
        }

        // 4. Definite Route
        RouteAction action = routes.get(primaryLabel);
        if (action == null) {
            recordTelemetry(text, primaryLabel, secondaryLabel, primaryConf, entropy, false, false, true);
            fallback.execute(context, payload);
            return;
        }

        action.execute(context, payload);
    }

    /**
     * Evaluates input text and generates a full diagnostic RouteTrace.
     */
    public RouteTrace inspect(String text) {
        long start = System.nanoTime();
        InferenceModel model = modelRef.get();
        if (model == null) {
            long lat = (System.nanoTime() - start) / 1000;
            return RouteTrace.fallback(text, "model not loaded", threshold, lat);
        }

        String safeText = InferenceModel.truncateToUtf8Boundary(text, InferenceModel.MAX_INPUT_BYTES);
        int[] tokens = model.getTokenizer().encode(safeText);
        if (tokens == null || tokens.length == 0) {
            long lat = (System.nanoTime() - start) / 1000;
            return RouteTrace.fallback(text, "empty input tokens", threshold, lat);
        }
        int len = Math.min(tokens.length, InferenceModel.MAX_SEQUENCE_TOKENS);
        tokens = Arrays.copyOf(tokens, len);

        String[] subwords = new String[len];
        int unkCount = 0;
        Integer unkID = model.getTokenizer().getVocabMap().get(com.intellibranch.core.BPETokenizer.UNK_TOKEN);
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

        float[] probs = model.forward(tokens, model.getTemperature());
        Map<String, Float> probMap = new LinkedHashMap<>();

        String bestLabel = "";
        String secondLabel = "";
        float bestScore = -1.0f;
        float secondScore = -1.0f;

        for (int i = 0; i < model.getLabels().size(); i++) {
            String lbl = model.getLabels().get(i);
            float p = probs[i];
            probMap.put(lbl, p);
            if (p > bestScore) {
                secondScore = bestScore;
                secondLabel = bestLabel;
                bestScore = p;
                bestLabel = lbl;
            } else if (p > secondScore) {
                secondScore = p;
                secondLabel = lbl;
            }
        }

        double calibratedConfidence = (double) bestScore * (1.0 - unkRatio);
        double calibratedSecond = (double) secondScore * (1.0 - unkRatio);
        double margin = calibratedConfidence - calibratedSecond;
        double entropy = com.intellibranch.core.Ops.computeEntropy(probs, probs.length);

        long latencyMicros = (System.nanoTime() - start) / 1000;

        boolean isFallback = false;
        String fallbackReason = "";
        boolean isPipeline = false;
        boolean isAmbiguous = false;

        if (calibratedConfidence < policy.lowThreshold()) {
            isFallback = true;
            fallbackReason = String.format(Locale.US, "confidence %.4f below low threshold %.4f", calibratedConfidence, policy.lowThreshold());
        } else if (unkRatio >= 0.5) {
            isFallback = true;
            fallbackReason = String.format(Locale.US, "excessive unknown tokens (%.2f >= 0.50)", unkRatio);
        } else if (entropy > policy.maxEntropy()) {
            isFallback = true;
            fallbackReason = String.format(Locale.US, "prediction entropy %.4f exceeds limit %.4f (OOD)", entropy, policy.maxEntropy());
        } else {
            if (!secondLabel.isEmpty() && calibratedSecond >= policy.pipelineThreshold()) {
                isPipeline = true;
            }
            if (calibratedConfidence < policy.highThreshold() || margin < policy.marginCutoff()) {
                isAmbiguous = true;
            }
            if (!routes.containsKey(bestLabel)) {
                isFallback = true;
                fallbackReason = String.format("label '%s' has no bound route handler", bestLabel);
            }
        }

        return new RouteTrace(
                text,
                tokens,
                subwords,
                unkRatio,
                probMap,
                bestLabel,
                secondLabel,
                calibratedConfidence,
                margin,
                entropy,
                policy.highThreshold(),
                isAmbiguous,
                isPipeline,
                isFallback,
                fallbackReason,
                latencyMicros
        );
    }
}
