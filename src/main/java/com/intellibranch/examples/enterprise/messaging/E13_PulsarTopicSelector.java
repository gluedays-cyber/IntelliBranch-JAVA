package com.intellibranch.examples.enterprise.messaging;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E13] Apache Pulsar Hierarchical Topic Selector
 * Domain: Messaging & Event Streaming
 * Maps unstructured tenant events to geo-replicated tenant Pulsar topics
 */
public class E13_PulsarTopicSelector extends BaseDemo {

    @Override
    public String getId() { return "E13"; }

    @Override
    public String getTitle() { return "Apache Pulsar Hierarchical Topic Selector"; }

    @Override
    public String getCategory() { return "Messaging & Event Streaming"; }

    @Override
    public String getDescription() { return "Maps unstructured tenant events to geo-replicated tenant Pulsar topics"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "us_east_topic", "eu_west_topic", "ap_south_topic", "fallback", "virginia", "newyork", "useast", "frankfurt", "dublin", "euwest", "tokyo", "singapore", "apsouth", "tenant", "cluster", "event", "compliance", "zone", "edge", "gateway", "global", "satellite", "ping" };
        String[] classes = new String[]{ "us_east_topic", "eu_west_topic", "ap_south_topic", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("us_east_topic", new String[]{"virginia", "newyork", "useast"});
            anchorMap.put("eu_west_topic", new String[]{"frankfurt", "dublin", "euwest"});
            anchorMap.put("ap_south_topic", new String[]{"tokyo", "singapore", "apsouth"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "virginia useast tenant cluster event");
        evaluateAndPrint(bundle, "frankfurt euwest compliance zone");
        evaluateAndPrint(bundle, "tokyo apsouth edge gateway");
        evaluateAndPrint(bundle, "global satellite ping");
    }

    public static void main(String[] args) {
        new E13_PulsarTopicSelector().execute();
    }
}