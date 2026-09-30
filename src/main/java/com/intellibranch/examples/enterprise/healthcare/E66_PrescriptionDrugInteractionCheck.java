package com.intellibranch.examples.enterprise.healthcare;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E66] Pharmacotherapy Drug-Drug Interaction Screener
 * Domain: Healthcare & BioInformatics
 * Screens medication combinations for fatal CYP450 contraindications or QT prolongation
 */
public class E66_PrescriptionDrugInteractionCheck extends BaseDemo {

    @Override
    public String getId() { return "E66"; }

    @Override
    public String getTitle() { return "Pharmacotherapy Drug-Drug Interaction Screener"; }

    @Override
    public String getCategory() { return "Healthcare & BioInformatics"; }

    @Override
    public String getDescription() { return "Screens medication combinations for fatal CYP450 contraindications or QT prolongation"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "contraindication_block", "mild_interaction_warn", "safe_combination", "fallback", "absorption_delayed", "take_with_food", "mild_drowsy", "vitamin_c_zinc", "normal_saline", "compatible", "warfarin_aspirin_fatal", "maoi_ssri_serotonin", "grapefruit_statin", "warfarin", "aspirin", "fatal", "maoi", "ssri", "serotonin", "syndrome", "absorption", "delayed", "take", "with", "food", "mild", "drowsy", "advice", "vitamin", "c", "zinc", "normal", "saline", "mixture", "unidentified", "herbal", "supplement" };
        String[] classes = new String[]{ "contraindication_block", "mild_interaction_warn", "safe_combination", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("mild_interaction_warn", new String[]{"absorption_delayed", "take_with_food", "mild_drowsy"});
            anchorMap.put("safe_combination", new String[]{"vitamin_c_zinc", "normal_saline", "compatible"});
            anchorMap.put("contraindication_block", new String[]{"warfarin_aspirin_fatal", "maoi_ssri_serotonin", "grapefruit_statin"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "warfarin aspirin fatal maoi ssri serotonin syndrome");
        evaluateAndPrint(bundle, "absorption delayed take with food mild drowsy advice");
        evaluateAndPrint(bundle, "vitamin c zinc normal saline compatible mixture");
        evaluateAndPrint(bundle, "unidentified herbal supplement");
    }

    public static void main(String[] args) {
        new E66_PrescriptionDrugInteractionCheck().execute();
    }
}