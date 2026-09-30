package com.intellibranch.training;

import com.intellibranch.core.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * High-performance in-memory offline neural model trainer with BPE tokenization, AdamW, and early stopping.
 */
public class Trainer {

    /**
     * Reads training data from a two-column (text,label) CSV file.
     */
    public static List<DataSample> loadCSVDataset(Path filePath) throws IOException {
        List<DataSample> samples = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + filePath);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = parseCsvLine(line);
                if (parts.length >= 2) {
                    String text = parts[0].trim();
                    String label = parts[1].trim();
                    if (!text.isEmpty() && !label.isEmpty()) {
                        samples.add(new DataSample(text, label));
                    }
                }
            }
        }

        if (samples.isEmpty()) {
            throw new IOException("Dataset contains no valid records: " + filePath);
        }
        return samples;
    }

    private static String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private record EncodedSample(int[] tokens, int classID) {}

    /**
     * Executes the complete training pipeline including BPE, AdamW optimization, and early stopping.
     */
    public static InferenceModel trainModel(List<DataSample> samples, TrainConfig cfg) {
        if (samples == null || samples.size() < 2) {
            throw new IllegalArgumentException("Dataset must contain at least 2 samples");
        }
        Random rng = new Random(cfg.getSeed());

        // 1. Collect unique labels
        Map<String, Integer> labelMap = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        for (DataSample s : samples) {
            if (!labelMap.containsKey(s.label())) {
                labelMap.put(s.label(), labels.size());
                labels.add(s.label());
            }
        }
        int numClasses = labels.size();
        if (numClasses < 2) {
            throw new IllegalArgumentException("Dataset must contain at least 2 distinct classes, found: " + numClasses);
        }

        // 2. Train Pure Java BPE Tokenizer
        List<String> corpus = new ArrayList<>(samples.size());
        for (DataSample s : samples) {
            corpus.add(s.text());
        }
        BPETokenizer tokenizer = BPETokenizer.trainBPE(corpus, cfg.getTargetVocabSize());
        int vocabSize = tokenizer.getVocabSize();

        // 3. Prepare Encoded Dataset
        Map<Integer, List<EncodedSample>> classBuckets = new HashMap<>();
        for (DataSample s : samples) {
            int[] tokens = tokenizer.encode(s.text());
            if (tokens == null || tokens.length == 0) {
                tokens = new int[]{0};
            }
            int cID = labelMap.get(s.label());
            classBuckets.computeIfAbsent(cID, k -> new ArrayList<>()).add(new EncodedSample(tokens, cID));
        }

        List<EncodedSample> trainSet = new ArrayList<>();
        List<EncodedSample> valSet = new ArrayList<>();

        // Stratified split: ensure every class has representation in both train and validation
        for (List<EncodedSample> bucket : classBuckets.values()) {
            Collections.shuffle(bucket, rng);
            if (bucket.size() <= 2) {
                trainSet.addAll(bucket);
                valSet.addAll(bucket);
            } else {
                int valCnt = Math.max(1, (int) (bucket.size() * 0.15));
                valSet.addAll(bucket.subList(0, valCnt));
                trainSet.addAll(bucket.subList(valCnt, bucket.size()));
            }
        }

        // 4. Initialize Network Weights using Xavier / He uniform initialization
        Header header = Header.of(
                Header.CURRENT_FORMAT_VERSION,
                vocabSize,
                cfg.getEmbeddingDim(),
                cfg.getHiddenDim(),
                numClasses
        );

        int embDim = cfg.getEmbeddingDim();
        int hiddenDim = cfg.getHiddenDim();
        int posLen = InferenceModel.MAX_SEQUENCE_TOKENS * embDim;

        Weights weights = new Weights(
                new float[vocabSize * embDim],
                new float[posLen],
                new float[embDim * hiddenDim],
                new float[hiddenDim],
                new float[hiddenDim * numClasses],
                new float[numClasses]
        );

        float embScale = (float) Math.sqrt(1.0 / (double) embDim);
        for (int i = 0; i < weights.getEmbedding().length; i++) {
            weights.getEmbedding()[i] = (rng.nextFloat() * 2.0f - 1.0f) * embScale;
        }
        for (int i = 0; i < weights.getPositional().length; i++) {
            weights.getPositional()[i] = (rng.nextFloat() * 2.0f - 1.0f) * embScale;
        }
        float w1Scale = (float) Math.sqrt(2.0 / (double) embDim);
        for (int i = 0; i < weights.getW1().length; i++) {
            weights.getW1()[i] = (rng.nextFloat() * 2.0f - 1.0f) * w1Scale;
        }
        float w2Scale = (float) Math.sqrt(2.0 / (double) hiddenDim);
        for (int i = 0; i < weights.getW2().length; i++) {
            weights.getW2()[i] = (rng.nextFloat() * 2.0f - 1.0f) * w2Scale;
        }

        // Initialize AdamW Optimizers
        AdamWState optEmb = new AdamWState(weights.getEmbedding().length);
        AdamWState optPos = new AdamWState(weights.getPositional().length);
        AdamWState optW1 = new AdamWState(weights.getW1().length);
        AdamWState optB1 = new AdamWState(weights.getB1().length);
        AdamWState optW2 = new AdamWState(weights.getW2().length);
        AdamWState optB2 = new AdamWState(weights.getB2().length);

        // Gradients Accumulator Buffers
        float[] gradEmb = new float[weights.getEmbedding().length];
        float[] gradPos = new float[weights.getPositional().length];
        float[] gradW1 = new float[weights.getW1().length];
        float[] gradB1 = new float[weights.getB1().length];
        float[] gradW2 = new float[weights.getW2().length];
        float[] gradB2 = new float[weights.getB2().length];

        // Scratch buffers for forward-backward
        float[] pooled = new float[embDim];
        float[] z1 = new float[hiddenDim];
        float[] a1 = new float[hiddenDim];
        float[] z2 = new float[numClasses];
        float[] probs = new float[numClasses];

        float[] dZ2 = new float[numClasses];
        float[] dA1 = new float[hiddenDim];
        float[] dZ1 = new float[hiddenDim];
        float[] dMean = new float[embDim];

        float bestValLoss = Float.MAX_VALUE;
        float bestTrainLoss = Float.MAX_VALUE;
        int patienceCounter = 0;
        Weights bestWeights = null;

        int batchSize = cfg.getBatchSize();
        if (trainSet.size() <= 32) {
            batchSize = 4;
        } else if (batchSize > trainSet.size()) {
            batchSize = trainSet.size();
        }
        if (batchSize <= 0) {
            batchSize = 1;
        }

        // 5. Training Loop
        for (int epoch = 1; epoch <= cfg.getEpochs(); epoch++) {
            Collections.shuffle(trainSet, rng);

            Arrays.fill(gradEmb, 0.0f);
            Arrays.fill(gradPos, 0.0f);
            Arrays.fill(gradW1, 0.0f);
            Arrays.fill(gradB1, 0.0f);
            Arrays.fill(gradW2, 0.0f);
            Arrays.fill(gradB2, 0.0f);

            int accumCount = 0;

            for (int idx = 0; idx < trainSet.size(); idx++) {
                EncodedSample sample = trainSet.get(idx);
                int seqLen = Math.min(sample.tokens().length, InferenceModel.MAX_SEQUENCE_TOKENS);

                // Forward Pass with Positional Encoding
                Ops.meanPoolingWithPos(sample.tokens(), seqLen, weights.getEmbedding(), weights.getPositional(), embDim, pooled);
                Ops.matMulVecAdd(pooled, weights.getW1(), weights.getB1(), embDim, hiddenDim, z1);
                for (int i = 0; i < hiddenDim; i++) {
                    a1[i] = Ops.gelu(z1[i]);
                }
                Ops.matMulVecAdd(a1, weights.getW2(), weights.getB2(), hiddenDim, numClasses, z2);
                Ops.softmax(z2, numClasses, 1.0f, probs);

                // Backward Pass
                // 1. Loss gradient wrt z2: dZ2 = probs - y_onehot
                for (int c = 0; c < numClasses; c++) {
                    dZ2[c] = probs[c];
                }
                dZ2[sample.classID()] -= 1.0f;

                // 2. Gradients for Layer 2: gradW2 += a1^T * dZ2, gradB2 += dZ2
                for (int h = 0; h < hiddenDim; h++) {
                    float ah = a1[h];
                    int rowOffset = h * numClasses;
                    for (int c = 0; c < numClasses; c++) {
                        gradW2[rowOffset + c] += ah * dZ2[c];
                    }
                }
                for (int c = 0; c < numClasses; c++) {
                    gradB2[c] += dZ2[c];
                }

                // 3. Backprop through Layer 2 to a1: dA1 = dZ2 * W2^T
                for (int h = 0; h < hiddenDim; h++) {
                    float sum = 0.0f;
                    int rowOffset = h * numClasses;
                    for (int c = 0; c < numClasses; c++) {
                        sum += dZ2[c] * weights.getW2()[rowOffset + c];
                    }
                    dA1[h] = sum;
                }

                // 4. Backprop through GELU activation: dZ1 = dA1 * GELU'(z1)
                for (int h = 0; h < hiddenDim; h++) {
                    dZ1[h] = dA1[h] * Ops.geluDerivative(z1[h]);
                }

                // 5. Gradients for Layer 1: gradW1 += pooled^T * dZ1, gradB1 += dZ1
                for (int e = 0; e < embDim; e++) {
                    float pe = pooled[e];
                    int rowOffset = e * hiddenDim;
                    for (int h = 0; h < hiddenDim; h++) {
                        gradW1[rowOffset + h] += pe * dZ1[h];
                    }
                }
                for (int h = 0; h < hiddenDim; h++) {
                    gradB1[h] += dZ1[h];
                }

                // 6. Backprop through Layer 1 to pooled embedding: dMean = dZ1 * W1^T
                for (int e = 0; e < embDim; e++) {
                    float sum = 0.0f;
                    int rowOffset = e * hiddenDim;
                    for (int h = 0; h < hiddenDim; h++) {
                        sum += dZ1[h] * weights.getW1()[rowOffset + h];
                    }
                    dMean[e] = sum;
                }

                // 7. Backprop through Non-Linear Positional Mean Pooling
                float invLen = 1.0f / (float) seqLen;
                for (int pos = 0; pos < seqLen; pos++) {
                    int tok = sample.tokens()[pos];
                    int tokOffset = tok * embDim;
                    int posOffset = pos * embDim;
                    for (int e = 0; e < embDim; e++) {
                        float sumVal = weights.getEmbedding()[tokOffset + e] + weights.getPositional()[posOffset + e];
                        float g = dMean[e] * invLen * Ops.geluDerivative(sumVal);
                        gradEmb[tokOffset + e] += g;
                        if (posOffset + embDim <= gradPos.length) {
                            gradPos[posOffset + e] += g;
                        }
                    }
                }

                accumCount++;

                // Step AdamW on batch boundary or dataset end
                if (accumCount % batchSize == 0 || idx == trainSet.size() - 1) {
                    float scale = 1.0f / (float) accumCount;
                    scaleArray(gradEmb, scale);
                    scaleArray(gradPos, scale);
                    scaleArray(gradW1, scale);
                    scaleArray(gradB1, scale);
                    scaleArray(gradW2, scale);
                    scaleArray(gradB2, scale);

                    optEmb.step(weights.getEmbedding(), gradEmb, cfg.getLearningRate(), 0.0f, cfg.getBeta1(), cfg.getBeta2(), cfg.getEpsilon());
                    optPos.step(weights.getPositional(), gradPos, cfg.getLearningRate(), 0.0f, cfg.getBeta1(), cfg.getBeta2(), cfg.getEpsilon());
                    optW1.step(weights.getW1(), gradW1, cfg.getLearningRate(), cfg.getWeightDecay(), cfg.getBeta1(), cfg.getBeta2(), cfg.getEpsilon());
                    optB1.step(weights.getB1(), gradB1, cfg.getLearningRate(), 0.0f, cfg.getBeta1(), cfg.getBeta2(), cfg.getEpsilon());
                    optW2.step(weights.getW2(), gradW2, cfg.getLearningRate(), cfg.getWeightDecay(), cfg.getBeta1(), cfg.getBeta2(), cfg.getEpsilon());
                    optB2.step(weights.getB2(), gradB2, cfg.getLearningRate(), 0.0f, cfg.getBeta1(), cfg.getBeta2(), cfg.getEpsilon());

                    Arrays.fill(gradEmb, 0.0f);
                    Arrays.fill(gradPos, 0.0f);
                    Arrays.fill(gradW1, 0.0f);
                    Arrays.fill(gradB1, 0.0f);
                    Arrays.fill(gradW2, 0.0f);
                    Arrays.fill(gradB2, 0.0f);
                    accumCount = 0;
                }
            }

            // Validation Evaluation
            float trainLoss = 0.0f;
            int trainCorrect = 0;
            for (EncodedSample sample : trainSet) {
                int seqLen = Math.min(sample.tokens().length, InferenceModel.MAX_SEQUENCE_TOKENS);
                Ops.meanPoolingWithPos(sample.tokens(), seqLen, weights.getEmbedding(), weights.getPositional(), embDim, pooled);
                Ops.matMulVecAdd(pooled, weights.getW1(), weights.getB1(), embDim, hiddenDim, z1);
                for (int i = 0; i < hiddenDim; i++) {
                    a1[i] = Ops.gelu(z1[i]);
                }
                Ops.matMulVecAdd(a1, weights.getW2(), weights.getB2(), hiddenDim, numClasses, z2);
                Ops.softmax(z2, numClasses, 1.0f, probs);

                float p = Math.max(1e-7f, probs[sample.classID()]);
                trainLoss -= (float) Math.log(p);

                int pred = 0;
                float maxP = -1.0f;
                for (int c = 0; c < numClasses; c++) {
                    if (probs[c] > maxP) {
                        maxP = probs[c];
                        pred = c;
                    }
                }
                if (pred == sample.classID()) {
                    trainCorrect++;
                }
            }
            trainLoss /= (float) trainSet.size();
            float trainAcc = (float) trainCorrect / (float) trainSet.size();

            float valLoss = 0.0f;
            int valCorrect = 0;
            for (EncodedSample sample : valSet) {
                int seqLen = Math.min(sample.tokens().length, InferenceModel.MAX_SEQUENCE_TOKENS);
                Ops.meanPoolingWithPos(sample.tokens(), seqLen, weights.getEmbedding(), weights.getPositional(), embDim, pooled);
                Ops.matMulVecAdd(pooled, weights.getW1(), weights.getB1(), embDim, hiddenDim, z1);
                for (int i = 0; i < hiddenDim; i++) {
                    a1[i] = Ops.gelu(z1[i]);
                }
                Ops.matMulVecAdd(a1, weights.getW2(), weights.getB2(), hiddenDim, numClasses, z2);
                Ops.softmax(z2, numClasses, 1.0f, probs);

                float p = Math.max(1e-7f, probs[sample.classID()]);
                valLoss -= (float) Math.log(p);

                int pred = 0;
                float maxP = -1.0f;
                for (int c = 0; c < numClasses; c++) {
                    if (probs[c] > maxP) {
                        maxP = probs[c];
                        pred = c;
                    }
                }
                if (pred == sample.classID()) {
                    valCorrect++;
                }
            }
            valLoss /= (float) valSet.size();
            float valAcc = (float) valCorrect / (float) valSet.size();

            // Check Best Model and Early Stopping
            boolean isBetter = false;
            if (samples.size() < 50) {
                if (trainLoss < bestTrainLoss) {
                    bestTrainLoss = trainLoss;
                    isBetter = true;
                }
            } else {
                if (valLoss < bestValLoss) {
                    bestValLoss = valLoss;
                    isBetter = true;
                }
            }

            if (isBetter) {
                bestValLoss = valLoss;
                patienceCounter = 0;
                bestWeights = weights.copy();
            } else {
                patienceCounter++;
                if (patienceCounter >= cfg.getPatience() && epoch >= 30) {
                    System.out.printf("[Early Stopping] Triggered at epoch %d (Train Loss: %.4f, Val Loss: %.4f)%n", epoch, trainLoss, valLoss);
                    break;
                }
            }

            if (epoch % 10 == 0 || epoch == cfg.getEpochs()) {
                System.out.printf("Epoch %3d/%3d - Train Loss: %.4f (Acc: %.1f%%) | Val Loss: %.4f (Acc: %.1f%%)%n",
                        epoch, cfg.getEpochs(), trainLoss, trainAcc * 100.0f, valLoss, valAcc * 100.0f);
            }
        }

        if (bestWeights == null) {
            bestWeights = weights;
        }

        return new InferenceModel(header, labels, tokenizer.getVocab(), tokenizer.getMergeRules(), bestWeights);
    }

    private static void scaleArray(float[] arr, float scale) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] *= scale;
        }
    }
}
