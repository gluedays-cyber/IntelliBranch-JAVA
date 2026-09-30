package com.intellibranch.examples.enterprise.healthcare;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E70] Genomics Next-Gen Sequencing Variant Triage
 * Domain: Healthcare & BioInformatics
 * Screens DNA variant call files (VCF) for pathogenic ACMG mutations
 */
public class E70_GenomicSequenceMarkerTriage extends BaseDemo {

    @Override
    public String getId() { return "E70"; }

    @Override
    public String getTitle() { return "Genomics Next-Gen Sequencing Variant Triage"; }

    @Override
    public String getCategory() { return "Healthcare & BioInformatics"; }

    @Override
    public String getDescription() { return "Screens DNA variant call files (VCF) for pathogenic ACMG mutations"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "variant_pathogenic_acmg", "variant_benign", "variant_uncertain_significance", "fallback", "brca1_frameshift", "nonsense_mutation", "truncating_stop", "synonymous_codon", "common_polymorphism", "intron_deep", "novel_missense", "conserved_domain", "vus_review", "brca1", "frameshift", "nonsense", "mutation", "truncating", "stop", "pathogenic", "synonymous", "codon", "common", "polymorphism", "intron", "deep", "benign", "novel", "missense", "conserved", "domain", "vus", "review", "finding", "low", "coverage", "read", "noise" };
        String[] classes = new String[]{ "variant_pathogenic_acmg", "variant_benign", "variant_uncertain_significance", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("variant_pathogenic_acmg", new String[]{"brca1_frameshift", "nonsense_mutation", "truncating_stop"});
            anchorMap.put("variant_benign", new String[]{"synonymous_codon", "common_polymorphism", "intron_deep"});
            anchorMap.put("variant_uncertain_significance", new String[]{"novel_missense", "conserved_domain", "vus_review"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "brca1 frameshift nonsense mutation truncating stop pathogenic");
        evaluateAndPrint(bundle, "synonymous codon common polymorphism intron deep benign");
        evaluateAndPrint(bundle, "novel missense conserved domain vus review finding");
        evaluateAndPrint(bundle, "low coverage read noise");
    }

    public static void main(String[] args) {
        new E70_GenomicSequenceMarkerTriage().execute();
    }
}