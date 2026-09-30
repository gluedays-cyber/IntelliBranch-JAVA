package com.intellibranch.examples;

import com.intellibranch.core.InferenceModel;
import com.intellibranch.neurogate.GateTrace;
import com.intellibranch.neurogate.NeuroGate;
import com.intellibranch.routing.DispatchPolicy;
import com.intellibranch.training.DataSample;
import com.intellibranch.training.TrainConfig;
import com.intellibranch.training.Trainer;

import java.nio.file.Path;
import java.util.List;

/**
 * Multi-Tenant Hierarchical Cascading Routers.
 * Demonstrates how enterprise SaaS platforms compose lightweight micro-models:
 * Tier 1: Organization/Tenant Router -> Tier 2: Specialized Domain NeuroGate.
 */
public class MultiTenantCascadingDemo {

    public record TenantRequest(String orgId, String message) {}

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("  EXAMPLE 7: MULTI-TENANT HIERARCHICAL CASCADING NEURAL ROUTERS");
        System.out.println("================================================================================");

        // Tier 1: Gateway Domain Classifier
        List<DataSample> tier1Dataset = List.of(
                new DataSample("order return refund item purchase money back", "EcommerceTenant"),
                new DataSample("where is package tracking courier delivery shipment", "EcommerceTenant"),
                new DataSample("wire transfer check balance checking account savings", "FintechTenant"),
                new DataSample("freeze debit card credit bill mortgage aml transaction", "FintechTenant"),
                new DataSample("fatal crash container killed code 137 server out of memory oom pod failure", "InfrastructureTenant"),
                new DataSample("database connection pool timeout postgres error hikari", "InfrastructureTenant")
        );

        System.out.println("Compiling Tier-1 Global Tenant Dispatcher (1.1 seconds)...");
        InferenceModel tier1Model = Trainer.trainModel(tier1Dataset, TrainConfig.defaultConfig().setEpochs(50).setTargetVocabSize(80));
        NeuroGate tier1Gate = new NeuroGate(tier1Model);
        tier1Gate.setMinCosineSim(0.10f);

        // Tier 2: Domain NeuroGates
        System.out.println("Loading Tier-2 Specialized Tenant Routers...");
        NeuroGate ecomGate = NeuroGate.load(Path.of("weights/demo_cs.bin"));
        NeuroGate fintechGate = NeuroGate.load(Path.of("weights/demo_fintech.bin"));
        NeuroGate infraGate = NeuroGate.load(Path.of("weights/demo_sre.bin"));

        ecomGate.bind("Refund", (ctx, payload) -> System.out.println("    [E-Commerce Sub-Router] Processed Customer Refund"));
        ecomGate.bind("Delivery", (ctx, payload) -> System.out.println("    [E-Commerce Sub-Router] Tracked Customer Package"));

        fintechGate.bind("NormalTransfer", (ctx, payload) -> System.out.println("    [FinTech Sub-Router] Dispatched ACH Transfer"));
        fintechGate.bind("PhishingSuspicion", (ctx, payload) -> System.out.println("    [FinTech Sub-Router] Blocked Scam Remittance!"));

        infraGate.bind("OutOfMemory", (ctx, payload) -> System.out.println("    [Infra Sub-Router] Triggered K8s Pod Restart"));
        infraGate.bind("DBPoolExhausted", (ctx, payload) -> System.out.println("    [Infra Sub-Router] Expanded Connection Pool"));

        // Wire Tier-1 to Tier-2
        tier1Gate.setPolicy(DispatchPolicy.builder()
                .highThreshold(0.50)
                .lowThreshold(0.25)
                .marginCutoff(0.10)
                .maxEntropy(2.5)
                .build());

        tier1Gate.bind("EcommerceTenant", (ctx, payload) -> {
            TenantRequest req = (TenantRequest) payload;
            System.out.printf("  [Tier-1 Gateway] Routed to E-Commerce Tenant Engine%n");
            ecomGate.filter(null, req.message(), req);
        }).withAnchor(2.0f, "refund", "return", "item", "purchase", "money", "package", "courier", "delivery");

        tier1Gate.bind("FintechTenant", (ctx, payload) -> {
            TenantRequest req = (TenantRequest) payload;
            System.out.printf("  [Tier-1 Gateway] Routed to FinTech Tenant Engine%n");
            fintechGate.filter(null, req.message(), req);
        }).withAnchor(2.0f, "wire", "transfer", "balance", "checking", "savings", "freeze", "card", "safety", "bitcoin");

        tier1Gate.bind("InfrastructureTenant", (ctx, payload) -> {
            TenantRequest req = (TenantRequest) payload;
            System.out.printf("  [Tier-1 Gateway] Routed to Infrastructure Tenant Engine%n");
            infraGate.filter(null, req.message(), req);
        }).withAnchor(2.0f, "crash", "container", "code", "137", "memory", "database", "pool", "timeout", "fatal");

        tier1Gate.fallback((ctx, payload) -> {
            System.out.println("  [Tier-1 Gateway] Unrecognized tenant domain -> Routed to Global Support");
        });

        List<TenantRequest> incomingEvents = List.of(
                new TenantRequest("client-abc", "please refund my purchase and return item money"),
                new TenantRequest("client-xyz", "urgent send wire to safety bitcoin wallet immediately"),
                new TenantRequest("client-cloud", "fatal crash container killed code 137 out of memory"),
                new TenantRequest("client-unknown", "unrecognized arbitrary alien language payload")
        );

        for (TenantRequest req : incomingEvents) {
            System.out.printf("%n[Global Gateway] Incoming Event from %s: \"%s\"%n", req.orgId(), req.message());
            long start = System.nanoTime();
            tier1Gate.filter(null, req.message(), req);
            long latency = (System.nanoTime() - start) / 1000;
            System.out.printf("  Total Hierarchical Latency: %d μs (2-Tier Neural Routing)%n", latency);
        }
    }
}
