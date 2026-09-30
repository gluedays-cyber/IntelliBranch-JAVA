package com.intellibranch.examples.enterprise.cloud;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E50] OpenTelemetry Intelligent Trace Head Sampler
 * Domain: Cloud Infrastructure & SRE
 * Samples 100% of slow/erroneous traces while downsampling boring 200 OK healthchecks
 */
public class E50_DistributedTraceSamplerDemo extends BaseDemo {

    @Override
    public String getId() { return "E50"; }

    @Override
    public String getTitle() { return "OpenTelemetry Intelligent Trace Head Sampler"; }

    @Override
    public String getCategory() { return "Cloud Infrastructure & SRE"; }

    @Override
    public String getDescription() { return "Samples 100% of slow/erroneous traces while downsampling boring 200 OK healthchecks"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "sample_full_trace", "sample_probabilistic_1pct", "drop_trace", "fallback", "http_200", "fast_10ms", "standard_read", "http_500", "latency_exceed_2s", "db_deadlock", "k8s_readiness_probe", "internal_poll", "kubelet", "http", "500", "latency", "exceed", "2s", "db", "deadlock", "trace", "200", "fast", "10ms", "standard", "read", "request", "k8s", "readiness", "probe", "internal", "poll", "unknown", "context" };
        String[] classes = new String[]{ "sample_full_trace", "sample_probabilistic_1pct", "drop_trace", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("sample_probabilistic_1pct", new String[]{"http_200", "fast_10ms", "standard_read"});
            anchorMap.put("sample_full_trace", new String[]{"http_500", "latency_exceed_2s", "db_deadlock"});
            anchorMap.put("drop_trace", new String[]{"k8s_readiness_probe", "internal_poll", "kubelet"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "http 500 latency exceed 2s db deadlock trace");
        evaluateAndPrint(bundle, "http 200 fast 10ms standard read request");
        evaluateAndPrint(bundle, "k8s readiness probe kubelet internal poll");
        evaluateAndPrint(bundle, "unknown trace context");
    }

    public static void main(String[] args) {
        new E50_DistributedTraceSamplerDemo().execute();
    }
}