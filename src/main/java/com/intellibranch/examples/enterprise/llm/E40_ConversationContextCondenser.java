package com.intellibranch.examples.enterprise.llm;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E40] Multi-Turn Chat Context Switch Detector
 * Domain: LLM & Generative AI
 * Detects when user topic switches abruptly to clear previous context buffers
 */
public class E40_ConversationContextCondenser extends BaseDemo {

    @Override
    public String getId() { return "E40"; }

    @Override
    public String getTitle() { return "Multi-Turn Chat Context Switch Detector"; }

    @Override
    public String getCategory() { return "LLM & Generative AI"; }

    @Override
    public String getDescription() { return "Detects when user topic switches abruptly to clear previous context buffers"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "continue_context", "switch_topic", "branch_subconversation", "fallback", "aside", "by_the_way", "tangent", "changing_subject", "new_topic", "unrelated_question", "furthermore", "and_then", "elaborate_more", "elaborate", "more", "and", "then", "explain", "changing", "subject", "unrelated", "question", "new", "topic", "by", "the", "way", "thought", "single", "word", "ok" };
        String[] classes = new String[]{ "continue_context", "switch_topic", "branch_subconversation", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("branch_subconversation", new String[]{"aside", "by_the_way", "tangent"});
            anchorMap.put("switch_topic", new String[]{"changing_subject", "new_topic", "unrelated_question"});
            anchorMap.put("continue_context", new String[]{"furthermore", "and_then", "elaborate_more"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "furthermore elaborate more and then explain");
        evaluateAndPrint(bundle, "changing subject unrelated question new topic");
        evaluateAndPrint(bundle, "aside by the way tangent thought");
        evaluateAndPrint(bundle, "single word ok");
    }

    public static void main(String[] args) {
        new E40_ConversationContextCondenser().execute();
    }
}