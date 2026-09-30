package com.intellibranch.examples.enterprise.ecommerce;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E63] Automated E-Commerce Return & Fraud Screener
 * Domain: E-Commerce & Retail
 * Evaluates return claims to approve instant refund, require warehouse inspection, or deny return
 */
public class E63_ReturnRefundValidator extends BaseDemo {

    @Override
    public String getId() { return "E63"; }

    @Override
    public String getTitle() { return "Automated E-Commerce Return & Fraud Screener"; }

    @Override
    public String getCategory() { return "E-Commerce & Retail"; }

    @Override
    public String getDescription() { return "Evaluates return claims to approve instant refund, require warehouse inspection, or deny return"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "instant_refund_no_return", "require_physical_inspection", "reject_abusive_return", "fallback", "low_cost_item", "broken_ceramic", "photo_verified", "expensive_macbook", "sealed_box", "serial_check", "wardrobing_worn", "counterfeit_tag", "habitual_returner", "low", "cost", "item", "broken", "ceramic", "photo", "verified", "refund", "expensive", "macbook", "sealed", "box", "serial", "check", "inspection", "wardrobing", "worn", "counterfeit", "tag", "habitual", "returner", "deny", "support", "claim", "ticket", "ping" };
        String[] classes = new String[]{ "instant_refund_no_return", "require_physical_inspection", "reject_abusive_return", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("instant_refund_no_return", new String[]{"low_cost_item", "broken_ceramic", "photo_verified"});
            anchorMap.put("require_physical_inspection", new String[]{"expensive_macbook", "sealed_box", "serial_check"});
            anchorMap.put("reject_abusive_return", new String[]{"wardrobing_worn", "counterfeit_tag", "habitual_returner"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "low cost item broken ceramic photo verified refund");
        evaluateAndPrint(bundle, "expensive macbook sealed box serial check inspection");
        evaluateAndPrint(bundle, "wardrobing worn counterfeit tag habitual returner deny");
        evaluateAndPrint(bundle, "support claim ticket ping");
    }

    public static void main(String[] args) {
        new E63_ReturnRefundValidator().execute();
    }
}