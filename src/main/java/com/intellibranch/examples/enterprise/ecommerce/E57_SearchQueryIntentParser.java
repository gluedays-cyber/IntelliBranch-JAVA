package com.intellibranch.examples.enterprise.ecommerce;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E57] E-Commerce Search Query Buyer Intent Parser
 * Domain: E-Commerce & Retail
 * Distinguishes high-intent purchase searches from browsing or refund support searches
 */
public class E57_SearchQueryIntentParser extends BaseDemo {

    @Override
    public String getId() { return "E57"; }

    @Override
    public String getTitle() { return "E-Commerce Search Query Buyer Intent Parser"; }

    @Override
    public String getCategory() { return "E-Commerce & Retail"; }

    @Override
    public String getDescription() { return "Distinguishes high-intent purchase searches from browsing or refund support searches"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "intent_purchase", "intent_spec_comparison", "intent_after_sales", "fallback", "versus_review", "specs_battery", "comparison_guide", "tracking_delivery", "broken_screen", "return_policy", "buy_now", "best_price", "discount_code", "buy", "now", "best", "price", "discount", "code", "iphone", "versus", "review", "specs", "battery", "comparison", "guide", "laptop", "tracking", "delivery", "broken", "screen", "return", "policy", "order", "random", "alphabet", "string" };
        String[] classes = new String[]{ "intent_purchase", "intent_spec_comparison", "intent_after_sales", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("intent_spec_comparison", new String[]{"versus_review", "specs_battery", "comparison_guide"});
            anchorMap.put("intent_after_sales", new String[]{"tracking_delivery", "broken_screen", "return_policy"});
            anchorMap.put("intent_purchase", new String[]{"buy_now", "best_price", "discount_code"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "buy now best price discount code iphone");
        evaluateAndPrint(bundle, "versus review specs battery comparison guide laptop");
        evaluateAndPrint(bundle, "tracking delivery broken screen return policy order");
        evaluateAndPrint(bundle, "random alphabet string");
    }

    public static void main(String[] args) {
        new E57_SearchQueryIntentParser().execute();
    }
}