package com.intellibranch.core;

import java.util.Arrays;

/**
 * High-performance, numerically protected mathematical and tensor operations for neural gating.
 */
public final class Ops {
    public static final float SQRT_2_OVER_PI = 0.7978845608f;
    public static final float GELU_COEFF = 0.044715f;
    public static final float NUMERICAL_CLAMP_LIMIT = 100.0f;

    private Ops() {
    }

    /**
     * Restricts a float value within [-limit, limit] and replaces NaN/Inf with 0.0.
     */
    public static float safeClamp(float val, float limit) {
        if (Float.isNaN(val) || Float.isInfinite(val)) {
            return 0.0f;
        }
        if (val < -limit) {
            return -limit;
        }
        if (val > limit) {
            return limit;
        }
        return val;
    }

    /**
     * Calculates the Gaussian Error Linear Unit (GELU) activation using standard tanh approximation.
     * GELU(x) = 0.5 * x * (1 + tanh(sqrt(2 / pi) * (x + 0.044715 * x^3)))
     */
    public static float gelu(float x) {
        x = safeClamp(x, NUMERICAL_CLAMP_LIMIT);
        float cube = x * x * x;
        float inner = SQRT_2_OVER_PI * (x + GELU_COEFF * cube);
        inner = safeClamp(inner, NUMERICAL_CLAMP_LIMIT);
        float tanhVal = (float) Math.tanh(inner);
        return safeClamp(0.5f * x * (1.0f + tanhVal), NUMERICAL_CLAMP_LIMIT);
    }

    /**
     * Calculates d(GELU(x))/dx for backpropagation.
     */
    public static float geluDerivative(float x) {
        float cube = x * x * x;
        float u = SQRT_2_OVER_PI * (x + GELU_COEFF * cube);
        float tanhU = (float) Math.tanh(u);
        float du = SQRT_2_OVER_PI * (1.0f + 3.0f * GELU_COEFF * x * x);
        float sech2 = 1.0f - tanhU * tanhU;
        return 0.5f * (1.0f + tanhU) + 0.5f * x * sech2 * du;
    }

    /**
     * Applies the GELU activation function in-place across a float slice.
     */
    public static void geluInPlace(float[] vec, int length) {
        int n = Math.min(vec.length, length);
        for (int i = 0; i < n; i++) {
            vec[i] = gelu(vec[i]);
        }
    }

    /**
     * Computes the average embedding vector across the token IDs with learned positional embeddings.
     */
    public static void meanPoolingWithPos(int[] tokenIDs, int seqLen, float[] embeddingTable, float[] posTable, int embDim, float[] out) {
        if (seqLen <= 0) {
            throw new IllegalArgumentException("Cannot pool over zero tokens");
        }
        Arrays.fill(out, 0, embDim, 0.0f);

        int maxSeq = (embDim > 0 && posTable != null && posTable.length > 0) ? posTable.length / embDim : 0;

        for (int pos = 0; pos < seqLen; pos++) {
            int tokID = tokenIDs[pos];
            int tokOffset = tokID * embDim;
            if (tokOffset + embDim > embeddingTable.length) {
                throw new IndexOutOfBoundsException("Token ID " + tokID + " exceeds embedding table bounds");
            }

            int posOffset = pos * embDim;
            boolean hasPos = maxSeq > 0 && pos < maxSeq;

            for (int d = 0; d < embDim; d++) {
                float val = safeClamp(embeddingTable[tokOffset + d], NUMERICAL_CLAMP_LIMIT);
                if (hasPos) {
                    val = gelu(val + posTable[posOffset + d]);
                }
                out[d] = safeClamp(out[d] + val, NUMERICAL_CLAMP_LIMIT);
            }
        }

        float invLen = 1.0f / (float) seqLen;
        for (int d = 0; d < embDim; d++) {
            out[d] = safeClamp(out[d] * invLen, NUMERICAL_CLAMP_LIMIT);
        }
    }

