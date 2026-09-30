package com.intellibranch.examples.enterprise.security;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E21] L7 DDoS Pattern Recognizer & Blackhole
 * Domain: Security & Compliance
 * Identifies HTTP flood and Slowloris signatures to drop traffic at the gateway border
 */
public class E21_DdosTrafficClassifier extends BaseDemo {

    @Override
    public String getId() { return "E21"; }

    @Override
    public String getTitle() { return "L7 DDoS Pattern Recognizer & Blackhole"; }

    @Override
    public String getCategory() { return "Security & Compliance"; }

    @Override
    public String getDescription() { return "Identifies HTTP flood and Slowloris signatures to drop traffic at the gateway border"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "drop_blackhole", "challenge_captcha", "forward_origin", "fallback", "legitimate", "browser_verified", "organic", "syn_flood", "slowloris", "spoofed_ip", "rapid_clicks", "no_user_agent", "burst", "syn", "flood", "spoofed", "ip", "pattern", "rapid", "clicks", "no", "user", "agent", "request", "browser", "verified", "browsing", "isolated", "udp", "ping" };
        String[] classes = new String[]{ "drop_blackhole", "challenge_captcha", "forward_origin", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("forward_origin", new String[]{"legitimate", "browser_verified", "organic"});
            anchorMap.put("drop_blackhole", new String[]{"syn_flood", "slowloris", "spoofed_ip"});
            anchorMap.put("challenge_captcha", new String[]{"rapid_clicks", "no_user_agent", "burst"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "syn flood slowloris spoofed ip pattern");
        evaluateAndPrint(bundle, "rapid clicks no user agent burst request");
        evaluateAndPrint(bundle, "legitimate browser verified organic browsing");
        evaluateAndPrint(bundle, "isolated udp ping");
    }

    public static void main(String[] args) {
        new E21_DdosTrafficClassifier().execute();
    }
}