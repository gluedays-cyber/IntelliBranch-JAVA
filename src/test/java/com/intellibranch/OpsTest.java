package com.intellibranch;

import com.intellibranch.core.Ops;

public class OpsTest {
    public static void runTests() {
        testSafeClamp();
        testGelu();
        testSoftmax();
        testL2Normalize();
        testDotProduct();
        System.out.println("  ✓ OpsTest passed");
    }

    private static void testSafeClamp() {
        if (Ops.safeClamp(Float.NaN, 100.0f) != 0.0f) {
            throw new AssertionError("safeClamp NaN failed");
        }
        if (Ops.safeClamp(Float.POSITIVE_INFINITY, 100.0f) != 0.0f) {
            throw new AssertionError("safeClamp Inf failed");
        }
        if (Ops.safeClamp(150.0f, 100.0f) != 100.0f) {
            throw new AssertionError("safeClamp upper bound failed");
        }
        if (Ops.safeClamp(-150.0f, 100.0f) != -100.0f) {
            throw new AssertionError("safeClamp lower bound failed");
        }
        if (Ops.safeClamp(5.5f, 100.0f) != 5.5f) {
            throw new AssertionError("safeClamp normal value failed");
        }
    }

    private static void testGelu() {
        float g0 = Ops.gelu(0.0f);
        if (Math.abs(g0) > 1e-6f) {
            throw new AssertionError("gelu(0) should be 0, got " + g0);
        }
        float gPositive = Ops.gelu(2.0f);
        if (gPositive <= 1.5f || gPositive >= 2.5f) {
            throw new AssertionError("gelu(2) out of range: " + gPositive);
        }
        float gNegative = Ops.gelu(-2.0f);
        if (gNegative >= 0.0f || gNegative <= -0.1f) {
            throw new AssertionError("gelu(-2) out of range: " + gNegative);
        }
    }

    private static void testSoftmax() {
        float[] logits = new float[]{1.0f, 2.0f, 3.0f};
        float[] probs = new float[3];
        Ops.softmax(logits, 3, 1.0f, probs);

        float sum = 0.0f;
        for (float p : probs) {
            sum += p;
        }
        if (Math.abs(sum - 1.0f) > 1e-4f) {
            throw new AssertionError("Softmax sum should be 1.0, got " + sum);
        }
        if (probs[2] <= probs[1] || probs[1] <= probs[0]) {
            throw new AssertionError("Softmax probabilities order is invalid");
        }
    }

    private static void testL2Normalize() {
        float[] vec = new float[]{3.0f, 4.0f};
        float[] out = new float[2];
        float norm = Ops.l2Normalize(vec, 2, out);
        if (Math.abs(norm - 5.0f) > 1e-5f) {
            throw new AssertionError("L2 norm should be 5, got " + norm);
        }
        if (Math.abs(out[0] - 0.6f) > 1e-5f || Math.abs(out[1] - 0.8f) > 1e-5f) {
            throw new AssertionError("Normalized vector values incorrect");
        }
    }

    private static void testDotProduct() {
        float[] a = new float[]{1.0f, 2.0f, 3.0f};
        float[] b = new float[]{4.0f, 5.0f, 6.0f};
        float dot = Ops.dotProduct(a, b, 3);
        if (Math.abs(dot - 32.0f) > 1e-5f) {
            throw new AssertionError("Dot product should be 32, got " + dot);
        }
    }
}
