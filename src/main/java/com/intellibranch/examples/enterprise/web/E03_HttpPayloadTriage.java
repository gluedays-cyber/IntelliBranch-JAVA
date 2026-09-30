package com.intellibranch.examples.enterprise.web;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E03] HTTP Request Payload QoS Prioritizer
 * Domain: Web & API Services
 * Triages incoming HTTP payloads into High, Normal, and Low priority queues
 */
public class E03_HttpPayloadTriage extends BaseDemo {

    @Override
    public String getId() { return "E03"; }

    @Override
    public String getTitle() { return "HTTP Request Payload QoS Prioritizer"; }

    @Override
    public String getCategory() { return "Web & API Services"; }

    @Override
    public String getDescription() { return "Triages incoming HTTP payloads into High, Normal, and Low priority queues"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "p0_critical", "p1_normal", "p2_batch", "fallback", "sync", "update", "get", "urgent", "crash", "emergency", "bulk", "export", "dump", "report", "user", "settings", "historical", "unknown", "request", "payload" };
        String[] classes = new String[]{ "p0_critical", "p1_normal", "p2_batch", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("p1_normal", new String[]{"sync", "update", "get"});
            anchorMap.put("p0_critical", new String[]{"urgent", "crash", "emergency"});
            anchorMap.put("p2_batch", new String[]{"bulk", "export", "dump"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "emergency crash dump report");
        evaluateAndPrint(bundle, "sync user settings update");
        evaluateAndPrint(bundle, "bulk export historical dump");
        evaluateAndPrint(bundle, "unknown request payload");
    }

    public static void main(String[] args) {
        new E03_HttpPayloadTriage().execute();
    }
}