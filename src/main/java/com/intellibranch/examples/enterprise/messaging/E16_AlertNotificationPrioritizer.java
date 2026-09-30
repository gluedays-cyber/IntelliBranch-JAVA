package com.intellibranch.examples.enterprise.messaging;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E16] PagerDuty Alert De-duplication & Escalation Router
 * Domain: Messaging & Event Streaming
 * Routes operational alerts to On-Call Pager, Slack channel, or daily digest
 */
public class E16_AlertNotificationPrioritizer extends BaseDemo {

    @Override
    public String getId() { return "E16"; }

    @Override
    public String getTitle() { return "PagerDuty Alert De-duplication & Escalation Router"; }

    @Override
    public String getCategory() { return "Messaging & Event Streaming"; }

    @Override
    public String getDescription() { return "Routes operational alerts to On-Call Pager, Slack channel, or daily digest"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "page_oncall", "slack_ops", "daily_digest", "fallback", "cert_expiry_30d", "info", "notice", "cpu_high", "slow_query", "warning", "production_down", "data_loss", "outage", "production", "down", "severe", "data", "loss", "cpu", "high", "slow", "query", "detected", "cert", "expiry", "30d", "unspecified", "monitor", "ping" };
        String[] classes = new String[]{ "page_oncall", "slack_ops", "daily_digest", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("daily_digest", new String[]{"cert_expiry_30d", "info", "notice"});
            anchorMap.put("slack_ops", new String[]{"cpu_high", "slow_query", "warning"});
            anchorMap.put("page_oncall", new String[]{"production_down", "data_loss", "outage"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "production down severe data loss outage");
        evaluateAndPrint(bundle, "cpu high warning slow query detected");
        evaluateAndPrint(bundle, "cert expiry 30d info notice");
        evaluateAndPrint(bundle, "unspecified monitor ping");
    }

    public static void main(String[] args) {
        new E16_AlertNotificationPrioritizer().execute();
    }
}