package com.intellibranch.examples.enterprise.security;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E20] Adaptive MFA Step-Up Security Challenge
 * Domain: Security & Compliance
 * Detects risk anomalies in authentication attempts and triggers FIDO2 / TOTP step-up
 */
public class E20_MfaStepUpEnforcer extends BaseDemo {

    @Override
    public String getId() { return "E20"; }

    @Override
    public String getTitle() { return "Adaptive MFA Step-Up Security Challenge"; }

    @Override
    public String getCategory() { return "Security & Compliance"; }

    @Override
    public String getDescription() { return "Detects risk anomalies in authentication attempts and triggers FIDO2 / TOTP step-up"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "require_mfa", "allow_session", "lockout_account", "fallback", "known_browser", "home_wifi", "trusted", "new_device", "tor_exit", "geo_velocity", "brute_force", "credential_stuffing", "100_fails", "new", "device", "tor", "exit", "node", "geo", "velocity", "jump", "known", "browser", "home", "wifi", "subnet", "brute", "force", "credential", "stuffing", "100", "fails", "standard", "login", "attempt" };
        String[] classes = new String[]{ "require_mfa", "allow_session", "lockout_account", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("allow_session", new String[]{"known_browser", "home_wifi", "trusted"});
            anchorMap.put("require_mfa", new String[]{"new_device", "tor_exit", "geo_velocity"});
            anchorMap.put("lockout_account", new String[]{"brute_force", "credential_stuffing", "100_fails"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "new device tor exit node geo velocity jump");
        evaluateAndPrint(bundle, "known browser home wifi trusted subnet");
        evaluateAndPrint(bundle, "brute force credential stuffing 100 fails");
        evaluateAndPrint(bundle, "standard login attempt");
    }

    public static void main(String[] args) {
        new E20_MfaStepUpEnforcer().execute();
    }
}