package com.intellibranch.examples.enterprise.fintech;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E25] Real-Time Wire Transfer Anti-Fraud Guard
 * Domain: FinTech & Payments
 * Scans wire transaction memos to intercept synthetic identity and mule accounts
 */
public class E25_WireTransferFraudGuard extends BaseDemo {

    @Override
    public String getId() { return "E25"; }

    @Override
    public String getTitle() { return "Real-Time Wire Transfer Anti-Fraud Guard"; }

    @Override
    public String getCategory() { return "FinTech & Payments"; }

    @Override
    public String getDescription() { return "Scans wire transaction memos to intercept synthetic identity and mule accounts"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "freeze_transfer", "flag_review", "instant_clear", "fallback", "first_time_payee", "limit_threshold", "unusual_hour", "utility_bill", "payroll", "recurring", "urgent_crypto", "mule_account", "foreign_offshore", "urgent", "crypto", "mule", "account", "foreign", "offshore", "transfer", "first", "time", "payee", "limit", "threshold", "unusual", "hour", "utility", "bill", "payment", "ordinary", "balance", "inquiry" };
        String[] classes = new String[]{ "freeze_transfer", "flag_review", "instant_clear", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("flag_review", new String[]{"first_time_payee", "limit_threshold", "unusual_hour"});
            anchorMap.put("instant_clear", new String[]{"utility_bill", "payroll", "recurring"});
            anchorMap.put("freeze_transfer", new String[]{"urgent_crypto", "mule_account", "foreign_offshore"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "urgent crypto mule account foreign offshore transfer");
        evaluateAndPrint(bundle, "first time payee limit threshold unusual hour");
        evaluateAndPrint(bundle, "recurring payroll utility bill payment");
        evaluateAndPrint(bundle, "ordinary account balance inquiry");
    }

    public static void main(String[] args) {
        new E25_WireTransferFraudGuard().execute();
    }
}