package com.intellibranch.examples.enterprise.healthcare;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E72] Health Insurance Claim Automated Adjudication
 * Domain: Healthcare & BioInformatics
 * Auto-approves compliant medical billing codes while flagging mismatched treatment guidelines
 */
public class E72_InsuranceClaimApprovalRouter extends BaseDemo {

    @Override
    public String getId() { return "E72"; }

    @Override
    public String getTitle() { return "Health Insurance Claim Automated Adjudication"; }

    @Override
    public String getCategory() { return "Healthcare & BioInformatics"; }

    @Override
    public String getDescription() { return "Auto-approves compliant medical billing codes while flagging mismatched treatment guidelines"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "auto_adjudicate_pay", "manual_medical_review", "deny_uncovered_procedure", "fallback", "experimental_therapy", "unlisted_cpt", "out_of_network_er", "cosmetic_rhinoplasty", "excluded_benefit", "expired_policy", "in_network_standard", "cpt_matched_icd10", "pre_authorized", "in", "network", "standard", "cpt", "matched", "icd10", "pre", "authorized", "claim", "experimental", "therapy", "unlisted", "out", "of", "er", "review", "cosmetic", "rhinoplasty", "excluded", "benefit", "expired", "policy", "deny", "corrupt", "file" };
        String[] classes = new String[]{ "auto_adjudicate_pay", "manual_medical_review", "deny_uncovered_procedure", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("manual_medical_review", new String[]{"experimental_therapy", "unlisted_cpt", "out_of_network_er"});
            anchorMap.put("deny_uncovered_procedure", new String[]{"cosmetic_rhinoplasty", "excluded_benefit", "expired_policy"});
            anchorMap.put("auto_adjudicate_pay", new String[]{"in_network_standard", "cpt_matched_icd10", "pre_authorized"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "in network standard cpt matched icd10 pre authorized claim");
        evaluateAndPrint(bundle, "experimental therapy unlisted cpt out of network er review");
        evaluateAndPrint(bundle, "cosmetic rhinoplasty excluded benefit expired policy deny");
        evaluateAndPrint(bundle, "corrupt claim file");
    }

    public static void main(String[] args) {
        new E72_InsuranceClaimApprovalRouter().execute();
    }
}