package com.intellibranch.examples.enterprise.healthcare;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E69] ICU Telemetry False-Positive Alarm Suppressor
 * Domain: Healthcare & BioInformatics
 * Filters out nuisance monitor alarms caused by loose ECG leads or patient motion
 */
public class E69_PatientVitalAlarmSuppressor extends BaseDemo {

    @Override
    public String getId() { return "E69"; }

    @Override
    public String getTitle() { return "ICU Telemetry False-Positive Alarm Suppressor"; }

    @Override
    public String getCategory() { return "Healthcare & BioInformatics"; }

    @Override
    public String getDescription() { return "Filters out nuisance monitor alarms caused by loose ECG leads or patient motion"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "real_clinical_alarm", "suppress_nuisance_artifact", "recalibrate_lead", "fallback", "impedance_high", "lead_v1_loose", "noisy_baseline", "toothbrush_artifact", "shivering_tremor", "electrode_detached", "sustained_bradycardia", "oxygen_desat_60", "hypertensive_crisis", "sustained", "bradycardia", "oxygen", "desat", "60", "hypertensive", "crisis", "toothbrush", "artifact", "shivering", "tremor", "electrode", "detached", "fake", "impedance", "high", "lead", "v1", "loose", "noisy", "baseline", "system", "diagnostic", "test" };
        String[] classes = new String[]{ "real_clinical_alarm", "suppress_nuisance_artifact", "recalibrate_lead", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("recalibrate_lead", new String[]{"impedance_high", "lead_v1_loose", "noisy_baseline"});
            anchorMap.put("suppress_nuisance_artifact", new String[]{"toothbrush_artifact", "shivering_tremor", "electrode_detached"});
            anchorMap.put("real_clinical_alarm", new String[]{"sustained_bradycardia", "oxygen_desat_60", "hypertensive_crisis"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "sustained bradycardia oxygen desat 60 hypertensive crisis");
        evaluateAndPrint(bundle, "toothbrush artifact shivering tremor electrode detached fake");
        evaluateAndPrint(bundle, "impedance high lead v1 loose noisy baseline");
        evaluateAndPrint(bundle, "system diagnostic test");
    }

    public static void main(String[] args) {
        new E69_PatientVitalAlarmSuppressor().execute();
    }
}