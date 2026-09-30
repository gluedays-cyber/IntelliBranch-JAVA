package com.intellibranch.core;

/**
 * Thread-local scratch buffer for zero-allocation neural forward passes.
 */
public class InferenceBuffer {
    public final float[] pooled;
    public final float[] hidden;
    public final float[] logits;
    public final float[] probs;

    public InferenceBuffer(int embeddingDim, int hiddenDim, int numClasses) {
        this.pooled = new float[embeddingDim];
        this.hidden = new float[hiddenDim];
        this.logits = new float[numClasses];
        this.probs = new float[numClasses];
    }
}
