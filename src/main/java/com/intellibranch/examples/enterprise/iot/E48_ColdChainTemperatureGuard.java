package com.intellibranch.examples.enterprise.iot;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E48] Pharmaceutical Cold-Chain Excursion Monitor
 * Domain: IoT & Edge Computing
 * Detects vaccine transport refrigeration unit failures and routes to emergency dry-ice depot
 */
public class E48_ColdChainTemperatureGuard extends BaseDemo {

    @Override
    public String getId() { return "E48"; }

    @Override
    public String getTitle() { return "Pharmaceutical Cold-Chain Excursion Monitor"; }

    @Override
    public String getCategory() { return "IoT & Edge Computing"; }

    @Override
    public String getDescription() { return "Detects vaccine transport refrigeration unit failures and routes to emergency dry-ice depot"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "depot_emergency_ice", "adjust_cooling", "safe_temperature", "fallback", "steady_minus70c", "optimal_vaccine", "nitrogen_full", "temp_excursion_plus8c", "compressor_burn", "door_ajar", "temp_drift_6c", "fan_slow", "mild_rise", "temp", "excursion", "plus8c", "compressor", "burn", "door", "ajar", "failure", "drift", "6c", "fan", "slow", "mild", "rise", "warning", "steady", "minus70c", "optimal", "vaccine", "nitrogen", "full", "uncalibrated", "probe", "noise" };
        String[] classes = new String[]{ "depot_emergency_ice", "adjust_cooling", "safe_temperature", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("safe_temperature", new String[]{"steady_minus70c", "optimal_vaccine", "nitrogen_full"});
            anchorMap.put("depot_emergency_ice", new String[]{"temp_excursion_plus8c", "compressor_burn", "door_ajar"});
            anchorMap.put("adjust_cooling", new String[]{"temp_drift_6c", "fan_slow", "mild_rise"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "temp excursion plus8c compressor burn door ajar failure");
        evaluateAndPrint(bundle, "temp drift 6c fan slow mild rise warning");
        evaluateAndPrint(bundle, "steady minus70c optimal vaccine nitrogen full");
        evaluateAndPrint(bundle, "uncalibrated probe noise");
    }

    public static void main(String[] args) {
        new E48_ColdChainTemperatureGuard().execute();
    }
}