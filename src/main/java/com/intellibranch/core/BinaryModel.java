package com.intellibranch.core;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * High-performance, Little-Endian binary model serializer and parser with SHA-256 integrity validation.
 */
public final class BinaryModel {

    private BinaryModel() {
    }

    private static MessageDigest createSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest unavailable", e);
        }
    }

    /**
     * Serializes an InferenceModel to a file with SHA-256 checksum validation.
     */
    public static void save(Path filePath, InferenceModel model) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream fos = Files.newOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            serialize(bos, model);
        }
    }

    /**
     * Deserializes an InferenceModel from a file with SHA-256 checksum validation.
     */
    public static InferenceModel load(Path filePath) throws IOException {
        try (InputStream fis = Files.newInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            return deserialize(bis);
        }
    }

    /**
     * Serializes the model structure into Little-Endian bytes and appends a 32-byte SHA-256 checksum.
     */
    public static void serialize(OutputStream outputStream, InferenceModel model) throws IOException {
        MessageDigest digest = createSha256();
        DigestOutputStream dos = new DigestOutputStream(outputStream, digest);
        DataOutputStream out = new DataOutputStream(dos);

        Header header = model.getHeader();
        Weights weights = model.getWeights();

        // 1. Header Block
        dos.write(Header.MAGIC_BYTES);
        writeUInt32LE(out, header.version());
        writeUInt32LE(out, header.vocabSize());
        writeUInt32LE(out, header.embeddingDim());
        writeUInt32LE(out, header.hiddenDim());
        writeUInt32LE(out, header.numClasses());

        // 2. Labels Block
        List<String> labels = model.getLabels();
        writeUInt32LE(out, labels.size());
        for (String lbl : labels) {
            byte[] bytes = lbl.getBytes(StandardCharsets.UTF_8);
            writeUInt32LE(out, bytes.length);
            dos.write(bytes);
        }

        // 3. Vocabulary Block
        List<String> vocab = model.getVocab();
        writeUInt32LE(out, vocab.size());
        for (String tok : vocab) {
            byte[] bytes = tok.getBytes(StandardCharsets.UTF_8);
            writeUInt32LE(out, bytes.length);
            dos.write(bytes);
        }

        // 4. Merge Rules Block
        List<MergeRule> rules = model.getMergeRules();
        writeUInt32LE(out, rules.size());
        for (MergeRule rule : rules) {
            writeUInt32LE(out, rule.token1());
            writeUInt32LE(out, rule.token2());
            writeUInt32LE(out, rule.target());
        }

        // 5. Tensor Blocks (IEEE 754 float32 Little-Endian)
        writeFloatArrayLE(dos, weights.getEmbedding());

        if (header.version() >= 2) {
            int posLen = InferenceModel.MAX_SEQUENCE_TOKENS * header.embeddingDim();
            float[] posWeights = weights.getPositional();
            if (posWeights == null || posWeights.length < posLen) {
                float[] padded = new float[posLen];
                if (posWeights != null) {
                    System.arraycopy(posWeights, 0, padded, 0, Math.min(posWeights.length, posLen));
                }
                posWeights = padded;
            }
            writeFloatArrayLE(dos, posWeights, posLen);
        }

        writeFloatArrayLE(dos, weights.getW1());
        writeFloatArrayLE(dos, weights.getB1());
        writeFloatArrayLE(dos, weights.getW2());
        writeFloatArrayLE(dos, weights.getB2());

        dos.flush();

        // 6. Write SHA-256 Checksum
        byte[] computedChecksum = digest.digest();
        outputStream.write(computedChecksum);
        outputStream.flush();
    }

    /**
     * Parses Little-Endian bytes, validates header & SHA-256 checksum, and instantiates an InferenceModel.
     */
    public static InferenceModel deserialize(InputStream inputStream) throws IOException {
        MessageDigest digest = createSha256();
        DigestInputStream dis = new DigestInputStream(inputStream, digest);
        DataInputStream in = new DataInputStream(dis);

        // 1. Read Header Block
        byte[] magic = new byte[4];
        in.readFully(magic);
        if (!Arrays.equals(magic, Header.MAGIC_BYTES)) {
            throw new IOException("Invalid binary format: missing IBRN magic header");
        }

        int version = (int) readUInt32LE(in);
        if (version != 1 && version != 2) {
            throw new IOException("Unsupported model format version: " + version + " (expected 1 or 2)");
        }

        int vocabSize = (int) readUInt32LE(in);
        int embeddingDim = (int) readUInt32LE(in);
        int hiddenDim = (int) readUInt32LE(in);
        int numClasses = (int) readUInt32LE(in);
        Header header = new Header(magic, version, vocabSize, embeddingDim, hiddenDim, numClasses);

        // 2. Read Labels Block
        int numLabels = (int) readUInt32LE(in);
        if (numLabels != numClasses) {
            throw new IOException("Number of labels (" + numLabels + ") does not match numClasses (" + numClasses + ")");
        }
        List<String> labels = new ArrayList<>(numLabels);
        for (int i = 0; i < numLabels; i++) {
            int strLen = (int) readUInt32LE(in);
            byte[] buf = new byte[strLen];
            in.readFully(buf);
            labels.add(new String(buf, StandardCharsets.UTF_8));
        }

        // 3. Read Vocabulary Block
        int vocabCount = (int) readUInt32LE(in);
        if (vocabCount != vocabSize) {
            throw new IOException("Vocab count (" + vocabCount + ") does not match vocabSize (" + vocabSize + ")");
        }
        List<String> vocab = new ArrayList<>(vocabCount);
        for (int i = 0; i < vocabCount; i++) {
            int strLen = (int) readUInt32LE(in);
            byte[] buf = new byte[strLen];
            in.readFully(buf);
            vocab.add(new String(buf, StandardCharsets.UTF_8));
        }

        // 4. Read Merge Rules Block
        int numRules = (int) readUInt32LE(in);
        List<MergeRule> rules = new ArrayList<>(numRules);
        for (int i = 0; i < numRules; i++) {
            int t1 = (int) readUInt32LE(in);
            int t2 = (int) readUInt32LE(in);
            int target = (int) readUInt32LE(in);
            rules.add(new MergeRule(t1, t2, target));
        }

        // 5. Read Tensor Blocks
        int posLen = InferenceModel.MAX_SEQUENCE_TOKENS * embeddingDim;
        float[] embedding = readFloatArrayLE(in, vocabSize * embeddingDim);
        float[] positional = (version >= 2) ? readFloatArrayLE(in, posLen) : new float[posLen];
        float[] w1 = readFloatArrayLE(in, embeddingDim * hiddenDim);
        float[] b1 = readFloatArrayLE(in, hiddenDim);
        float[] w2 = readFloatArrayLE(in, hiddenDim * numClasses);
        float[] b2 = readFloatArrayLE(in, numClasses);

        Weights weights = new Weights(embedding, positional, w1, b1, w2, b2);

        // 6. Verify SHA-256 Checksum (Read raw from base inputStream, not via DigestInputStream)
        byte[] storedChecksum = new byte[32];
        int readBytes = 0;
        while (readBytes < 32) {
            int r = inputStream.read(storedChecksum, readBytes, 32 - readBytes);
            if (r < 0) {
                throw new EOFException("Unexpected EOF while reading model SHA-256 checksum");
            }
            readBytes += r;
        }

        byte[] computedChecksum = digest.digest();
        if (!MessageDigest.isEqual(storedChecksum, computedChecksum)) {
            throw new IOException("Checksum verification failed: model file corrupted");
        }

        return new InferenceModel(header, labels, vocab, rules, weights);
    }

    private static void writeUInt32LE(DataOutputStream out, long value) throws IOException {
        out.writeByte((int) (value & 0xFF));
        out.writeByte((int) ((value >>> 8) & 0xFF));
        out.writeByte((int) ((value >>> 16) & 0xFF));
        out.writeByte((int) ((value >>> 24) & 0xFF));
    }

    private static long readUInt32LE(DataInputStream in) throws IOException {
        int b1 = in.readUnsignedByte();
        int b2 = in.readUnsignedByte();
        int b3 = in.readUnsignedByte();
        int b4 = in.readUnsignedByte();
        return (((long) b4 << 24) | ((long) b3 << 16) | ((long) b2 << 8) | (long) b1) & 0xFFFFFFFFL;
    }

    private static void writeFloatArrayLE(OutputStream out, float[] array) throws IOException {
        if (array == null) return;
        writeFloatArrayLE(out, array, array.length);
    }

    private static void writeFloatArrayLE(OutputStream out, float[] array, int length) throws IOException {
        if (array == null || length == 0) return;
        byte[] bytes = new byte[length * 4];
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < length; i++) {
            bb.putFloat(array[i]);
        }
        out.write(bytes);
    }

    private static float[] readFloatArrayLE(DataInputStream in, int length) throws IOException {
        byte[] bytes = new byte[length * 4];
        in.readFully(bytes);
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = bb.getFloat();
        }
        return array;
    }
}
