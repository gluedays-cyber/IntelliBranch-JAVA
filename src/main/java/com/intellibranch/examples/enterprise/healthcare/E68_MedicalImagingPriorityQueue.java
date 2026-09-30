package com.intellibranch.examples.enterprise.healthcare;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E68] PACS Radiology Scan Urgent Review Triage
 * Domain: Healthcare & BioInformatics
 * Triages DICOM CT/MRI scans for acute intracranial hemorrhage or pulmonary embolism findings
 */
public class E68_MedicalImagingPriorityQueue extends BaseDemo {

    @Override
    public String getId() { return "E68"; }

    @Override
    public String getTitle() { return "PACS Radiology Scan Urgent Review Triage"; }

    @Override
    public String getCategory() { return "Healthcare & BioInformatics"; }

    @Override
    public String getDescription() { return "Triages DICOM CT/MRI scans for acute intracranial hemorrhage or pulmonary embolism findings"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "stat_radiology_reading", "standard_worklist", "screening_elective", "fallback", "annual_mammogram", "routine_spine_mri", "wellness_check", "intracranial_hemorrhage", "pulmonary_embolism", "aortic_dissection", "followup_nodule", "fracture_stable", "routine_xray", "intracranial", "hemorrhage", "pulmonary", "embolism", "aortic", "dissection", "stat", "followup", "nodule", "fracture", "stable", "routine", "xray", "reading", "annual", "mammogram", "spine", "mri", "wellness", "check", "corrupted", "dicom", "metadata" };
        String[] classes = new String[]{ "stat_radiology_reading", "standard_worklist", "screening_elective", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("screening_elective", new String[]{"annual_mammogram", "routine_spine_mri", "wellness_check"});
            anchorMap.put("stat_radiology_reading", new String[]{"intracranial_hemorrhage", "pulmonary_embolism", "aortic_dissection"});
            anchorMap.put("standard_worklist", new String[]{"followup_nodule", "fracture_stable", "routine_xray"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "intracranial hemorrhage pulmonary embolism aortic dissection stat");
        evaluateAndPrint(bundle, "followup nodule fracture stable routine xray reading");
        evaluateAndPrint(bundle, "annual mammogram routine spine mri wellness check");
        evaluateAndPrint(bundle, "corrupted dicom metadata");
    }

    public static void main(String[] args) {
        new E68_MedicalImagingPriorityQueue().execute();
    }
}