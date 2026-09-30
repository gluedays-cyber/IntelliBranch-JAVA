package com.intellibranch.examples.enterprise.ecommerce;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E60] Customer Support Ticket NLP Triage & Escalation
 * Domain: E-Commerce & Retail
 * Classifies support inquiries into VIP Refund, Shipping Delay, or General FAQ tiers
 */
public class E60_CustomerSupportTicketTriage extends BaseDemo {

    @Override
    public String getId() { return "E60"; }

    @Override
    public String getTitle() { return "Customer Support Ticket NLP Triage & Escalation"; }

    @Override
    public String getCategory() { return "E-Commerce & Retail"; }

    @Override
    public String getDescription() { return "Classifies support inquiries into VIP Refund, Shipping Delay, or General FAQ tiers"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "escalate_refund_manager", "logistics_tracker_bot", "faq_automated_reply", "fallback", "change_my_email", "store_hours", "reset_my_pw", "angry_customer", "threaten_lawsuit", "refund_stolen", "where_is_my_package", "tracking_stuck", "fedex_delay", "angry", "customer", "threaten", "lawsuit", "refund", "stolen", "money", "where", "is", "my", "package", "tracking", "stuck", "fedex", "delay", "change", "email", "store", "hours", "reset", "pw", "faq", "blank", "support", "message" };
        String[] classes = new String[]{ "escalate_refund_manager", "logistics_tracker_bot", "faq_automated_reply", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("faq_automated_reply", new String[]{"change_my_email", "store_hours", "reset_my_pw"});
            anchorMap.put("escalate_refund_manager", new String[]{"angry_customer", "threaten_lawsuit", "refund_stolen"});
            anchorMap.put("logistics_tracker_bot", new String[]{"where_is_my_package", "tracking_stuck", "fedex_delay"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "angry customer threaten lawsuit refund stolen money");
        evaluateAndPrint(bundle, "where is my package tracking stuck fedex delay");
        evaluateAndPrint(bundle, "change my email store hours reset my pw faq");
        evaluateAndPrint(bundle, "blank support message");
    }

    public static void main(String[] args) {
        new E60_CustomerSupportTicketTriage().execute();
    }
}