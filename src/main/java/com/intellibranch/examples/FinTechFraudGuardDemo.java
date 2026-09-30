package com.intellibranch.examples;

import com.intellibranch.neurogate.GateTrace;
import com.intellibranch.neurogate.NeuroGate;
import com.intellibranch.routing.DispatchPolicy;

import java.nio.file.Path;
import java.util.List;

/**
 * Real-Time FinTech Remittance Audit & Fraud Prevention.
 * Inspects wire transfer memos to prevent scam losses, chargeback disputes, and AML violations.
 */
public class FinTechFraudGuardDemo {

    public record WireTransaction(String txId, double amount, String sender, String memo) {}

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("  EXAMPLE 4: FINTECH REAL-TIME WIRE MEMO AUDIT & SCAM INTERCEPTION");
        System.out.println("================================================================================");

        Path modelPath = Path.of("weights/demo_fintech.bin");
        NeuroGate gate = NeuroGate.load(modelPath);

        gate.setPolicy(DispatchPolicy.builder()
                .highThreshold(0.70)
                .lowThreshold(0.35)
                .marginCutoff(0.15)
                .maxEntropy(2.0)
                .pipelineThreshold(0.30)
                .build());
        gate.setMinCosineSim(0.30f);

        gate.bind("NormalTransfer", (ctx, payload) -> {
            WireTransaction tx = (WireTransaction) payload;
            System.out.printf("  [INSTANT APPROVAL] Wire %s ($%.2f) approved -> dispatched to Fedwire/ACH%n", tx.txId(), tx.amount());
        }).withAnchor(2.0f, "lunch", "split", "colleagues", "monthly", "payment", "bill", "rent", "dinner");

        gate.bind("PhishingSuspicion", (ctx, payload) -> {
            WireTransaction tx = (WireTransaction) payload;
            System.out.printf("  [INTERCEPT & BLOCK] Wire %s ($%.2f) FROZEN! Suspicious scam keyword pattern! Alerting fraud desk!%n", tx.txId(), tx.amount());
        }).withAnchor(2.2f, "urgent", "police", "fine", "bitcoin", "wallet", "scam", "compromised", "safety");

        gate.bind("ChargebackDispute", (ctx, payload) -> {
            WireTransaction tx = (WireTransaction) payload;
            System.out.printf("  [DISPUTE ROUTE] Wire %s ($%.2f) flagged for duplicate charge arbitration ticket%n", tx.txId(), tx.amount());
        }).withAnchor(2.0f, "dispute", "charged", "three", "times", "single", "card", "unauthorized");

        gate.bind("HighValueAudit", (ctx, payload) -> {
            WireTransaction tx = (WireTransaction) payload;
            System.out.printf("  [AML ESCROW AUDIT] Wire %s ($%.2f) escrow hold placed pending dual-officer signoff%n", tx.txId(), tx.amount());
        }).withAnchor(1.8f, "acquisition", "escrow", "million", "tranche", "corporate", "commercial", "estate");

        gate.ambiguous((ctx, p, s, payload) -> {
            WireTransaction tx = (WireTransaction) payload;
            System.out.printf("  [STEP-UP 2FA] Ambiguous memo (%s vs %s) for wire %s: SMS OTP required from sender %s%n",
                    p, s, tx.txId(), tx.sender());
        }).fallback((ctx, payload) -> {
            WireTransaction tx = (WireTransaction) payload;
            System.out.printf("  [MANUAL REVIEW] Wire %s routed to compliance investigator queue%n", tx.txId());
        });

        List<WireTransaction> transactions = List.of(
                new WireTransaction("TX-9901", 45.00, "Alice", "monthly lunch payment split with office colleagues"),
                new WireTransaction("TX-9902", 4800.00, "Bob", "urgent send funds now police fine wire to bitcoin wallet immediately"),
                new WireTransaction("TX-9903", 14.50, "Carol", "merchant charged my card three times for single coffee"),
                new WireTransaction("TX-9904", 5000000.00, "AcmeCorp", "corporate acquisition escrow settlement tranche wire five million dollars"),
                new WireTransaction("TX-9905", 250.00, "David", "settle dinner bill or dispute charge")
        );

        for (WireTransaction tx : transactions) {
            System.out.printf("%n[Remittance Engine] Auditing Wire: %s | Amount: $%.2f | Memo: \"%s\"%n",
                    tx.txId(), tx.amount(), tx.memo());
            GateTrace trace = gate.inspect(tx.memo());
            long start = System.nanoTime();
            gate.filter(null, tx.memo(), tx);
            long latency = (System.nanoTime() - start) / 1000;
            System.out.printf("  Decision: %s | Confidence: %.2f%% | Cosine: %.4f | Latency: %d μs%n",
                    trace.predictedLabel(), trace.confidence() * 100.0, trace.cosineSimilarity(), latency);
        }
    }
}
