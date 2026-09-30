package com.intellibranch.training;

/**
 * TrainConfig defines the hyperparameter specification for offline neural model training.
 */
public class TrainConfig {
    private int embeddingDim = 64;
    private int hiddenDim = 128;
    private int targetVocabSize = 200;
    private float learningRate = 0.001f;
    private float weightDecay = 0.01f;
    private float beta1 = 0.9f;
    private float beta2 = 0.999f;
    private float epsilon = 1e-8f;
    private int epochs = 100;
    private int batchSize = 32;
    private int patience = 5;
    private long seed = 42L;

    public static TrainConfig defaultConfig() {
        return new TrainConfig();
    }

    public int getEmbeddingDim() {
        return embeddingDim;
    }

    public TrainConfig setEmbeddingDim(int embeddingDim) {
        this.embeddingDim = embeddingDim;
        return this;
    }

    public int getHiddenDim() {
        return hiddenDim;
    }

    public TrainConfig setHiddenDim(int hiddenDim) {
        this.hiddenDim = hiddenDim;
        return this;
    }

    public int getTargetVocabSize() {
        return targetVocabSize;
    }

    public TrainConfig setTargetVocabSize(int targetVocabSize) {
        this.targetVocabSize = targetVocabSize;
        return this;
    }

    public float getLearningRate() {
        return learningRate;
    }

    public TrainConfig setLearningRate(float learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public float getWeightDecay() {
        return weightDecay;
    }

    public TrainConfig setWeightDecay(float weightDecay) {
        this.weightDecay = weightDecay;
        return this;
    }

    public float getBeta1() {
        return beta1;
    }

    public TrainConfig setBeta1(float beta1) {
        this.beta1 = beta1;
        return this;
    }

    public float getBeta2() {
        return beta2;
    }

    public TrainConfig setBeta2(float beta2) {
        this.beta2 = beta2;
        return this;
    }

    public float getEpsilon() {
        return epsilon;
    }

    public TrainConfig setEpsilon(float epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    public int getEpochs() {
        return epochs;
    }

    public TrainConfig setEpochs(int epochs) {
        this.epochs = epochs;
        return this;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public TrainConfig setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public int getPatience() {
        return patience;
    }

    public TrainConfig setPatience(int patience) {
        this.patience = patience;
        return this;
    }

    public long getSeed() {
        return seed;
    }

    public TrainConfig setSeed(long seed) {
        this.seed = seed;
        return this;
    }
}
