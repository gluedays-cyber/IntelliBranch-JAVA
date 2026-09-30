package com.intellibranch.examples.enterprise.ecommerce;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E58] Collaborative Real-Time RecSys Filter
 * Domain: E-Commerce & Retail
 * Directs shopper cart contents to Cross-Sell, Up-Sell, or Clearance recommendation models
 */
public class E58_ProductRecommendationFilter extends BaseDemo {

    @Override
    public String getId() { return "E58"; }

    @Override
    public String getTitle() { return "Collaborative Real-Time RecSys Filter"; }

    @Override
    public String getCategory() { return "E-Commerce & Retail"; }

    @Override
    public String getDescription() { return "Directs shopper cart contents to Cross-Sell, Up-Sell, or Clearance recommendation models"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "cross_sell_accessories", "up_sell_premium_model", "clearance_bundle", "fallback", "seasonal_stock", "winter_jacket", "clearance_discount", "viewing_budget_tv", "show_oled", "better_sound", "bought_camera", "needs_lens", "sd_card", "bought", "camera", "needs", "lens", "sd", "card", "trip", "accessory", "viewing", "budget", "tv", "show", "oled", "better", "sound", "quality", "seasonal", "stock", "winter", "jacket", "clearance", "discount", "sale", "unrelated", "cart", "item" };
        String[] classes = new String[]{ "cross_sell_accessories", "up_sell_premium_model", "clearance_bundle", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("clearance_bundle", new String[]{"seasonal_stock", "winter_jacket", "clearance_discount"});
            anchorMap.put("up_sell_premium_model", new String[]{"viewing_budget_tv", "show_oled", "better_sound"});
            anchorMap.put("cross_sell_accessories", new String[]{"bought_camera", "needs_lens", "sd_card"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "bought camera needs lens sd card trip accessory");
        evaluateAndPrint(bundle, "viewing budget tv show oled better sound quality");
        evaluateAndPrint(bundle, "seasonal stock winter jacket clearance discount sale");
        evaluateAndPrint(bundle, "unrelated cart item");
    }

    public static void main(String[] args) {
        new E58_ProductRecommendationFilter().execute();
    }
}