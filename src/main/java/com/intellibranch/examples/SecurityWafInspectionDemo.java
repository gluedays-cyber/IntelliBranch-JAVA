package com.intellibranch.examples;

import com.intellibranch.core.InferenceModel;
import com.intellibranch.neurogate.GateTrace;
import com.intellibranch.neurogate.NeuroGate;
import com.intellibranch.routing.DispatchPolicy;
import com.intellibranch.training.DataSample;
import com.intellibranch.training.TrainConfig;
import com.intellibranch.training.Trainer;

import java.util.List;

/**
 * Microsecond API Gateway & WAF Security Payload Inspection Demo.
 * Classifies HTTP parameters and payloads to block SQLi, RCE, and Path Traversal attacks.
 */
public class SecurityWafInspectionDemo {

    public record HttpRequestPayload(String clientIp, String endpoint, String rawParam) {}

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("  EXAMPLE 5: MICROSECOND WAF & API GATEWAY PAYLOAD SECURITY INSPECTOR");
        System.out.println("================================================================================");

        List<DataSample> dataset = List.of(
                new DataSample("select username password from users where id equals 1", "SQLInjection"),
                new DataSample("union select null null table name from information schema", "SQLInjection"),
                new DataSample("drop table customers or 1 equals 1 comment", "SQLInjection"),
                new DataSample("cat etc passwd dot dot slash etc shadow", "PathTraversal"),
                new DataSample("directory traversal dot dot slash windows system32 cmd", "PathTraversal"),
                new DataSample("curl http attacker com pipe bash remote execution", "CommandInjection"),
                new DataSample("semicolon rm dash rf slash bin sh spawn reverse shell", "CommandInjection"),
                new DataSample("get user profile information by account id 4920", "NormalTraffic"),
                new DataSample("update delivery address street number apartment 4b", "NormalTraffic"),
                new DataSample("query transaction history for month of september", "NormalTraffic")
        );

        TrainConfig cfg = TrainConfig.defaultConfig()
                .setEpochs(60)
                .setLearningRate(0.005f)
                .setEmbeddingDim(32)
                .setHiddenDim(32)
                .setTargetVocabSize(80);

        System.out.println("Training embedded WAF classifier in memory (1.2 seconds)...");
        InferenceModel model = Trainer.trainModel(dataset, cfg);
        NeuroGate gate = new NeuroGate(model);

        gate.setPolicy(DispatchPolicy.builder()
                .highThreshold(0.65)
                .lowThreshold(0.35)
                .marginCutoff(0.10)
                .maxEntropy(2.2)
                .build());

        gate.bind("SQLInjection", (ctx, payload) -> {
            HttpRequestPayload req = (HttpRequestPayload) payload;
            System.out.printf("  [SECURITY ALERT: SQLi] BLOCKED IP %s | Potential database injection attempt%n", req.clientIp());
        }).withAnchor(2.0f, "select", "union", "drop", "table", "from", "users", "schema");

        gate.bind("PathTraversal", (ctx, payload) -> {
            HttpRequestPayload req = (HttpRequestPayload) payload;
            System.out.printf("  [SECURITY ALERT: LFI] BLOCKED IP %s | Unauthorized file path traversal attempt%n", req.clientIp());
        }).withAnchor(2.0f, "passwd", "shadow", "system32", "etc", "dot", "slash");

        gate.bind("CommandInjection", (ctx, payload) -> {
            HttpRequestPayload req = (HttpRequestPayload) payload;
            System.out.printf("  [SECURITY ALERT: RCE] BLOCKED IP %s | Arbitrary shell command execution attempt%n", req.clientIp());
        }).withAnchor(2.0f, "curl", "bash", "shell", "spawn", "pipe", "rm");

        gate.bind("NormalTraffic", (ctx, payload) -> {
            HttpRequestPayload req = (HttpRequestPayload) payload;
            System.out.printf("  [TRAFFIC ALLOWED] Forwarding request from IP %s to downstream microservice%n", req.clientIp());
        }).withAnchor(1.5f, "user", "profile", "account", "update", "delivery", "transaction");

        gate.fallback((ctx, payload) -> {
            HttpRequestPayload req = (HttpRequestPayload) payload;
            System.out.printf("  [WAF HONEYPOT] Suspicious anomaly from IP %s routed to rate-limited sandbox%n", req.clientIp());
        });

        List<HttpRequestPayload> incomingRequests = List.of(
                new HttpRequestPayload("192.168.1.50", "/api/user", "get user profile information by account id 4920"),
                new HttpRequestPayload("45.33.32.156", "/api/search", "union select null table name from users"),
                new HttpRequestPayload("185.220.101.5", "/api/download", "cat etc passwd dot dot slash etc shadow"),
                new HttpRequestPayload("103.251.167.20", "/api/exec", "curl http attacker com pipe bash"),
                new HttpRequestPayload("192.168.1.88", "/api/order", "update delivery address street number apartment 4b"),
                new HttpRequestPayload("218.92.0.11", "/api/test", "weird binary exploit probe 0x900x900x90")
        );

        for (HttpRequestPayload req : incomingRequests) {
            System.out.printf("%n[Gateway Edge] Inspecting %s from %s | Param: \"%s\"%n", req.endpoint(), req.clientIp(), req.rawParam());
            GateTrace trace = gate.inspect(req.rawParam());
            long start = System.nanoTime();
            gate.filter(null, req.rawParam(), req);
            long latency = (System.nanoTime() - start) / 1000;
            System.out.printf("  Inspection: %s (Confidence: %.2f%%, Latency: %d μs, Threat: %b)%n",
                    trace.predictedLabel(), trace.confidence() * 100.0, latency, !"NormalTraffic".equals(trace.predictedLabel()));
        }
    }
}
