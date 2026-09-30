package com.intellibranch.routing;

import java.util.Map;

/**
 * RouteTrace encapsulates comprehensive diagnostic metadata explaining a routing decision.
 */
public record RouteTrace(
        String inputText,
        int[] tokenIDs,
        String[] subwords,
        double unknownTokenRatio,
        Map<String, Float> classProbabilities,
        String predictedLabel,
        String secondaryLabel,
        double confidence,
        double margin,
        double entropy,
        double threshold,
        boolean isAmbiguous,
        boolean isPipeline,
        boolean isFallback,
        String fallbackReason,
        long latencyMicros
) {
    public static RouteTrace fallback(String text, String reason, double threshold, long latencyMicros) {
        return new RouteTrace(
                text,
                new int[0],
                new String[0],
                0.0,
                Map.of(),
                "",
                "",
                0.0,
                0.0,
                0.0,
                threshold,
                false,
                false,
                true,
                reason,
                latencyMicros
        );
    }
}
