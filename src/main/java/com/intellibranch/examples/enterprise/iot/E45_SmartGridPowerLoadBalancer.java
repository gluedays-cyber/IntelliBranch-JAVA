package com.intellibranch.examples.enterprise.iot;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E45] Smart Energy Grid Peak Demand Balancer
 * Domain: IoT & Edge Computing
 * Routes grid load telemetry to Battery Storage Discharge, Peaker Plant, or Renewable curtailment
 */
public class E45_SmartGridPowerLoadBalancer extends BaseDemo {

    @Override
    public String getId() { return "E45"; }

    @Override
    public String getTitle() { return "Smart Energy Grid Peak Demand Balancer"; }

    @Override
    public String getCategory() { return "IoT & Edge Computing"; }

    @Override
    public String getDescription() { return "Routes grid load telemetry to Battery Storage Discharge, Peaker Plant, or Renewable curtailment"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "dispatch_battery", "throttle_industrial", "grid_balanced", "fallback", "grid_frequency_drop", "peak_surge", "brownout_risk", "60hz_nominal", "solar_optimal", "surplus_wind", "shed_load", "curtail_hvac", "demand_response", "grid", "frequency", "drop", "peak", "surge", "brownout", "risk", "shed", "load", "curtail", "hvac", "demand", "response", "trigger", "60hz", "nominal", "solar", "optimal", "surplus", "wind", "substation", "sync", "ping" };
        String[] classes = new String[]{ "dispatch_battery", "throttle_industrial", "grid_balanced", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("dispatch_battery", new String[]{"grid_frequency_drop", "peak_surge", "brownout_risk"});
            anchorMap.put("grid_balanced", new String[]{"60hz_nominal", "solar_optimal", "surplus_wind"});
            anchorMap.put("throttle_industrial", new String[]{"shed_load", "curtail_hvac", "demand_response"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "grid frequency drop peak surge brownout risk");
        evaluateAndPrint(bundle, "shed load curtail hvac demand response trigger");
        evaluateAndPrint(bundle, "60hz nominal solar optimal surplus wind");
        evaluateAndPrint(bundle, "substation sync ping");
    }

    public static void main(String[] args) {
        new E45_SmartGridPowerLoadBalancer().execute();
    }
}