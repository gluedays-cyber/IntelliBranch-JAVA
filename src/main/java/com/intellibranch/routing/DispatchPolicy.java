package com.intellibranch.routing;

/**
 * DispatchPolicy defines the 3-tier confidence criteria, multi-intent threshold, and OOD entropy boundary.
 */
public record DispatchPolicy(
        double highThreshold,
        double lowThreshold,
        double marginCutoff,
        double maxEntropy,
        double pipelineThreshold,
        double minLogSumExp
) {
    public static DispatchPolicy defaultPolicy() {
        return new DispatchPolicy(0.75, 0.40, 0.15, 2.0, 0.30, 0.0);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private double highThreshold = 0.75;
        private double lowThreshold = 0.40;
        private double marginCutoff = 0.15;
        private double maxEntropy = 2.0;
        private double pipelineThreshold = 0.30;
        private double minLogSumExp = 0.0;

        public Builder highThreshold(double val) {
            this.highThreshold = val;
            return this;
        }

        public Builder lowThreshold(double val) {
            this.lowThreshold = val;
            return this;
        }

        public Builder marginCutoff(double val) {
            this.marginCutoff = val;
            return this;
        }

        public Builder maxEntropy(double val) {
            this.maxEntropy = val;
            return this;
        }

        public Builder pipelineThreshold(double val) {
            this.pipelineThreshold = val;
            return this;
        }

        public Builder minLogSumExp(double val) {
            this.minLogSumExp = val;
            return this;
        }

        public DispatchPolicy build() {
            return new DispatchPolicy(highThreshold, lowThreshold, marginCutoff, maxEntropy, pipelineThreshold, minLogSumExp);
        }
    }
}
