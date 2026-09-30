package com.intellibranch.examples.enterprise.web;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E01] REST API Intent-Based Endpoint Dispatcher
 * Domain: Web & API Services
 * Classifies raw REST URI/intent paths to microservice handlers without regex bottlenecks
 */
public class E01_RestApiIntentRouter extends BaseDemo {

    @Override
    public String getId() { return "E01"; }

    @Override
    public String getTitle() { return "REST API Intent-Based Endpoint Dispatcher"; }

    @Override
    public String getCategory() { return "Web & API Services"; }

    @Override
    public String getDescription() { return "Classifies raw REST URI/intent paths to microservice handlers without regex bottlenecks"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "users", "billing", "inventory", "fallback", "stock", "warehouse", "item", "profile", "account", "user", "invoice", "charge", "payment", "fetch", "details", "for", "corporate", "check", "in", "unsupported", "ping", "healthcheck" };
        String[] classes = new String[]{ "users", "billing", "inventory", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("inventory", new String[]{"stock", "warehouse", "item"});
            anchorMap.put("users", new String[]{"profile", "account", "user"});
            anchorMap.put("billing", new String[]{"invoice", "charge", "payment"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "fetch user profile details");
        evaluateAndPrint(bundle, "charge invoice for corporate billing");
        evaluateAndPrint(bundle, "check item stock in warehouse");
        evaluateAndPrint(bundle, "unsupported ping healthcheck");
    }

    public static void main(String[] args) {
        new E01_RestApiIntentRouter().execute();
    }
}