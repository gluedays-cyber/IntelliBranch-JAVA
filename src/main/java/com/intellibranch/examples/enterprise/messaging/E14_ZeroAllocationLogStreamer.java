package com.intellibranch.examples.enterprise.messaging;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E14] 0 B/op Zero-Allocation Log Stream Classifier
 * Domain: Messaging & Event Streaming
 * Parses high-volume syslog records in hot memory paths without creating String garbage
 */
public class E14_ZeroAllocationLogStreamer extends BaseDemo {

    @Override
    public String getId() { return "E14"; }

    @Override
    public String getTitle() { return "0 B/op Zero-Allocation Log Stream Classifier"; }

    @Override
    public String getCategory() { return "Messaging & Event Streaming"; }

    @Override
    public String getDescription() { return "Parses high-volume syslog records in hot memory paths without creating String garbage"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "syslog_error", "syslog_security", "syslog_debug", "fallback", "trace", "enter", "exit", "fatal", "segfault", "panic", "unauthorized", "sudo", "auth_fail", "kernel", "0x80", "auth", "fail", "attempt", "function", "loop", "debug", "regular", "system", "uptime", "info" };
        String[] classes = new String[]{ "syslog_error", "syslog_security", "syslog_debug", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("syslog_debug", new String[]{"trace", "enter", "exit"});
            anchorMap.put("syslog_error", new String[]{"fatal", "segfault", "panic"});
            anchorMap.put("syslog_security", new String[]{"unauthorized", "sudo", "auth_fail"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "kernel panic fatal segfault 0x80");
        evaluateAndPrint(bundle, "sudo auth fail unauthorized attempt");
        evaluateAndPrint(bundle, "trace enter function loop debug");
        evaluateAndPrint(bundle, "regular system uptime info");
    }

    public static void main(String[] args) {
        new E14_ZeroAllocationLogStreamer().execute();
    }
}