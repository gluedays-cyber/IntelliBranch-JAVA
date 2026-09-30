package com.intellibranch;

import com.intellibranch.core.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class BinaryModelTest {
    public static void runTests() {
        testSerializationRoundTrip();
        testCorruptedMagicRejection();
        testChecksumTamperRejection();
        System.out.println("  ✓ BinaryModelTest passed");
    }

    private static InferenceModel createDummyModel() {
        Header header = Header.of(2, 4, 4, 8, 2);
        List<String> labels = List.of("Refund", "Delivery");
        List<String> vocab = List.of("[PAD]", "[UNK]", "refund", "delivery");
        List<MergeRule> rules = List.of(new MergeRule(2, 3, 3));
        Weights weights = new Weights(
                new float[4 * 4],
                new float[InferenceModel.MAX_SEQUENCE_TOKENS * 4],
                new float[4 * 8],
                new float[8],
                new float[8 * 2],
                new float[2]
        );
        for (int i = 0; i < weights.getEmbedding().length; i++) weights.getEmbedding()[i] = 0.1f * i;
        for (int i = 0; i < weights.getW1().length; i++) weights.getW1()[i] = 0.05f * i;
        for (int i = 0; i < weights.getW2().length; i++) weights.getW2()[i] = 0.02f * i;
        return new InferenceModel(header, labels, vocab, rules, weights);
    }

    private static void testSerializationRoundTrip() {
        InferenceModel original = createDummyModel();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BinaryModel.serialize(baos, original);
            byte[] bytes = baos.toByteArray();
            if (bytes.length == 0) {
                throw new AssertionError("Serialized bytes should not be empty");
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            InferenceModel loaded = BinaryModel.deserialize(bais);

            if (loaded.getHeader().vocabSize() != original.getHeader().vocabSize() ||
                    loaded.getHeader().numClasses() != original.getHeader().numClasses()) {
                throw new AssertionError("Header mismatch after deserialize");
            }
            if (!loaded.getLabels().equals(original.getLabels())) {
                throw new AssertionError("Labels mismatch after deserialize");
            }
            if (!loaded.getVocab().equals(original.getVocab())) {
                throw new AssertionError("Vocab mismatch after deserialize");
            }
            if (loaded.getWeights().getW1()[1] != original.getWeights().getW1()[1]) {
                throw new AssertionError("Weights mismatch after deserialize");
            }
        } catch (IOException e) {
            throw new AssertionError("Serialization round-trip failed: " + e.getMessage(), e);
        }
    }

    private static void testCorruptedMagicRejection() {
        InferenceModel original = createDummyModel();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BinaryModel.serialize(baos, original);
            byte[] bytes = baos.toByteArray();
            bytes[0] = 'X'; // Corrupt magic

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            try {
                BinaryModel.deserialize(bais);
                throw new AssertionError("Expected IOException for invalid magic");
            } catch (IOException expected) {
                // Expected
            }
        } catch (IOException e) {
            throw new AssertionError("Test failed: " + e.getMessage(), e);
        }
    }

    private static void testChecksumTamperRejection() {
        InferenceModel original = createDummyModel();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BinaryModel.serialize(baos, original);
            byte[] bytes = baos.toByteArray();
            // Tamper tensor payload
            bytes[50] = (byte) (bytes[50] ^ 0xFF);

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            try {
                BinaryModel.deserialize(bais);
                throw new AssertionError("Expected IOException for tampered payload checksum");
            } catch (IOException expected) {
                // Expected
            }
        } catch (IOException e) {
            throw new AssertionError("Test failed: " + e.getMessage(), e);
        }
    }
}
