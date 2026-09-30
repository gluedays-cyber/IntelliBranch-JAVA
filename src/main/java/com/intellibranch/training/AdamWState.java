package com.intellibranch.training;

/**
 * AdamW optimizer state maintaining first and second momentum vectors with decoupled weight decay.
 */
public class AdamWState {
    private final float[] m;
    private final float[] v;
    private int t;

    public AdamWState(int size) {
        this.m = new float[size];
        this.v = new float[size];
        this.t = 0;
    }

    public void step(float[] param, float[] grad, float lr, float weightDecay, float beta1, float beta2, float eps) {
        t++;
        double tDouble = (double) t;
        float bc1 = (float) (1.0 - Math.pow((double) beta1, tDouble));
        float bc2 = (float) (1.0 - Math.pow((double) beta2, tDouble));

        for (int i = 0; i < param.length; i++) {
            float g = grad[i];

            // Update biased 1st and 2nd moment
            m[i] = beta1 * m[i] + (1.0f - beta1) * g;
            v[i] = beta2 * v[i] + (1.0f - beta2) * (g * g);

            // Bias-corrected estimates
            float mHat = m[i] / bc1;
            float vHat = v[i] / bc2;

            // Decoupled weight decay and Adam parameter step
            param[i] -= lr * (mHat / ((float) Math.sqrt((double) vHat) + eps) + weightDecay * param[i]);
        }
    }
}
