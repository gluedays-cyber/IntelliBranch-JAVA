package com.intellibranch.examples.enterprise.cloud;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E52] AWS EC2 Spot Instance 2-Minute Eviction Triage
 * Domain: Cloud Infrastructure & SRE
 * Intercepts CloudWatch 2-minute spot eviction notices to gracefully drain workloads
 */
public class E52_SpotInstanceEvictionHandler extends BaseDemo {

    @Override
    public String getId() { return "E52"; }

    @Override
    public String getTitle() { return "AWS EC2 Spot Instance 2-Minute Eviction Triage"; }

    @Override
    public String getCategory() { return "Cloud Infrastructure & SRE"; }

    @Override
    public String getDescription() { return "Intercepts CloudWatch 2-minute spot eviction notices to gracefully drain workloads"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "drain_and_checkpoint", "rebalance_to_ondemand", "ignore_worker", "fallback", "stateless_web", "redundant_replica", "auto_heal", "critical_batch_job", "cannot_fail", "provision_ondemand", "spot_interruption_notice", "save_state", "flush_state", "spot", "interruption", "notice", "save", "state", "flush", "checkpoint", "critical", "batch", "job", "cannot", "fail", "provision", "ondemand", "stateless", "web", "redundant", "replica", "auto", "heal", "node", "routine", "instance", "poll" };
        String[] classes = new String[]{ "drain_and_checkpoint", "rebalance_to_ondemand", "ignore_worker", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("ignore_worker", new String[]{"stateless_web", "redundant_replica", "auto_heal"});
            anchorMap.put("rebalance_to_ondemand", new String[]{"critical_batch_job", "cannot_fail", "provision_ondemand"});
            anchorMap.put("drain_and_checkpoint", new String[]{"spot_interruption_notice", "save_state", "flush_state"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "spot interruption notice save state flush state checkpoint");
        evaluateAndPrint(bundle, "critical batch job cannot fail provision ondemand fallback");
        evaluateAndPrint(bundle, "stateless web redundant replica auto heal node");
        evaluateAndPrint(bundle, "routine instance poll");
    }

    public static void main(String[] args) {
        new E52_SpotInstanceEvictionHandler().execute();
    }
}