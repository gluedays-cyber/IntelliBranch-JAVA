package com.intellibranch.core;

import java.util.Arrays;

/**
 * Header contains the structural hyperparameters of the embedded neural model.
 */
public record Header(
        byte[] magic,
        int version,
        int vocabSize,
        int embeddingDim,
        int hiddenDim,
        int numClasses
) {
    public static final byte[] MAGIC_BYTES = new byte[]{'I', 'B', 'R', 'N'};
    public static final int CURRENT_FORMAT_VERSION = 2;

    public Header {
        if (magic == null || magic.length != 4) {
            magic = Arrays.copyOf(MAGIC_BYTES, 4);
        }
    }

    public static Header of(int version, int vocabSize, int embeddingDim, int hiddenDim, int numClasses) {
        return new Header(Arrays.copyOf(MAGIC_BYTES, 4), version, vocabSize, embeddingDim, hiddenDim, numClasses);
    }

    public boolean isValidMagic() {
        return magic != null && Arrays.equals(magic, MAGIC_BYTES);
    }
}
