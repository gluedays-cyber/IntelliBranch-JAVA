package com.intellibranch.examples.enterprise.security;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E24] Zero-Trust Architecture Context Router
 * Domain: Security & Compliance
 * Evaluates device posture, context certificates, and least-privilege policies
 */
public class E24_ZeroTrustAccessEvaluator extends BaseDemo {

    @Override
    public String getId() { return "E24"; }

    @Override
    public String getTitle() { return "Zero-Trust Architecture Context Router"; }

    @Override
    public String getCategory() { return "Security & Compliance"; }

    @Override
    public String getDescription() { return "Evaluates device posture, context certificates, and least-privilege policies"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "grant_access", "require_posture_fix", "deny_access", "fallback", "rooted_device", "jailbroken", "unmanaged", "outdated_browser", "av_disabled", "screen_lock", "compliant_edr", "valid_cert", "patched_os", "compliant", "edr", "valid", "cert", "patched", "os", "verified", "outdated", "browser", "av", "disabled", "screen", "lock", "issue", "rooted", "device", "machine", "guest", "visitor", "network", "probe" };
        String[] classes = new String[]{ "grant_access", "require_posture_fix", "deny_access", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("deny_access", new String[]{"rooted_device", "jailbroken", "unmanaged"});
            anchorMap.put("require_posture_fix", new String[]{"outdated_browser", "av_disabled", "screen_lock"});
            anchorMap.put("grant_access", new String[]{"compliant_edr", "valid_cert", "patched_os"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "compliant edr valid cert patched os verified");
        evaluateAndPrint(bundle, "outdated browser av disabled screen lock issue");
        evaluateAndPrint(bundle, "rooted device jailbroken unmanaged machine");
        evaluateAndPrint(bundle, "guest visitor network probe");
    }

    public static void main(String[] args) {
        new E24_ZeroTrustAccessEvaluator().execute();
    }
}