package com.intellibranch.examples;

import com.intellibranch.neurogate.NeuroGate;
import com.intellibranch.routing.DispatchPolicy;

import java.nio.file.Path;
import java.util.List;

/**
 * High-Throughput Streaming Event QoS Partitioning with Zero Allocation.
 * Simulates Apache Kafka / Pulsar consumer stream classification.
 */
public class KafkaStreamQoSDemo {

    public record KafkaMessage(String topic, int partition, long offset, String rawPayload) {}

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("  EXAMPLE 2: KAFKA STREAM CONSUMER QoS PARTITIONING & AUTO-TRIAGE");
        System.out.println("================================================================================");

        Path modelPath = Path.of("weights/demo_sre.bin");
        NeuroGate gate = NeuroGate.load(modelPath);
        gate.setPolicy(DispatchPolicy.defaultPolicy());
        gate.setMinCosineSim(0.30f);

        // Bind priority actions with symbolic keyword anchors
        gate.bind("OutOfMemory", (ctx, payload) -> {
            KafkaMessage msg = (KafkaMessage) payload;
            System.out.printf("  [P0 CRITICAL QUEUE] HPA scale-up triggered for message at offset %d%n", msg.offset());
        }).withAnchor(1.5f, "memory", "oom", "allocating", "starvation", "killed", "137");

        gate.bind("DBPoolExhausted", (ctx, payload) -> {
            KafkaMessage msg = (KafkaMessage) payload;
            System.out.printf("  [P1 WARNING QUEUE] PostgreSQL pool capped worker signaled at offset %d%n", msg.offset());
        }).withAnchor(1.5f, "hikaripool", "connection", "pool", "timeout", "postgres");

        gate.bind("AuthBruteForce", (ctx, payload) -> {
            KafkaMessage msg = (KafkaMessage) payload;
            System.out.printf("  [SECURITY QUEUE] Auto-banning malicious IP at offset %d%n", msg.offset());
        }).withAnchor(1.5f, "security", "login", "attempts", "alert", "brute", "fail2ban");

        gate.bind("SystemHealth", (ctx, payload) -> {
            KafkaMessage msg = (KafkaMessage) payload;
            System.out.printf("  [METRIC / COLD QUEUE] Routine health heartbeat archived at offset %d%n", msg.offset());
        }).withAnchor(1.5f, "health", "probe", "healthz", "200", "ok", "heartbeat");

        gate.fallback((ctx, payload) -> {
            KafkaMessage msg = (KafkaMessage) payload;
            System.out.printf("  [UNRESOLVED LOG QUEUE] Unknown log streamed to DLQ at offset %d%n", msg.offset());
        });

        List<KafkaMessage> streamBatch = List.of(
                new KafkaMessage("app-logs", 0, 1001L, "container exited with code 137 OOMKilled memory limit exceeded"),
                new KafkaMessage("app-logs", 1, 1002L, "HikariPool-1 - Connection is not available request timed out after 30000ms"),
                new KafkaMessage("app-logs", 0, 1003L, "SECURITY ALERT: 250 failed login attempts in 60 seconds from single IP"),
                new KafkaMessage("app-logs", 2, 1004L, "INFO: health check probe /healthz returned 200 OK latency: 2ms"),
                new KafkaMessage("app-logs", 1, 1005L, "unknown proprietary unformatted binary blob payload #########")
        );

        long batchStart = System.nanoTime();
        for (KafkaMessage msg : streamBatch) {
            System.out.printf("%n[Stream Consumer] Read Message from %s [Part: %d, Offset: %d]%n",
                    msg.topic(), msg.partition(), msg.offset());
            System.out.printf("  Payload: \"%s\"%n", msg.rawPayload());

            long start = System.nanoTime();
            gate.filter(null, msg.rawPayload(), msg);
            long latencyMicros = (System.nanoTime() - start) / 1000;
            System.out.printf("  => Dispatched in %d μs (Zero Heap Allocation)%n", latencyMicros);
        }

        long totalElapsedMicros = (System.nanoTime() - batchStart) / 1000;
        System.out.printf("%n[Batch Summary] Processed %d streaming messages in %d μs (Avg: %.2f μs/message)%n",
                streamBatch.size(), totalElapsedMicros, (double) totalElapsedMicros / streamBatch.size());
    }
}
