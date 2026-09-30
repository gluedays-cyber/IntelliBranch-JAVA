package com.intellibranch.neurogate;

import java.util.List;
import java.util.Map;

/**
 * GateTrace captures comprehensive runtime metrics across all three geometric heads of NeuroGate.
 */
public record GateTrace(
        String inputText,
        int[] tokenIDs,
        String[] subwords,
        double unknownTokenRatio,
        float cosineSimilarity,
        boolean isOOD,
        long anchorBitmask,
        List<String> triggeredAnchors,
        Map<String, Float> classProbabilities,
        String predictedLabel,
        String secondaryLabel,
        double confidence,
        double margin,
        double entropy,
        double logSumExp,
        double freeEnergy,
        double threshold,
        boolean isAmbiguous,
        boolean isPipeline,
        boolean isFallback,
        String fallbackReason,
        long latencyMicros
) {
    public static GateTrace fallback(String text, String reason, long latencyMicros) {
        return new GateTrace(
                text,
                new int[0],
                new String[0],
                0.0,
                0.0f,
                true,
                0L,
                List.of(),
                Map.of(),
                "",
                "",
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                false,
                false,
                true,
                reason,
                latencyMicros
        );
    }
}
