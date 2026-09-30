package com.intellibranch.examples.enterprise.fintech;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E27] Cryptocurrency AML/CFT Compliance Triage
 * Domain: FinTech & Payments
 * Inspects on-chain inputs against OFAC sanctions and darknet mixer heuristics
 */
public class E27_CryptoAmlTransactionRouter extends BaseDemo {

    @Override
    public String getId() { return "E27"; }

    @Override
    public String getTitle() { return "Cryptocurrency AML/CFT Compliance Triage"; }

    @Override
    public String getCategory() { return "FinTech & Payments"; }

    @Override
    public String getDescription() { return "Inspects on-chain inputs against OFAC sanctions and darknet mixer heuristics"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "block_ofac_sanction", "quarantine_aml", "clean_wallet_clear", "fallback", "tornado_cash", "sanctioned_entity", "lazarus", "coinbase_custody", "kyc_verified", "clean_utxo", "high_hop_count", "unregistered_vasp", "p2p_mixer", "tornado", "cash", "sanctioned", "entity", "wallet", "hit", "high", "hop", "count", "unregistered", "vasp", "p2p", "mixer", "trace", "coinbase", "custody", "kyc", "verified", "clean", "utxo", "transfer", "zero", "memo" };
        String[] classes = new String[]{ "block_ofac_sanction", "quarantine_aml", "clean_wallet_clear", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("block_ofac_sanction", new String[]{"tornado_cash", "sanctioned_entity", "lazarus"});
            anchorMap.put("clean_wallet_clear", new String[]{"coinbase_custody", "kyc_verified", "clean_utxo"});
            anchorMap.put("quarantine_aml", new String[]{"high_hop_count", "unregistered_vasp", "p2p_mixer"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "tornado cash sanctioned entity lazarus wallet hit");
        evaluateAndPrint(bundle, "high hop count unregistered vasp p2p mixer trace");
        evaluateAndPrint(bundle, "coinbase custody kyc verified clean utxo transfer");
        evaluateAndPrint(bundle, "zero transfer memo");
    }

    public static void main(String[] args) {
        new E27_CryptoAmlTransactionRouter().execute();
    }
}