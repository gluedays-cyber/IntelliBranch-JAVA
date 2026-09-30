package com.intellibranch.examples.enterprise.llm;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E39] Dynamic Token Budget & Prompt Compression Router
 * Domain: LLM & Generative AI
 * Routes verbose prompts to semantic condensation pipelines when context limits approach
 */
public class E39_TokenBudgetThrottler extends BaseDemo {

    @Override
    public String getId() { return "E39"; }

    @Override
    public String getTitle() { return "Dynamic Token Budget & Prompt Compression Router"; }

    @Override
    public String getCategory() { return "LLM & Generative AI"; }

    @Override
    public String getDescription() { return "Routes verbose prompts to semantic condensation pipelines when context limits approach"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "pass_raw", "compress_context", "truncate_history", "fallback", "short_prompt", "within_budget", "concise", "exceeded_limit", "overflow", "emergency_prune", "massive_chat", "80pct_budget", "redundant_text", "short", "prompt", "within", "budget", "query", "massive", "chat", "redundant", "text", "80pct", "warning", "exceeded", "limit", "emergency", "prune", "history", "blank", "context", "state" };
        String[] classes = new String[]{ "pass_raw", "compress_context", "truncate_history", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("pass_raw", new String[]{"short_prompt", "within_budget", "concise"});
            anchorMap.put("truncate_history", new String[]{"exceeded_limit", "overflow", "emergency_prune"});
            anchorMap.put("compress_context", new String[]{"massive_chat", "80pct_budget", "redundant_text"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "short prompt concise within budget query");
        evaluateAndPrint(bundle, "massive chat redundant text 80pct budget warning");
        evaluateAndPrint(bundle, "exceeded limit overflow emergency prune chat history");
        evaluateAndPrint(bundle, "blank context state");
    }

    public static void main(String[] args) {
        new E39_TokenBudgetThrottler().execute();
    }
}