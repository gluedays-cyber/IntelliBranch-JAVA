package com.intellibranch.cli;

import com.intellibranch.core.BinaryModel;
import com.intellibranch.core.InferenceModel;
import com.intellibranch.training.DataSample;
import com.intellibranch.training.TrainConfig;
import com.intellibranch.training.Trainer;

import java.nio.file.Path;
import java.util.List;

/**
 * Command-line interface for offline BPE + AdamW neural model training and binary export.
 */
public class TrainerCli {
    public static void main(String[] args) {
        String dataPath = "data/sample_dataset.csv";
        String outPath = "weights/model.bin";
        int epochs = 150;
        int targetVocab = 150;
        float lr = 0.005f;

        for (int i = 0; i < args.length; i++) {
            if ("--data".equals(args[i]) && i + 1 < args.length) {
                dataPath = args[++i];
            } else if ("--out".equals(args[i]) && i + 1 < args.length) {
                outPath = args[++i];
            } else if ("--epochs".equals(args[i]) && i + 1 < args.length) {
                epochs = Integer.parseInt(args[++i]);
            } else if ("--vocab".equals(args[i]) && i + 1 < args.length) {
                targetVocab = Integer.parseInt(args[++i]);
            } else if ("--lr".equals(args[i]) && i + 1 < args.length) {
                lr = Float.parseFloat(args[++i]);
            }
        }

        try {
            System.out.println("Loading dataset from: " + dataPath);
            List<DataSample> samples = Trainer.loadCSVDataset(Path.of(dataPath));
            System.out.println("Loaded " + samples.size() + " training samples");

            TrainConfig cfg = TrainConfig.defaultConfig()
                    .setEpochs(epochs)
                    .setTargetVocabSize(targetVocab)
                    .setLearningRate(lr)
                    .setBatchSize(16)
                    .setPatience(10);

            System.out.println("Starting offline BPE + AdamW training pipeline...");
            InferenceModel model = Trainer.trainModel(samples, cfg);

            Path out = Path.of(outPath);
            System.out.println("Serializing trained model to Little-Endian binary: " + out);
            BinaryModel.save(out, model);

            System.out.println("Training and binary export completed successfully.");
            System.out.printf("Model saved at: %s (Vocab: %d, Classes: %d)%n",
                    out, model.getHeader().vocabSize(), model.getHeader().numClasses());
        } catch (Exception e) {
            System.err.println("Fatal training error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
