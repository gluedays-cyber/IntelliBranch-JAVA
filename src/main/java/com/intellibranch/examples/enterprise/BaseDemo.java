package com.intellibranch.examples.enterprise;

import com.intellibranch.core.BPETokenizer;
import com.intellibranch.core.Header;
import com.intellibranch.core.InferenceModel;
import com.intellibranch.core.MergeRule;
import com.intellibranch.core.Ops;
import com.intellibranch.core.Weights;
import com.intellibranch.neurogate.GateTrace;
import com.intellibranch.neurogate.NeuroGate;
import com.intellibranch.routing.DispatchPolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base utility class providing ultra-fast in-memory synthesis of NeuroGate and Router instances
 * for enterprise domain scenarios.
 */
public abstract class BaseDemo implements EnterpriseDemo {

    protected record GateBundle(NeuroGate gate, InferenceModel model, BPETokenizer tokenizer) {}

    protected GateBundle buildDomainGate(String[] vocabWords, String[] classes, Map<String, String[]> anchorMap) {
        int embedDim = 32;
        int hiddenDim = 64;
        int seqLen = 32;
        int numClasses = classes.length;

        // 1. Build BPE Tokenizer using corpus of domain words and anchor keywords
        List<String> corpus = new ArrayList<>();
        for (String w : vocabWords) {
            corpus.add(w.replace('_', ' ').toLowerCase());
        }
        for (String[] kws : anchorMap.values()) {
            for (String kw : kws) {
                corpus.add(kw.replace('_', ' ').toLowerCase());
            }
        }

        int targetVocab = Math.max(256, vocabWords.length * 2);
        BPETokenizer tokenizer = BPETokenizer.trainBPE(corpus, targetVocab);
        int vocabSize = tokenizer.getVocabSize();

        // 2. Synthesize Deterministic Normalized Weights
        Header header = Header.of(2, vocabSize, embedDim, hiddenDim, numClasses);

        float[] embedding = new float[vocabSize * embedDim];
        float[] positional = new float[seqLen * embedDim];
        float[] w1 = new float[embedDim * hiddenDim];
        float[] b1 = new float[hiddenDim];
        float[] w2 = new float[hiddenDim * numClasses];
        float[] b2 = new float[numClasses];

        // Class index map
        Map<String, Integer> classIndex = new HashMap<>();
        for (int i = 0; i < classes.length; i++) {
            classIndex.put(classes[i], i);
        }

        // Biased embeddings for anchor tokens
        for (Map.Entry<String, String[]> entry : anchorMap.entrySet()) {
            Integer cIdx = classIndex.get(entry.getKey());
            if (cIdx == null) continue;

            for (String kw : entry.getValue()) {
                int[] tokenIds = tokenizer.encode(kw.replace('_', ' '));
                for (int tid : tokenIds) {
                    if (tid >= 0 && tid < vocabSize) {
                        int offset = tid * embedDim;
                        for (int d = 0; d < embedDim; d++) {
                            if ((d % numClasses) == cIdx) {
                                embedding[offset + d] = 3.5f;
                            }
                        }
                    }
                }
            }
        }

        // Projection weights W1 & W2: linear projection toward matching class dimension
        for (int i = 0; i < embedDim; i++) {
            for (int h = 0; h < hiddenDim; h++) {
                if ((i % numClasses) == (h % numClasses)) {
                    w1[i * hiddenDim + h] = 1.0f;
                }
            }
        }
        for (int h = 0; h < hiddenDim; h++) {
            for (int c = 0; c < numClasses; c++) {
                if ((h % numClasses) == c) {
                    w2[h * numClasses + c] = 2.0f;
                } else {
                    w2[h * numClasses + c] = -0.5f;
                }
            }
        }

        Weights weights = new Weights(embedding, positional, w1, b1, w2, b2);
        InferenceModel infModel = new InferenceModel(header, List.of(classes), tokenizer.getVocab(), tokenizer.getMergeRules(), weights);

        // 3. Construct 3-Head NeuroGate
        NeuroGate gate = new NeuroGate(infModel);
        // Policy: low threshold 0.20, high 0.40, entropy cutoff 3.8
        gate.setPolicy(new DispatchPolicy(0.20, 0.40, 0.05, 0.45, 3.8, 0.0));

        // Bind routes and anchor rules
        for (String cls : classes) {
            String[] keywords = anchorMap.get(cls);
            if (keywords != null && keywords.length > 0) {
                // Register keywords with space replacement so they match raw query text
                List<String> cleanKeywords = new ArrayList<>();
                for (String kw : keywords) {
                    cleanKeywords.add(kw.replace('_', ' '));
                    cleanKeywords.add(kw);
                }
                gate.bind(cls, (ctx, payload) -> {}).withAnchor(3.0f, cleanKeywords.toArray(new String[0]));
            } else {
                gate.bind(cls, (ctx, payload) -> {});
            }
        }
        gate.fallback((ctx, payload) -> {});

        return new GateBundle(gate, infModel, tokenizer);
    }

    protected void printHeader() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("[%s] %s | Domain: %s%n", getId(), getTitle(), getCategory());
        System.out.println("Desc: " + getDescription());
        System.out.println("--------------------------------------------------------------------------------");
    }

    protected void evaluateAndPrint(GateBundle bundle, String query) {
        GateTrace trace = bundle.gate().inspect(query);
        String outcome = trace.isFallback() ? "FALLBACK" : trace.predictedLabel();
        System.out.printf("  [Query] %-60s -> Branch: %-20s (Conf: %.3f, OOD: %-5b, Latency: %d us)%n",
                "\"" + query + "\"", outcome, trace.confidence(), trace.isOOD(), trace.latencyMicros());
    }
}
