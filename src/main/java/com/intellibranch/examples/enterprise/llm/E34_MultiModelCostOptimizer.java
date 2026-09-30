package com.intellibranch.examples.enterprise.llm;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E34] Multi-LLM Cost & Tier Router (GPT-4o vs Claude vs Haiku)
 * Domain: LLM & Generative AI
 * Routes incoming prompt complexity to low-cost Flash/Haiku models or high-end Frontier models
 */
public class E34_MultiModelCostOptimizer extends BaseDemo {

    @Override
    public String getId() { return "E34"; }

    @Override
    public String getTitle() { return "Multi-LLM Cost & Tier Router (GPT-4o vs Claude vs Haiku)"; }

    @Override
    public String getCategory() { return "LLM & Generative AI"; }

    @Override
    public String getDescription() { return "Routes incoming prompt complexity to low-cost Flash/Haiku models or high-end Frontier models"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "frontier_gpt4o", "mid_claude_sonnet", "fast_gemini_flash", "fallback", "quantum_physics", "complex_math", "legal_contract", "summarize_line", "sentiment_label", "translate", "code_refactor", "email_draft", "analysis", "solve", "complex", "math", "and", "quantum", "physics", "equations", "code", "refactor", "email", "draft", "for", "marketing", "summarize", "line", "sentiment", "label", "blank", "prompt", "input" };
        String[] classes = new String[]{ "frontier_gpt4o", "mid_claude_sonnet", "fast_gemini_flash", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("frontier_gpt4o", new String[]{"quantum_physics", "complex_math", "legal_contract"});
            anchorMap.put("fast_gemini_flash", new String[]{"summarize_line", "sentiment_label", "translate"});
            anchorMap.put("mid_claude_sonnet", new String[]{"code_refactor", "email_draft", "analysis"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "solve complex math and quantum physics equations");
        evaluateAndPrint(bundle, "code refactor email draft for marketing");
        evaluateAndPrint(bundle, "summarize line translate sentiment label");
        evaluateAndPrint(bundle, "blank prompt input");
    }

    public static void main(String[] args) {
        new E34_MultiModelCostOptimizer().execute();
    }
}