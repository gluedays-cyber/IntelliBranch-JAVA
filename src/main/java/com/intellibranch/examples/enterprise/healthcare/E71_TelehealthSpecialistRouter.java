package com.intellibranch.examples.enterprise.healthcare;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E71] Telemedicine Virtual Consultation Doctor Router
 * Domain: Healthcare & BioInformatics
 * Routes patient intake symptom descriptions to Dermatology, Psychiatry, or Internal Medicine
 */
public class E71_TelehealthSpecialistRouter extends BaseDemo {

    @Override
    public String getId() { return "E71"; }

    @Override
    public String getTitle() { return "Telemedicine Virtual Consultation Doctor Router"; }

    @Override
    public String getCategory() { return "Healthcare & BioInformatics"; }

    @Override
    public String getDescription() { return "Routes patient intake symptom descriptions to Dermatology, Psychiatry, or Internal Medicine"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "route_dermatologist", "route_psychiatrist", "route_internist", "fallback", "erythema_rash", "eczema_flare", "mole_changing_color", "persistent_cough", "abdominal_pain", "fever_fatigue", "major_depression", "panic_attack", "insomnia_anxiety", "erythema", "rash", "eczema", "flare", "mole", "changing", "color", "photo", "major", "depression", "panic", "attack", "insomnia", "anxiety", "session", "persistent", "cough", "abdominal", "pain", "fever", "fatigue", "consult", "scheduling", "availability", "ping" };
        String[] classes = new String[]{ "route_dermatologist", "route_psychiatrist", "route_internist", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("route_dermatologist", new String[]{"erythema_rash", "eczema_flare", "mole_changing_color"});
            anchorMap.put("route_internist", new String[]{"persistent_cough", "abdominal_pain", "fever_fatigue"});
            anchorMap.put("route_psychiatrist", new String[]{"major_depression", "panic_attack", "insomnia_anxiety"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "erythema rash eczema flare mole changing color photo");
        evaluateAndPrint(bundle, "major depression panic attack insomnia anxiety session");
        evaluateAndPrint(bundle, "persistent cough abdominal pain fever fatigue consult");
        evaluateAndPrint(bundle, "scheduling availability ping");
    }

    public static void main(String[] args) {
        new E71_TelehealthSpecialistRouter().execute();
    }
}