package com.intellibranch.examples.enterprise.cloud;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E51] PagerDuty Major Incident Severity Triage (SEV1-SEV3)
 * Domain: Cloud Infrastructure & SRE
 * Classifies incident chat channels and logs into SEV1, SEV2, or SEV3 priority levels
 */
public class E51_IncidentSeverityClassifier extends BaseDemo {

    @Override
    public String getId() { return "E51"; }

    @Override
    public String getTitle() { return "PagerDuty Major Incident Severity Triage (SEV1-SEV3)"; }

    @Override
    public String getCategory() { return "Cloud Infrastructure & SRE"; }

    @Override
    public String getDescription() { return "Classifies incident chat channels and logs into SEV1, SEV2, or SEV3 priority levels"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "sev1_critical_war_room", "sev2_high_degraded", "sev3_minor_bug", "fallback", "typo_dashboard", "cosmetic_css", "internal_admin", "payment_failing", "global_outage", "ceo_escalation", "latency_spike_asia", "search_partial_down", "retry_surge", "global", "outage", "payment", "failing", "ceo", "escalation", "war", "room", "latency", "spike", "asia", "search", "partial", "down", "retry", "surge", "typo", "dashboard", "cosmetic", "css", "internal", "admin", "issue", "informational", "slack", "message" };
        String[] classes = new String[]{ "sev1_critical_war_room", "sev2_high_degraded", "sev3_minor_bug", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("sev3_minor_bug", new String[]{"typo_dashboard", "cosmetic_css", "internal_admin"});
            anchorMap.put("sev1_critical_war_room", new String[]{"payment_failing", "global_outage", "ceo_escalation"});
            anchorMap.put("sev2_high_degraded", new String[]{"latency_spike_asia", "search_partial_down", "retry_surge"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "global outage payment failing ceo escalation war room");
        evaluateAndPrint(bundle, "latency spike asia search partial down retry surge");
        evaluateAndPrint(bundle, "typo dashboard cosmetic css internal admin issue");
        evaluateAndPrint(bundle, "informational slack message");
    }

    public static void main(String[] args) {
        new E51_IncidentSeverityClassifier().execute();
    }
}