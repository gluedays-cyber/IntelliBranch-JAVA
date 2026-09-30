package com.intellibranch.examples.enterprise.messaging;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E10] RabbitMQ Dead-Letter Reason Classifier
 * Domain: Messaging & Event Streaming
 * Routes rejected queue messages to retry, discard, or operator inspection exchanges
 */
public class E10_RabbitMqDeadLetterRouter extends BaseDemo {

    @Override
    public String getId() { return "E10"; }

    @Override
    public String getTitle() { return "RabbitMQ Dead-Letter Reason Classifier"; }

    @Override
    public String getCategory() { return "Messaging & Event Streaming"; }

    @Override
    public String getDescription() { return "Routes rejected queue messages to retry, discard, or operator inspection exchanges"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "retry_queue", "discard_queue", "manual_operator", "fallback", "lock_conflict", "transient", "network", "fraud", "tamper", "breach", "corrupt", "malformed", "junk", "lock", "conflict", "retry", "payload", "flag", "manual", "unknown", "rejection", "notice" };
        String[] classes = new String[]{ "retry_queue", "discard_queue", "manual_operator", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("retry_queue", new String[]{"lock_conflict", "transient", "network"});
            anchorMap.put("manual_operator", new String[]{"fraud", "tamper", "breach"});
            anchorMap.put("discard_queue", new String[]{"corrupt", "malformed", "junk"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "transient network lock conflict retry");
        evaluateAndPrint(bundle, "malformed corrupt junk payload");
        evaluateAndPrint(bundle, "tamper breach fraud flag manual");
        evaluateAndPrint(bundle, "unknown rejection notice");
    }

    public static void main(String[] args) {
        new E10_RabbitMqDeadLetterRouter().execute();
    }
}