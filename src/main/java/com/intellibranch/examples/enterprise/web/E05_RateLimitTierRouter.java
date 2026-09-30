package com.intellibranch.examples.enterprise.web;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E05] Adaptive Rate-Limit & SLA Tier Router
 * Domain: Web & API Services
 * Assigns inbound API requests to Bronze, Silver, or Gold throttling buckets
 */
public class E05_RateLimitTierRouter extends BaseDemo {

    @Override
    public String getId() { return "E05"; }

    @Override
    public String getTitle() { return "Adaptive Rate-Limit & SLA Tier Router"; }

    @Override
    public String getCategory() { return "Web & API Services"; }

    @Override
    public String getDescription() { return "Assigns inbound API requests to Bronze, Silver, or Gold throttling buckets"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "gold_unlimited", "silver_throttled", "bronze_strict", "fallback", "free", "demo", "trial", "enterprise", "vip", "contract", "standard", "pro", "business", "SLA", "request", "client", "tier", "user", "anonymous", "guest", "visitor" };
        String[] classes = new String[]{ "gold_unlimited", "silver_throttled", "bronze_strict", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("bronze_strict", new String[]{"free", "demo", "trial"});
            anchorMap.put("gold_unlimited", new String[]{"enterprise", "vip", "contract"});
            anchorMap.put("silver_throttled", new String[]{"standard", "pro", "business"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "enterprise vip SLA request");
        evaluateAndPrint(bundle, "pro business standard client");
        evaluateAndPrint(bundle, "free trial demo tier user");
        evaluateAndPrint(bundle, "anonymous guest visitor");
    }

    public static void main(String[] args) {
        new E05_RateLimitTierRouter().execute();
    }
}