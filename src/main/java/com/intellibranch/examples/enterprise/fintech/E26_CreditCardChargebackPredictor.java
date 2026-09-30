package com.intellibranch.examples.enterprise.fintech;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E26] Chargeback & Dispute Risk Predictor
 * Domain: FinTech & Payments
 * Predicts dispute probability based on merchant code, basket velocity, and geographic jump
 */
public class E26_CreditCardChargebackPredictor extends BaseDemo {

    @Override
    public String getId() { return "E26"; }

    @Override
    public String getTitle() { return "Chargeback & Dispute Risk Predictor"; }

    @Override
    public String getCategory() { return "FinTech & Payments"; }

    @Override
    public String getDescription() { return "Predicts dispute probability based on merchant code, basket velocity, and geographic jump"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "reject_transaction", "require_3ds", "approve_transaction", "fallback", "cross_border", "high_amount", "new_merchant", "stolen_bin", "velocity_burst", "mismatched_cvv", "chip_dip", "tokenized_apple_pay", "regular", "stolen", "bin", "velocity", "burst", "mismatched", "cvv", "attempt", "cross", "border", "high", "amount", "new", "merchant", "purchase", "chip", "dip", "tokenized", "apple", "pay", "store", "random", "pos", "ping" };
        String[] classes = new String[]{ "reject_transaction", "require_3ds", "approve_transaction", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("require_3ds", new String[]{"cross_border", "high_amount", "new_merchant"});
            anchorMap.put("reject_transaction", new String[]{"stolen_bin", "velocity_burst", "mismatched_cvv"});
            anchorMap.put("approve_transaction", new String[]{"chip_dip", "tokenized_apple_pay", "regular"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "stolen bin velocity burst mismatched cvv attempt");
        evaluateAndPrint(bundle, "cross border high amount new merchant purchase");
        evaluateAndPrint(bundle, "chip dip tokenized apple pay regular store");
        evaluateAndPrint(bundle, "random pos ping");
    }

    public static void main(String[] args) {
        new E26_CreditCardChargebackPredictor().execute();
    }
}