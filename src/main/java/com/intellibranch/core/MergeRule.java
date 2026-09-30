package com.intellibranch.core;

/**
 * MergeRule represents a pair-to-target token merge rule for BPE.
 */
public record MergeRule(int token1, int token2, int target) {
}
