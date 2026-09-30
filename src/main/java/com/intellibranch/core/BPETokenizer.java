package com.intellibranch.core;

import java.util.*;

/**
 * Byte-Pair Encoding (BPE) subword tokenizer with offline training and serialization support.
 */
public class BPETokenizer {
    public static final String PAD_TOKEN = "[PAD]";
    public static final String UNK_TOKEN = "[UNK]";

    private final List<String> vocab;
    private final Map<String, Integer> vocabMap;
    private final List<MergeRule> mergeRules;
    private final Map<Long, Integer> ruleLookup;

    public BPETokenizer(List<String> vocab, List<MergeRule> mergeRules) {
        this.vocab = new ArrayList<>(vocab);
        this.vocabMap = new HashMap<>(vocab.size());
        for (int i = 0; i < vocab.size(); i++) {
            this.vocabMap.put(vocab.get(i), i);
        }

        this.mergeRules = new ArrayList<>(mergeRules);
        this.ruleLookup = new HashMap<>(mergeRules.size());
        for (MergeRule r : mergeRules) {
            long key = (((long) r.token1()) << 32) | (r.token2() & 0xFFFFFFFFL);
            this.ruleLookup.put(key, r.target());
        }
    }

    public List<String> getVocab() {
        return Collections.unmodifiableList(vocab);
    }

    public Map<String, Integer> getVocabMap() {
        return Collections.unmodifiableMap(vocabMap);
    }

    public List<MergeRule> getMergeRules() {
        return Collections.unmodifiableList(mergeRules);
    }

    public int getVocabSize() {
        return vocab.size();
    }

    /**
     * Converts input text into subword token IDs using learned merge rules.
     */
    public int[] encode(String text) {
        if (text == null) {
            return new int[0];
        }
        text = text.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return new int[0];
        }

        Integer unkId = vocabMap.get(UNK_TOKEN);
        int unk = (unkId != null) ? unkId : 1;

        // 1. Initial character split using code points
        int[] codePoints = text.codePoints().toArray();
        int[] tokens = new int[codePoints.length];
        for (int i = 0; i < codePoints.length; i++) {
            String ch = new String(codePoints, i, 1);
            Integer id = vocabMap.get(ch);
            tokens[i] = (id != null) ? id : unk;
        }

        if (tokens.length <= 1) {
            return tokens;
        }

        // 2. Iteratively apply merge rules until no pairs can be merged
        while (true) {
            boolean merged = false;
            int[] next = new int[tokens.length];
            int nextLen = 0;
            int i = 0;
            while (i < tokens.length) {
                if (i < tokens.length - 1) {
                    long key = (((long) tokens[i]) << 32) | (tokens[i + 1] & 0xFFFFFFFFL);
                    Integer target = ruleLookup.get(key);
                    if (target != null) {
                        next[nextLen++] = target;
                        i += 2;
                        merged = true;
                        continue;
                    }
                }
                next[nextLen++] = tokens[i];
                i++;
            }

            tokens = Arrays.copyOf(next, nextLen);
            if (!merged) {
                break;
            }
        }

        return tokens;
    }

    /**
     * Transforms a sequence of token IDs back into readable text.
     */
    public String decode(int[] tokens) {
        if (tokens == null || tokens.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int tok : tokens) {
            if (tok >= 0 && tok < vocab.size()) {
                String str = vocab.get(tok);
                if (!PAD_TOKEN.equals(str) && !UNK_TOKEN.equals(str)) {
                    sb.append(str);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Trains a BPE vocabulary and merge rules from a raw text corpus.
     */
    public static BPETokenizer trainBPE(List<String> corpus, int targetVocabSize) {
        if (targetVocabSize < 10) {
            targetVocabSize = 10;
        }

        Map<String, Integer> vocabMap = new LinkedHashMap<>();
        List<String> vocab = new ArrayList<>();

        // Add special tokens
        for (String st : new String[]{PAD_TOKEN, UNK_TOKEN}) {
            vocabMap.put(st, vocab.size());
            vocab.add(st);
        }

        // 1. Collect unique characters
        for (String text : corpus) {
            if (text == null) continue;
            text = text.trim().toLowerCase(Locale.ROOT);
            for (int cp : text.codePoints().toArray()) {
                String ch = new String(Character.toChars(cp));
                if (!vocabMap.containsKey(ch)) {
                    vocabMap.put(ch, vocab.size());
                    vocab.add(ch);
                }
            }
        }

        // 2. Tokenize corpus into character token sequences
        List<int[]> tokenizedCorpus = new ArrayList<>();
        for (String text : corpus) {
            if (text == null) continue;
            text = text.trim().toLowerCase(Locale.ROOT);
            if (text.isEmpty()) continue;
            int[] cps = text.codePoints().toArray();
            int[] seq = new int[cps.length];
            for (int i = 0; i < cps.length; i++) {
                String ch = new String(Character.toChars(cps[i]));
                seq[i] = vocabMap.get(ch);
            }
            tokenizedCorpus.add(seq);
        }

        List<MergeRule> mergeRules = new ArrayList<>();
        Map<Long, Integer> ruleLookup = new HashMap<>();

        // 3. Iteratively merge most frequent adjacent pairs
        while (vocab.size() < targetVocabSize) {
            Map<Long, Integer> pairCounts = new HashMap<>();
            for (int[] seq : tokenizedCorpus) {
                for (int i = 0; i < seq.length - 1; i++) {
                    long key = (((long) seq[i]) << 32) | (seq[i + 1] & 0xFFFFFFFFL);
                    pairCounts.put(key, pairCounts.getOrDefault(key, 0) + 1);
                }
            }

            if (pairCounts.isEmpty()) {
                break;
            }

            long bestKey = 0;
            int maxFreq = -1;
            for (Map.Entry<Long, Integer> entry : pairCounts.entrySet()) {
                if (entry.getValue() > maxFreq) {
                    maxFreq = entry.getValue();
                    bestKey = entry.getKey();
                }
            }

            if (maxFreq < 2 && vocab.size() >= targetVocabSize / 2) {
                break;
            }

            int t1 = (int) (bestKey >>> 32);
            int t2 = (int) (bestKey & 0xFFFFFFFFL);

            String str1 = vocab.get(t1);
            String str2 = vocab.get(t2);
            String mergedStr = str1 + str2;

            int newID = vocab.size();
            vocabMap.put(mergedStr, newID);
            vocab.add(mergedStr);

            MergeRule rule = new MergeRule(t1, t2, newID);
            mergeRules.add(rule);
            ruleLookup.put(bestKey, newID);

            // Apply merge in-place across tokenized corpus
            for (int sIdx = 0; sIdx < tokenizedCorpus.size(); sIdx++) {
                int[] seq = tokenizedCorpus.get(sIdx);
                int[] newSeq = new int[seq.length];
                int nLen = 0;
                int i = 0;
                while (i < seq.length) {
                    if (i < seq.length - 1 && seq[i] == t1 && seq[i + 1] == t2) {
                        newSeq[nLen++] = newID;
                        i += 2;
                    } else {
                        newSeq[nLen++] = seq[i];
                        i++;
                    }
                }
                tokenizedCorpus.set(sIdx, Arrays.copyOf(newSeq, nLen));
            }
        }

        return new BPETokenizer(vocab, mergeRules);
    }
}
