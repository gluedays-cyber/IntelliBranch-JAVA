package com.intellibranch.examples.enterprise.cloud;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E53] Distributed Log Stream Semantic Anomaly Detector
 * Domain: Cloud Infrastructure & SRE
 * Identifies novel panic signatures and unhandled stack traces across thousands of microservices
 */
public class E53_LogClusterAnomalyDetector extends BaseDemo {

    @Override
    public String getId() { return "E53"; }

    @Override
    public String getTitle() { return "Distributed Log Stream Semantic Anomaly Detector"; }

    @Override
    public String getCategory() { return "Cloud Infrastructure & SRE"; }

    @Override
    public String getDescription() { return "Identifies novel panic signatures and unhandled stack traces across thousands of microservices"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "novel_panic_anomaly", "known_benign_exception", "routine_info_log", "fallback", "connection_reset_by_peer", "file_not_found_favicon", "socket_closed", "started_service_in", "loaded_spring_context", "listening_port", "unhandled_nullpointer", "stack_overflow", "sigsegv_native", "unhandled", "nullpointer", "stack", "overflow", "sigsegv", "native", "core", "dump", "connection", "reset", "by", "peer", "file", "not", "found", "favicon", "socket", "closed", "started", "service", "in", "loaded", "spring", "context", "listening", "port", "8080", "unstructured", "raw", "stdout", "string" };
        String[] classes = new String[]{ "novel_panic_anomaly", "known_benign_exception", "routine_info_log", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("known_benign_exception", new String[]{"connection_reset_by_peer", "file_not_found_favicon", "socket_closed"});
            anchorMap.put("routine_info_log", new String[]{"started_service_in", "loaded_spring_context", "listening_port"});
            anchorMap.put("novel_panic_anomaly", new String[]{"unhandled_nullpointer", "stack_overflow", "sigsegv_native"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "unhandled nullpointer stack overflow sigsegv native core dump");
        evaluateAndPrint(bundle, "connection reset by peer file not found favicon socket closed");
        evaluateAndPrint(bundle, "started service in loaded spring context listening port 8080");
        evaluateAndPrint(bundle, "unstructured raw stdout string");
    }

    public static void main(String[] args) {
        new E53_LogClusterAnomalyDetector().execute();
    }
}