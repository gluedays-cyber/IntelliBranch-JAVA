package com.intellibranch.examples.enterprise.iot;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E44] Wearable Patient Vital Signs ECG Triage
 * Domain: IoT & Edge Computing
 * Monitors ECG rhythm strips for arrhythmia or ventricular fibrillation alerts
 */
public class E44_MedicalTelemetryMonitor extends BaseDemo {

    @Override
    public String getId() { return "E44"; }

    @Override
    public String getTitle() { return "Wearable Patient Vital Signs ECG Triage"; }

    @Override
    public String getCategory() { return "IoT & Edge Computing"; }

    @Override
    public String getDescription() { return "Monitors ECG rhythm strips for arrhythmia or ventricular fibrillation alerts"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "code_blue_alert", "nurse_station_flag", "patient_resting", "fallback", "sinus_rhythm", "hr_72", "stable_vitals", "v_fib", "asystole", "cardiac_arrest", "tachycardia", "pvc_spike", "spo2_low", "v", "fib", "cardiac", "arrest", "code", "blue", "emergency", "pvc", "spike", "spo2", "low", "notification", "sinus", "rhythm", "hr", "72", "stable", "vitals", "resting", "lead", "off", "disconnect", "noise" };
        String[] classes = new String[]{ "code_blue_alert", "nurse_station_flag", "patient_resting", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("patient_resting", new String[]{"sinus_rhythm", "hr_72", "stable_vitals"});
            anchorMap.put("code_blue_alert", new String[]{"v_fib", "asystole", "cardiac_arrest"});
            anchorMap.put("nurse_station_flag", new String[]{"tachycardia", "pvc_spike", "spo2_low"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "v fib asystole cardiac arrest code blue emergency");
        evaluateAndPrint(bundle, "tachycardia pvc spike spo2 low notification");
        evaluateAndPrint(bundle, "sinus rhythm hr 72 stable vitals resting");
        evaluateAndPrint(bundle, "lead off disconnect noise");
    }

    public static void main(String[] args) {
        new E44_MedicalTelemetryMonitor().execute();
    }
}