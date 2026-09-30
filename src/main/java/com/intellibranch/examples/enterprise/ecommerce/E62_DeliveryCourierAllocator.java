package com.intellibranch.examples.enterprise.ecommerce;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E62] Last-Mile Delivery Carrier Allocation Router
 * Domain: E-Commerce & Retail
 * Selects optimal courier (FedEx, UPS, Local Same-Day Courier) based on postal code urgency
 */
public class E62_DeliveryCourierAllocator extends BaseDemo {

    @Override
    public String getId() { return "E62"; }

    @Override
    public String getTitle() { return "Last-Mile Delivery Carrier Allocation Router"; }

    @Override
    public String getCategory() { return "E-Commerce & Retail"; }

    @Override
    public String getDescription() { return "Selects optimal courier (FedEx, UPS, Local Same-Day Courier) based on postal code urgency"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "courier_sameday_bike", "courier_fedex_overnight", "courier_ground_standard", "fallback", "downtown_urgent", "2hr_delivery", "perishable_grocery", "heavy_furniture", "bulk_box", "5_day_standard", "high_value_jewelry", "express_air", "signature_req", "downtown", "urgent", "2hr", "delivery", "perishable", "grocery", "order", "high", "value", "jewelry", "express", "air", "signature", "req", "shipment", "heavy", "furniture", "bulk", "box", "5", "day", "standard", "freight", "invalid", "address", "format" };
        String[] classes = new String[]{ "courier_sameday_bike", "courier_fedex_overnight", "courier_ground_standard", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("courier_sameday_bike", new String[]{"downtown_urgent", "2hr_delivery", "perishable_grocery"});
            anchorMap.put("courier_ground_standard", new String[]{"heavy_furniture", "bulk_box", "5_day_standard"});
            anchorMap.put("courier_fedex_overnight", new String[]{"high_value_jewelry", "express_air", "signature_req"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "downtown urgent 2hr delivery perishable grocery order");
        evaluateAndPrint(bundle, "high value jewelry express air signature req shipment");
        evaluateAndPrint(bundle, "heavy furniture bulk box 5 day standard freight");
        evaluateAndPrint(bundle, "invalid address format");
    }

    public static void main(String[] args) {
        new E62_DeliveryCourierAllocator().execute();
    }
}