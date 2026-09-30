package com.intellibranch.core;

/**
 * MatchSlot captures a predicted class index and its normalized confidence score.
 */
public record MatchSlot(short index, float confidence) {
    public static final MatchSlot EMPTY = new MatchSlot((short) -1, 0.0f);
}
