package com.intellibranch.examples.enterprise.healthcare;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E67] Electronic Health Record (EHR) Clinical Note Classifier
 * Domain: Healthcare & BioInformatics
 * Categorizes clinical progress notes into Cardiology, Oncology, or Neurology specialty registries
 */
public class E67_EhrClinicalNoteClassifier extends BaseDemo {

    @Override
    public String getId() { return "E67"; }

    @Override
    public String getTitle() { return "Electronic Health Record (EHR) Clinical Note Classifier"; }

    @Override
    public String getCategory() { return "Healthcare & BioInformatics"; }

    @Override
    public String getDescription() { return "Categorizes clinical progress notes into Cardiology, Oncology, or Neurology specialty registries"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "registry_cardiology", "registry_oncology", "registry_neurology", "fallback", "epileptic_seizure", "parkinsons_tremor", "eeg_spike", "carcinoma_biopsy", "chemotherapy_cycle", "metastasis", "ejection_fraction", "stenosis_valve", "coronary_bypass", "ejection", "fraction", "stenosis", "valve", "coronary", "bypass", "report", "carcinoma", "biopsy", "chemotherapy", "cycle", "tumor", "epileptic", "seizure", "parkinsons", "tremor", "eeg", "spike", "neurology", "billing", "demographic", "data" };
        String[] classes = new String[]{ "registry_cardiology", "registry_oncology", "registry_neurology", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("registry_neurology", new String[]{"epileptic_seizure", "parkinsons_tremor", "eeg_spike"});
            anchorMap.put("registry_oncology", new String[]{"carcinoma_biopsy", "chemotherapy_cycle", "metastasis"});
            anchorMap.put("registry_cardiology", new String[]{"ejection_fraction", "stenosis_valve", "coronary_bypass"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "ejection fraction stenosis valve coronary bypass report");
        evaluateAndPrint(bundle, "carcinoma biopsy chemotherapy cycle metastasis tumor");
        evaluateAndPrint(bundle, "epileptic seizure parkinsons tremor eeg spike neurology");
        evaluateAndPrint(bundle, "billing demographic data");
    }

    public static void main(String[] args) {
        new E67_EhrClinicalNoteClassifier().execute();
    }
}