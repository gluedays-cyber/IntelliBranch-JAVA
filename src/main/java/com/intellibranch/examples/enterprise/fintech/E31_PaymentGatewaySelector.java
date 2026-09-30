package com.intellibranch.examples.enterprise.fintech;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E31] Dynamic PSP Gateway Cost & Uptime Router
 * Domain: FinTech & Payments
 * Routes credit card authorizations across Stripe, Adyen, and Checkout.com for optimal fees
 */
public class E31_PaymentGatewaySelector extends BaseDemo {

    @Override
    public String getId() { return "E31"; }

    @Override
    public String getTitle() { return "Dynamic PSP Gateway Cost & Uptime Router"; }

    @Override
    public String getCategory() { return "FinTech & Payments"; }

    @Override
    public String getDescription() { return "Routes credit card authorizations across Stripe, Adyen, and Checkout.com for optimal fees"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "route_stripe", "route_adyen", "route_checkout_com", "fallback", "mena_region", "mada", "local_card", "eu_ideal", "sepa", "interchange_plus", "us_domestic", "apple_pay", "stripe_radar", "us", "domestic", "apple", "pay", "stripe", "radar", "customer", "eu", "ideal", "interchange", "plus", "transaction", "mena", "region", "local", "card", "checkout", "unsupported", "currency" };
        String[] classes = new String[]{ "route_stripe", "route_adyen", "route_checkout_com", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("route_checkout_com", new String[]{"mena_region", "mada", "local_card"});
            anchorMap.put("route_adyen", new String[]{"eu_ideal", "sepa", "interchange_plus"});
            anchorMap.put("route_stripe", new String[]{"us_domestic", "apple_pay", "stripe_radar"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "us domestic apple pay stripe radar customer");
        evaluateAndPrint(bundle, "eu ideal sepa interchange plus transaction");
        evaluateAndPrint(bundle, "mena region mada local card checkout");
        evaluateAndPrint(bundle, "unsupported currency card");
    }

    public static void main(String[] args) {
        new E31_PaymentGatewaySelector().execute();
    }
}