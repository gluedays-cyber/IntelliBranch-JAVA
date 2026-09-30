package com.intellibranch.examples.enterprise.fintech;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E32] VAT & Corporate Tax Audit Risk Screener
 * Domain: FinTech & Payments
 * Screens transaction batches for suspicious transfer pricing or missing VAT receipts
 */
public class E32_TaxAuditRiskTriage extends BaseDemo {

    @Override
    public String getId() { return "E32"; }

    @Override
    public String getTitle() { return "VAT & Corporate Tax Audit Risk Screener"; }

    @Override
    public String getCategory() { return "FinTech & Payments"; }

    @Override
    public String getDescription() { return "Screens transaction batches for suspicious transfer pricing or missing VAT receipts"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "audit_required", "automated_tax_clear", "vat_reverse_charge", "fallback", "carousel_scheme", "missing_tax_id", "offshore_haven", "b2b_cross_border", "eu_reverse", "tax_exempt", "standard_receipt", "vat_validated", "matched_po", "carousel", "scheme", "missing", "tax", "id", "offshore", "haven", "payment", "standard", "receipt", "vat", "validated", "matched", "po", "b2b", "cross", "border", "eu", "reverse", "exempt", "sale", "unregistered", "donation", "memo" };
        String[] classes = new String[]{ "audit_required", "automated_tax_clear", "vat_reverse_charge", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("audit_required", new String[]{"carousel_scheme", "missing_tax_id", "offshore_haven"});
            anchorMap.put("vat_reverse_charge", new String[]{"b2b_cross_border", "eu_reverse", "tax_exempt"});
            anchorMap.put("automated_tax_clear", new String[]{"standard_receipt", "vat_validated", "matched_po"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "carousel scheme missing tax id offshore haven payment");
        evaluateAndPrint(bundle, "standard receipt vat validated matched po");
        evaluateAndPrint(bundle, "b2b cross border eu reverse tax exempt sale");
        evaluateAndPrint(bundle, "unregistered donation memo");
    }

    public static void main(String[] args) {
        new E32_TaxAuditRiskTriage().execute();
    }
}