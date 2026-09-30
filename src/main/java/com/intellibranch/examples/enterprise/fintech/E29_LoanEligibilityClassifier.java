package com.intellibranch.examples.enterprise.fintech;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E29] Instant Micro-Loan Underwriting Router
 * Domain: FinTech & Payments
 * Directs borrower applications to instant approval, manual audit, or immediate decline
 */
public class E29_LoanEligibilityClassifier extends BaseDemo {

    @Override
    public String getId() { return "E29"; }

    @Override
    public String getTitle() { return "Instant Micro-Loan Underwriting Router"; }

    @Override
    public String getCategory() { return "FinTech & Payments"; }

    @Override
    public String getDescription() { return "Directs borrower applications to instant approval, manual audit, or immediate decline"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "auto_approve", "underwriter_review", "decline_risk", "fallback", "default_history", "active_bankruptcy", "no_income", "prime_credit", "stable_income", "low_dti", "self_employed", "thin_file", "variable_bonus", "prime", "credit", "stable", "income", "low", "dti", "borrower", "self", "employed", "thin", "file", "variable", "bonus", "applicant", "active", "bankruptcy", "default", "history", "no", "unfilled", "form", "inquiry" };
        String[] classes = new String[]{ "auto_approve", "underwriter_review", "decline_risk", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("decline_risk", new String[]{"default_history", "active_bankruptcy", "no_income"});
            anchorMap.put("auto_approve", new String[]{"prime_credit", "stable_income", "low_dti"});
            anchorMap.put("underwriter_review", new String[]{"self_employed", "thin_file", "variable_bonus"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "prime credit stable income low dti borrower");
        evaluateAndPrint(bundle, "self employed thin file variable bonus applicant");
        evaluateAndPrint(bundle, "active bankruptcy default history no income");
        evaluateAndPrint(bundle, "unfilled form inquiry");
    }

    public static void main(String[] args) {
        new E29_LoanEligibilityClassifier().execute();
    }
}