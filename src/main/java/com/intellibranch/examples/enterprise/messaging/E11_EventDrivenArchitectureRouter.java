package com.intellibranch.examples.enterprise.messaging;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E11] EDA Domain Event Fan-Out Router
 * Domain: Messaging & Event Streaming
 * Fans out domain events (OrderCreated, InventoryReserved, PaymentCaptured) to reactive listeners
 */
public class E11_EventDrivenArchitectureRouter extends BaseDemo {

    @Override
    public String getId() { return "E11"; }

    @Override
    public String getTitle() { return "EDA Domain Event Fan-Out Router"; }

    @Override
    public String getCategory() { return "Messaging & Event Streaming"; }

    @Override
    public String getDescription() { return "Fans out domain events (OrderCreated, InventoryReserved, PaymentCaptured) to reactive listeners"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "fulfillment_stream", "notification_stream", "ledger_stream", "fallback", "order_placed", "box", "shipping", "send_sms", "push_notification", "email", "credit", "debit", "balance", "order", "placed", "packing", "send", "sms", "push", "notification", "to", "user", "settlement", "unclassified", "event", "hook" };
        String[] classes = new String[]{ "fulfillment_stream", "notification_stream", "ledger_stream", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("fulfillment_stream", new String[]{"order_placed", "box", "shipping"});
            anchorMap.put("notification_stream", new String[]{"send_sms", "push_notification", "email"});
            anchorMap.put("ledger_stream", new String[]{"credit", "debit", "balance"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "shipping order placed packing box");
        evaluateAndPrint(bundle, "send sms push notification to user");
        evaluateAndPrint(bundle, "credit debit balance settlement");
        evaluateAndPrint(bundle, "unclassified event hook");
    }

    public static void main(String[] args) {
        new E11_EventDrivenArchitectureRouter().execute();
    }
}