    /**
     * Computes out = vec * weights + bias where vec is [1 x inDim], weights is [inDim x outDim] row-major,
     * bias is [outDim], and out is [outDim].
     */
    public static void matMulVecAdd(float[] vec, float[] weights, float[] bias, int inDim, int outDim, float[] out) {
        for (int j = 0; j < outDim; j++) {
            out[j] = safeClamp(bias[j], NUMERICAL_CLAMP_LIMIT);
        }

        for (int i = 0; i < inDim; i++) {
            float v = safeClamp(vec[i], NUMERICAL_CLAMP_LIMIT);
            if (v == 0.0f) {
                continue;
            }
            int rowOffset = i * outDim;
            for (int j = 0; j < outDim; j++) {
                float w = safeClamp(weights[rowOffset + j], NUMERICAL_CLAMP_LIMIT);
                out[j] = safeClamp(out[j] + v * w, NUMERICAL_CLAMP_LIMIT);
            }
        }
    }

    /**
     * Computes numerically stable softmax probabilities over logits with temperature scaling.
     */
    public static void softmax(float[] logits, int numClasses, float temperature, float[] out) {
        if (numClasses <= 0) {
            throw new IllegalArgumentException("Empty logits");
        }
        if (temperature <= 0.0f || Float.isNaN(temperature) || Float.isInfinite(temperature)) {
            temperature = 1.0f;
        }

        float invTemp = 1.0f / temperature;

        float maxLogit = safeClamp(logits[0], NUMERICAL_CLAMP_LIMIT) * invTemp;
        for (int i = 1; i < numClasses; i++) {
            float scaled = safeClamp(logits[i], NUMERICAL_CLAMP_LIMIT) * invTemp;
            if (scaled > maxLogit) {
                maxLogit = scaled;
            }
        }

        float sumExp = 0.0f;
        for (int i = 0; i < numClasses; i++) {
            float val = safeClamp(logits[i], NUMERICAL_CLAMP_LIMIT) * invTemp - maxLogit;
            float e = (float) Math.exp(val);
            if (Float.isNaN(e) || Float.isInfinite(e)) {
                e = 0.0f;
            }
            out[i] = e;
            sumExp += e;
        }

        if (sumExp <= 0.0f || Float.isNaN(sumExp) || Float.isInfinite(sumExp)) {
            float uniform = 1.0f / (float) numClasses;
            Arrays.fill(out, 0, numClasses, uniform);
            return;
        }

        float invSum = 1.0f / sumExp;
        for (int i = 0; i < numClasses; i++) {
            out[i] *= invSum;
        }
    }

    /**
     * Computes out = vec / ||vec||2 with numerical safety. Returns the original Euclidean norm.
     */
    public static float l2Normalize(float[] vec, int length, float[] out) {
        double sumSq = 0.0;
        for (int i = 0; i < length; i++) {
            sumSq += (double) vec[i] * (double) vec[i];
        }
        float norm = (float) Math.sqrt(sumSq);
        if (norm < 1e-7f || Float.isNaN(norm) || Float.isInfinite(norm)) {
            Arrays.fill(out, 0, length, 0.0f);
            return 0.0f;
        }
        float invNorm = 1.0f / norm;
        for (int i = 0; i < length; i++) {
            out[i] = vec[i] * invNorm;
        }
        return norm;
    }

    /**
     * Computes the dot product between two float vectors without allocations.
     */
    public static float dotProduct(float[] a, float[] b, int length) {
        float sum = 0.0f;
        for (int i = 0; i < length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    /**
     * Computes Shannon entropy in bits with epsilon guards.
     */
    public static float computeEntropy(float[] probs, int length) {
        double entropy = 0.0;
        double log2 = Math.log(2.0);
        for (int i = 0; i < length; i++) {
            float p = probs[i];
            if (p > 1e-7f) {
                entropy -= (double) p * (Math.log(p) / log2);
            }
        }
        return (float) entropy;
    }
}
