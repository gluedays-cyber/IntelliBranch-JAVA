package com.intellibranch.examples.enterprise.messaging;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E09] Kafka Intelligent Partition Key Router
 * Domain: Messaging & Event Streaming
 * Analyzes message payload to assign optimal Kafka partition key preventing skew
 */
public class E09_KafkaPartitionKeyRouter extends BaseDemo {

    @Override
    public String getId() { return "E09"; }

    @Override
    public String getTitle() { return "Kafka Intelligent Partition Key Router"; }

    @Override
    public String getCategory() { return "Messaging & Event Streaming"; }

    @Override
    public String getDescription() { return "Analyzes message payload to assign optimal Kafka partition key preventing skew"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "partition_finance", "partition_logistics", "partition_audit", "fallback", "compliance", "retention", "gdpr", "tracking", "package", "freight", "ledger", "currency", "transfer", "event", "update", "audit", "noise", "message", "line" };
        String[] classes = new String[]{ "partition_finance", "partition_logistics", "partition_audit", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("partition_audit", new String[]{"compliance", "retention", "gdpr"});
            anchorMap.put("partition_logistics", new String[]{"tracking", "package", "freight"});
            anchorMap.put("partition_finance", new String[]{"ledger", "currency", "transfer"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "transfer ledger currency event");
        evaluateAndPrint(bundle, "package freight tracking update");
        evaluateAndPrint(bundle, "compliance audit gdpr retention");
        evaluateAndPrint(bundle, "noise message line");
    }

    public static void main(String[] args) {
        new E09_KafkaPartitionKeyRouter().execute();
    }
}