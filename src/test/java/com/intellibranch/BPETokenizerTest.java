package com.intellibranch;

import com.intellibranch.core.BPETokenizer;

import java.util.List;

public class BPETokenizerTest {
    public static void runTests() {
        testBpeTrainingAndEncoding();
        System.out.println("  ✓ BPETokenizerTest passed");
    }

    private static void testBpeTrainingAndEncoding() {
        List<String> corpus = List.of(
                "low lower lowest",
                "newer widest",
                "please refund money",
                "track my package shipment"
        );

        BPETokenizer tokenizer = BPETokenizer.trainBPE(corpus, 50);
        if (tokenizer.getVocabSize() <= 10) {
            throw new AssertionError("Vocab size too small: " + tokenizer.getVocabSize());
        }

        int[] tokens = tokenizer.encode("please refund package");
        if (tokens.length == 0) {
            throw new AssertionError("Token sequence should not be empty");
        }

        String decoded = tokenizer.decode(tokens);
        if (!decoded.contains("please") || !decoded.contains("refund")) {
            throw new AssertionError("Decoded text should preserve words, got: " + decoded);
        }
    }
}
