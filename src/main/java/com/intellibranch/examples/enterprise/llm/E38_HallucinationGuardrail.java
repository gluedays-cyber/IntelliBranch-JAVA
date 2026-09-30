package com.intellibranch.examples.enterprise.llm;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E38] LLM Output Hallucination Guardrail
 * Domain: LLM & Generative AI
 * Inspects generated LLM answers against ground truth facts to suppress hallucinatory claims
 */
public class E38_HallucinationGuardrail extends BaseDemo {

    @Override
    public String getId() { return "E38"; }

    @Override
    public String getTitle() { return "LLM Output Hallucination Guardrail"; }

    @Override
    public String getCategory() { return "LLM & Generative AI"; }

    @Override
    public String getDescription() { return "Inspects generated LLM answers against ground truth facts to suppress hallucinatory claims"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "pass_verified", "flag_hallucination", "regenerate_prompt", "fallback", "cited_source", "grounded_fact", "consistent_num", "partially_true", "extrapolation", "vague_date", "unsupported_claim", "phantom_author", "false_stat", "grounded", "fact", "cited", "source", "consistent", "num", "answer", "unsupported", "claim", "phantom", "author", "false", "stat", "fabricated", "partially", "true", "vague", "date", "statement", "unparseable", "response", "format" };
        String[] classes = new String[]{ "pass_verified", "flag_hallucination", "regenerate_prompt", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("pass_verified", new String[]{"cited_source", "grounded_fact", "consistent_num"});
            anchorMap.put("regenerate_prompt", new String[]{"partially_true", "extrapolation", "vague_date"});
            anchorMap.put("flag_hallucination", new String[]{"unsupported_claim", "phantom_author", "false_stat"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "grounded fact cited source consistent num answer");
        evaluateAndPrint(bundle, "unsupported claim phantom author false stat fabricated");
        evaluateAndPrint(bundle, "partially true extrapolation vague date statement");
        evaluateAndPrint(bundle, "unparseable response format");
    }

    public static void main(String[] args) {
        new E38_HallucinationGuardrail().execute();
    }
}