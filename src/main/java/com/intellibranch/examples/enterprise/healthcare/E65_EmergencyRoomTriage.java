package com.intellibranch.examples.enterprise.healthcare;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E65] Hospital Emergency Room Clinical Triage (ESI Level)
 * Domain: Healthcare & BioInformatics
 * Classifies arriving ER patients into ESI 1 (Immediate Resuscitation) to ESI 5 (Non-Urgent)
 */
public class E65_EmergencyRoomTriage extends BaseDemo {

    @Override
    public String getId() { return "E65"; }

    @Override
    public String getTitle() { return "Hospital Emergency Room Clinical Triage (ESI Level)"; }

    @Override
    public String getCategory() { return "Healthcare & BioInformatics"; }

    @Override
    public String getDescription() { return "Classifies arriving ER patients into ESI 1 (Immediate Resuscitation) to ESI 5 (Non-Urgent)"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "esi1_resuscitation", "esi2_emergent_cardiac", "esi4_non_urgent", "fallback", "unresponsive_gcs3", "severe_hemorrhage", "pulseless", "mild_sore_throat", "suture_removal", "sprained_ankle", "crushing_chest_pain", "stroke_face_droop", "diabetic_keto", "unresponsive", "gcs3", "severe", "hemorrhage", "patient", "crushing", "chest", "pain", "stroke", "face", "droop", "diabetic", "keto", "mild", "sore", "throat", "suture", "removal", "sprained", "ankle", "walkin", "routine", "admin", "intake" };
        String[] classes = new String[]{ "esi1_resuscitation", "esi2_emergent_cardiac", "esi4_non_urgent", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("esi1_resuscitation", new String[]{"unresponsive_gcs3", "severe_hemorrhage", "pulseless"});
            anchorMap.put("esi4_non_urgent", new String[]{"mild_sore_throat", "suture_removal", "sprained_ankle"});
            anchorMap.put("esi2_emergent_cardiac", new String[]{"crushing_chest_pain", "stroke_face_droop", "diabetic_keto"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "unresponsive gcs3 severe hemorrhage pulseless patient");
        evaluateAndPrint(bundle, "crushing chest pain stroke face droop diabetic keto");
        evaluateAndPrint(bundle, "mild sore throat suture removal sprained ankle walkin");
        evaluateAndPrint(bundle, "routine admin intake");
    }

    public static void main(String[] args) {
        new E65_EmergencyRoomTriage().execute();
    }
}