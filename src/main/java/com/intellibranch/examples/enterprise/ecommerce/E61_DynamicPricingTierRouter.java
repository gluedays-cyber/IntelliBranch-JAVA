package com.intellibranch.examples.enterprise.ecommerce;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E61] Dynamic Surge Pricing & Loyalty Discount Router
 * Domain: E-Commerce & Retail
 * Routes shopper checkout sessions to surge pricing, loyalty discounts, or promotional tiers
 */
public class E61_DynamicPricingTierRouter extends BaseDemo {

    @Override
    public String getId() { return "E61"; }

    @Override
    public String getTitle() { return "Dynamic Surge Pricing & Loyalty Discount Router"; }

    @Override
    public String getCategory() { return "E-Commerce & Retail"; }

    @Override
    public String getDescription() { return "Routes shopper checkout sessions to surge pricing, loyalty discounts, or promotional tiers"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "apply_loyalty_discount", "surge_high_demand", "standard_msrp", "fallback", "concert_tickets", "last_seat", "uber_surge", "routine_grocery", "regular_shelf_item", "fixed_price", "vip_platinum", "anniversary_member", "10_year_loyalty", "vip", "platinum", "anniversary", "member", "10", "year", "loyalty", "discount", "concert", "tickets", "last", "seat", "uber", "surge", "high", "demand", "routine", "grocery", "regular", "shelf", "item", "fixed", "price", "unknown", "checkout", "token" };
        String[] classes = new String[]{ "apply_loyalty_discount", "surge_high_demand", "standard_msrp", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("surge_high_demand", new String[]{"concert_tickets", "last_seat", "uber_surge"});
            anchorMap.put("standard_msrp", new String[]{"routine_grocery", "regular_shelf_item", "fixed_price"});
            anchorMap.put("apply_loyalty_discount", new String[]{"vip_platinum", "anniversary_member", "10_year_loyalty"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "vip platinum anniversary member 10 year loyalty discount");
        evaluateAndPrint(bundle, "concert tickets last seat uber surge high demand");
        evaluateAndPrint(bundle, "routine grocery regular shelf item fixed price");
        evaluateAndPrint(bundle, "unknown checkout token");
    }

    public static void main(String[] args) {
        new E61_DynamicPricingTierRouter().execute();
    }
}