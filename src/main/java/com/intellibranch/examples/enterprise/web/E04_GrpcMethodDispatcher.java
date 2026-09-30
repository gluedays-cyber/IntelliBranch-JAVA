package com.intellibranch.examples.enterprise.web;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E04] gRPC Multiplexed Stream Dispatcher
 * Domain: Web & API Services
 * Directs multiplexed HTTP/2 gRPC streaming calls to optimal worker thread pools
 */
public class E04_GrpcMethodDispatcher extends BaseDemo {

    @Override
    public String getId() { return "E04"; }

    @Override
    public String getTitle() { return "gRPC Multiplexed Stream Dispatcher"; }

    @Override
    public String getCategory() { return "Web & API Services"; }

    @Override
    public String getDescription() { return "Directs multiplexed HTTP/2 gRPC streaming calls to optimal worker thread pools"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "unary_worker", "bidi_stream", "heavy_compute", "fallback", "matrix", "render", "train", "ping", "lookup", "simple", "stream", "chat", "feed", "query", "live", "broadcast", "heavy", "compute", "job", "unrecognized", "grpc", "frame" };
        String[] classes = new String[]{ "unary_worker", "bidi_stream", "heavy_compute", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("heavy_compute", new String[]{"matrix", "render", "train"});
            anchorMap.put("unary_worker", new String[]{"ping", "lookup", "simple"});
            anchorMap.put("bidi_stream", new String[]{"stream", "chat", "feed"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "simple ping lookup query");
        evaluateAndPrint(bundle, "live chat stream feed broadcast");
        evaluateAndPrint(bundle, "matrix render heavy compute job");
        evaluateAndPrint(bundle, "unrecognized grpc frame");
    }

    public static void main(String[] args) {
        new E04_GrpcMethodDispatcher().execute();
    }
}