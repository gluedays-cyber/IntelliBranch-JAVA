package com.intellibranch.examples.enterprise.iot;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E42] Industrial PLC & SCADA Anomaly Router
 * Domain: IoT & Edge Computing
 * Classifies factory floor vibration and thermal spikes for predictive maintenance dispatch
 */
public class E42_FactorySensorTelemetryAlert extends BaseDemo {

    @Override
    public String getId() { return "E42"; }

    @Override
    public String getTitle() { return "Industrial PLC & SCADA Anomaly Router"; }

    @Override
    public String getCategory() { return "IoT & Edge Computing"; }

    @Override
    public String getDescription() { return "Classifies factory floor vibration and thermal spikes for predictive maintenance dispatch"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "emergency_shutdown", "maintenance_dispatch", "normal_telemetry", "fallback", "rpm_nominal", "temp_steady", "optimal", "overpressure", "thermal_runaway", "bearing_seizure", "vibration_harmonic", "filter_clog", "oil_degraded", "bearing", "seizure", "thermal", "runaway", "detected", "vibration", "harmonic", "filter", "clog", "oil", "degraded", "alert", "rpm", "nominal", "temp", "steady", "reading", "offline", "sensor", "static" };
        String[] classes = new String[]{ "emergency_shutdown", "maintenance_dispatch", "normal_telemetry", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("normal_telemetry", new String[]{"rpm_nominal", "temp_steady", "optimal"});
            anchorMap.put("emergency_shutdown", new String[]{"overpressure", "thermal_runaway", "bearing_seizure"});
            anchorMap.put("maintenance_dispatch", new String[]{"vibration_harmonic", "filter_clog", "oil_degraded"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "bearing seizure overpressure thermal runaway detected");
        evaluateAndPrint(bundle, "vibration harmonic filter clog oil degraded alert");
        evaluateAndPrint(bundle, "rpm nominal temp steady optimal reading");
        evaluateAndPrint(bundle, "offline sensor static");
    }

    public static void main(String[] args) {
        new E42_FactorySensorTelemetryAlert().execute();
    }
}