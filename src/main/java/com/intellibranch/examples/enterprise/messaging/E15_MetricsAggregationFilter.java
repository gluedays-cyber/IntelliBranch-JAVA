package com.intellibranch.examples.enterprise.messaging;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E15] Prometheus Metric Sampling & Pre-Filter
 * Domain: Messaging & Event Streaming
 * Pre-filters high-cardinality metric events before TSDB ingestion to control storage costs
 */
public class E15_MetricsAggregationFilter extends BaseDemo {

    @Override
    public String getId() { return "E15"; }

    @Override
    public String getTitle() { return "Prometheus Metric Sampling & Pre-Filter"; }

    @Override
    public String getCategory() { return "Messaging & Event Streaming"; }

    @Override
    public String getDescription() { return "Pre-filters high-cardinality metric events before TSDB ingestion to control storage costs"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "retain_100pct", "sample_10pct", "drop_discard", "fallback", "http_duration", "disk_io", "bytes", "slo", "availability", "error_rate", "ephemeral", "loopback", "tmp", "error", "rate", "critical", "http", "duration", "disk", "io", "sample", "thread", "metric", "unknown", "counter" };
        String[] classes = new String[]{ "retain_100pct", "sample_10pct", "drop_discard", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("sample_10pct", new String[]{"http_duration", "disk_io", "bytes"});
            anchorMap.put("retain_100pct", new String[]{"slo", "availability", "error_rate"});
            anchorMap.put("drop_discard", new String[]{"ephemeral", "loopback", "tmp"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "slo error rate critical availability");
        evaluateAndPrint(bundle, "http duration disk io bytes sample");
        evaluateAndPrint(bundle, "ephemeral loopback tmp thread metric");
        evaluateAndPrint(bundle, "unknown counter metric");
    }

    public static void main(String[] args) {
        new E15_MetricsAggregationFilter().execute();
    }
}