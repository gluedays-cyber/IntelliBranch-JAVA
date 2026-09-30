package com.intellibranch.examples.enterprise.llm;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E37] Autonomous AI Agent Tool Call Dispatcher
 * Domain: LLM & Generative AI
 * Classifies natural language intent into Calculator, SQL Database, or Web Search tool invocation
 */
public class E37_AiAgentToolDispatcher extends BaseDemo {

    @Override
    public String getId() { return "E37"; }

    @Override
    public String getTitle() { return "Autonomous AI Agent Tool Call Dispatcher"; }

    @Override
    public String getCategory() { return "LLM & Generative AI"; }

    @Override
    public String getDescription() { return "Classifies natural language intent into Calculator, SQL Database, or Web Search tool invocation"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "tool_calculator", "tool_database", "tool_web_search", "fallback", "compute", "square_root", "multiply", "latest_news", "weather_today", "current_stock", "select_rows", "count_users", "query_db", "square", "root", "45", "by", "12", "select", "rows", "count", "users", "query", "db", "latest", "news", "weather", "today", "current", "stock", "idle", "agent", "pause" };
        String[] classes = new String[]{ "tool_calculator", "tool_database", "tool_web_search", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("tool_calculator", new String[]{"compute", "square_root", "multiply"});
            anchorMap.put("tool_web_search", new String[]{"latest_news", "weather_today", "current_stock"});
            anchorMap.put("tool_database", new String[]{"select_rows", "count_users", "query_db"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "compute square root multiply 45 by 12");
        evaluateAndPrint(bundle, "select rows count users query db");
        evaluateAndPrint(bundle, "latest news weather today current stock");
        evaluateAndPrint(bundle, "idle agent pause");
    }

    public static void main(String[] args) {
        new E37_AiAgentToolDispatcher().execute();
    }
}