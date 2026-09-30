package com.intellibranch.core;

/**
 * StaticInferenceResult encapsulates top-2 ranked prediction slots and uncertainty entropy.
 */
public record StaticInferenceResult(
        MatchSlot primary,
        MatchSlot secondary,
        float entropy,
        int total
) {
    public static final StaticInferenceResult EMPTY = new StaticInferenceResult(MatchSlot.EMPTY, MatchSlot.EMPTY, 0.0f, 0);

    public boolean hasPrimary() {
        return total >= 1 && primary != null && primary.index() >= 0;
    }

    public boolean hasSecondary() {
        return total >= 2 && secondary != null && secondary.index() >= 0;
    }
}
