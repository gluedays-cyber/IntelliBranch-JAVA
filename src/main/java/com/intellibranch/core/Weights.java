package com.intellibranch.core;

import java.util.Arrays;

/**
 * Weights holds the linear algebra parameters for the embedding, positional encoding, and 2-layer MLP.
 */
public class Weights {
    private float[] embedding;
    private float[] positional;
    private float[] w1;
    private float[] b1;
    private float[] w2;
    private float[] b2;

    public Weights() {
    }

    public Weights(float[] embedding, float[] positional, float[] w1, float[] b1, float[] w2, float[] b2) {
        this.embedding = embedding;
        this.positional = positional;
        this.w1 = w1;
        this.b1 = b1;
        this.w2 = w2;
        this.b2 = b2;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public float[] getPositional() {
        return positional;
    }

    public void setPositional(float[] positional) {
        this.positional = positional;
    }

    public float[] getW1() {
        return w1;
    }

    public void setW1(float[] w1) {
        this.w1 = w1;
    }

    public float[] getB1() {
        return b1;
    }

    public void setB1(float[] b1) {
        this.b1 = b1;
    }

    public float[] getW2() {
        return w2;
    }

    public void setW2(float[] w2) {
        this.w2 = w2;
    }

    public float[] getB2() {
        return b2;
    }

    public void setB2(float[] b2) {
        this.b2 = b2;
    }

    public Weights copy() {
        return new Weights(
                embedding != null ? Arrays.copyOf(embedding, embedding.length) : null,
                positional != null ? Arrays.copyOf(positional, positional.length) : null,
                w1 != null ? Arrays.copyOf(w1, w1.length) : null,
                b1 != null ? Arrays.copyOf(b1, b1.length) : null,
                w2 != null ? Arrays.copyOf(w2, w2.length) : null,
                b2 != null ? Arrays.copyOf(b2, b2.length) : null
        );
    }
}
