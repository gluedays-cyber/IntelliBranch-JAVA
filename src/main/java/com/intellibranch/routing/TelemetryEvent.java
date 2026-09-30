package com.intellibranch.routing;

/**
 * TelemetryEvent captures diagnostic metadata for ambiguous, fallback, or multi-intent routing queries.
 */
public record TelemetryEvent(
        String inputText,
        String predictedLabel,
        String secondaryLabel,
        double confidence,
        double entropy,
        boolean isAmbiguous,
        boolean isPipeline,
        boolean isFallback,
        long timestampNano
) {
    public TelemetryEvent(String inputText, String predictedLabel, String secondaryLabel,
                          double confidence, double entropy, boolean isAmbiguous,
                          boolean isPipeline, boolean isFallback) {
        this(inputText, predictedLabel, secondaryLabel, confidence, entropy, isAmbiguous, isPipeline, isFallback, System.nanoTime());
    }
}
