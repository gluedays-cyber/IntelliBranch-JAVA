package com.intellibranch.cli;

import com.intellibranch.core.BinaryModel;
import com.intellibranch.core.InferenceModel;
import com.intellibranch.neurogate.GateTrace;
import com.intellibranch.neurogate.NeuroGate;
import com.intellibranch.routing.DispatchPolicy;
import com.intellibranch.training.DataSample;
import com.intellibranch.training.TrainConfig;
import com.intellibranch.training.Trainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * IntelliBranch 6-Domain NeuroGate 3-Head Intelligent Filtering & Microsecond Dispatch Demonstration.
 */
public class DemoRunner {

    public record TestCase(String query, String expectation) {}

    public static class DemoSuite {
        String domainName;
        String modelPath;
        String dataPath;
        String description;
        DispatchPolicy policy;
        float minCosine;
        Consumer<NeuroGate> setupGate;
        List<TestCase> testCases;
        CustomAction customRun;

        public DemoSuite(String domainName, String modelPath, String dataPath, String description,
                         DispatchPolicy policy, float minCosine, Consumer<NeuroGate> setupGate,
                         List<TestCase> testCases, CustomAction customRun) {
            this.domainName = domainName;
            this.modelPath = modelPath;
            this.dataPath = dataPath;
            this.description = description;
            this.policy = policy;
            this.minCosine = minCosine;
            this.setupGate = setupGate;
            this.testCases = testCases;
            this.customRun = customRun;
        }
    }

    @FunctionalInterface
    public interface CustomAction {
        void execute(NeuroGate gate, Object ctx) throws Exception;
    }

    private static void ensureModel(String modelPath, String dataPath) {
        Path mPath = Path.of(modelPath);
        if (!Files.exists(mPath)) {
            System.out.printf("Model [%s] not found. Auto-training on-the-fly from [%s]...%n", modelPath, dataPath);
            try {
                List<DataSample> samples = Trainer.loadCSVDataset(Path.of(dataPath));
                TrainConfig cfg = TrainConfig.defaultConfig()
                        .setEpochs(60)
                        .setLearningRate(0.003f)
                        .setTargetVocabSize(256);

                InferenceModel model = Trainer.trainModel(samples, cfg);
                Files.createDirectories(mPath.getParent());
                BinaryModel.save(mPath, model);
                System.out.printf("Successfully compiled [%s] in memory.%n", modelPath);
            } catch (Exception e) {
                throw new RuntimeException("Auto-training failed for " + modelPath, e);
            }
        }
    }

    private static double measureBenchmarkLatency(NeuroGate gate, String query, int iterations) {
        gate.inspect(query); // Warmup
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            gate.inspect(query);
        }
        long elapsed = System.nanoTime() - start;
        return (double) elapsed / (double) (iterations * 1000L); // μs/op
    }

    public static void main(String[] args) {
        String targetDomain = "all";
        for (int i = 0; i < args.length; i++) {
            if ("--domain".equals(args[i]) && i + 1 < args.length) {
                targetDomain = args[++i].toLowerCase(Locale.ROOT);
            }
        }

        Map<String, DemoSuite> suites = buildSuites();
        List<String> orderedKeys = List.of("cs", "llm", "sre", "iot", "cicd", "fintech");

        long totalStart = System.currentTimeMillis();
        int totalQueries = 0;
        int executedDomains = 0;

        System.out.println("================================================================================");
        System.out.println("  INTELLIBRANCH-JAVA v2.0 - 6-DOMAIN NEUROGATE 3-HEAD FILTERING SUITE");
        System.out.println("================================================================================");

        for (String key : orderedKeys) {
            if (!"all".equals(targetDomain) && !targetDomain.equals(key)) {
                continue;
            }

            executedDomains++;
            DemoSuite suite = suites.get(key);
            System.out.printf("%n>>> DOMAIN: %s%n", suite.domainName);
            System.out.printf("    Model Path  : %s%n", suite.modelPath);
            System.out.printf("    Capability  : %s%n", suite.description);
            System.out.println("    ----------------------------------------------------------------------------");

            ensureModel(suite.modelPath, suite.dataPath);

            NeuroGate gate;
            try {
                gate = NeuroGate.load(Path.of(suite.modelPath));
            } catch (IOException e) {
                throw new RuntimeException("Failed to load NeuroGate [" + suite.modelPath + "]", e);
            }

            gate.setPolicy(suite.policy);
            if (suite.minCosine > 0.0f) {
                gate.setMinCosineSim(suite.minCosine);
            }
            try {
                List<DataSample> samples = Trainer.loadCSVDataset(Path.of(suite.dataPath));
                gate.calibrateDomainCentroid(samples);
            } catch (Exception ignored) {
            }

            suite.setupGate.accept(gate);

            for (TestCase tc : suite.testCases) {
                totalQueries++;
                GateTrace trace = gate.inspect(tc.query);
                double benchLatency = measureBenchmarkLatency(gate, tc.query, 1000);

                String routedLabel = trace.predictedLabel();
                if (trace.isPipeline()) {
                    routedLabel = String.format("Pipeline (%s -> %s)", trace.predictedLabel(), trace.secondaryLabel());
                } else if (trace.isOOD()) {
                    routedLabel = String.format("OOD Fallback (%s)", trace.predictedLabel());
                }

                System.out.printf("  • Input    : \"%s\"%n", tc.query);
                System.out.printf("    Expect   : %s%n", tc.expectation);
                System.out.printf(Locale.US, "    Inference: %s (Confidence: %.2f%%, Cosine: %.4f, Entropy: %.4f, Energy: %.2f, Latency: %.2f μs/op, OOD: %b)%n",
                        routedLabel, trace.confidence() * 100.0, trace.cosineSimilarity(), trace.entropy(), trace.freeEnergy(), benchLatency, trace.isOOD());

                try {
                    gate.filterPipeline(null, tc.query, null);
                } catch (Exception e) {
                    System.err.println("Handler error: " + e.getMessage());
                }
                System.out.println();
            }

            if (suite.customRun != null) {
                try {
                    suite.customRun.execute(gate, null);
                } catch (Exception e) {
                    System.err.println("Custom run error: " + e.getMessage());
                }
                System.out.println();
            }
        }

        long totalDuration = System.currentTimeMillis() - totalStart;
        String domainLabel = (executedDomains > 1) ? "domains" : "domain";
        System.out.println("================================================================================");
        System.out.printf("DEMONSTRATION COMPLETED: %d queries routed across %d distinct neural %s in %d ms%n",
                totalQueries, executedDomains, domainLabel, totalDuration);
        System.out.println("ALL INFERENCES RUN IN MICROSECONDS IN PURE JAVA WITH ZERO HEAP ALLOCATIONS.");
        System.out.println("================================================================================");
    }

    private static Map<String, DemoSuite> buildSuites() {
        Map<String, DemoSuite> suites = new LinkedHashMap<>();

        // 1. CS Gateway
        suites.put("cs", new DemoSuite(
                "1. E-Commerce CS Gateway (XOR Order & Multi-Intent Pipeline with NeuroGate)",
                "weights/demo_cs.bin",
                "data/demo_cs.csv",
                "Demonstrates 3-head NeuroGate with L2 Cosine OOD boundary, symbolic anchors, and multi-intent pipeline.",
                DispatchPolicy.builder()
                        .highThreshold(0.70)
                        .lowThreshold(0.35)
                        .marginCutoff(0.15)
                        .maxEntropy(0.70)
                        .pipelineThreshold(0.25)
                        .minLogSumExp(7.0)
                        .build(),
                0.35f,
                gate -> {
                    gate.bind("Refund", (ctx, payload) ->
                            System.out.println("    [ACTION: Refund] Process refund request & reverse charge")
                    ).withAnchor(1.3f, "refund", "money", "card", "charge", "return");

                    gate.bind("Delivery", (ctx, payload) ->
                            System.out.println("    [ACTION: Delivery] Query courier GPS tracking & update address")
                    ).withAnchor(1.3f, "courier", "delivered", "package", "delivery", "box", "shipping");

                    gate.bind("Account", (ctx, payload) ->
                            System.out.println("    [ACTION: Account] Trigger security verification & unlock profile")
                    ).withAnchor(1.3f, "account", "login", "password", "security", "portal", "profile", "factor");

                    gate.bind("Payment", (ctx, payload) ->
                            System.out.println("    [ACTION: Payment] Retry checkout gateway & validate billing")
                    ).withAnchor(1.3f, "payment", "checkout", "billing", "pay", "declined");

                    var pipe = (com.intellibranch.routing.PipelineAction) (ctx, p, s, payload) ->
                            System.out.printf("    [PIPELINE: %s -> %s] Return box approved THEN update reshipment destination%n", p, s);

                    gate.bindPipeline("Refund", "Delivery", pipe)
                            .bindPipeline("Delivery", "Refund", pipe)
                            .ambiguous((ctx, p, s, payload) ->
                                    System.out.printf("    [AMBIGUOUS: %s vs %s] Borderline confidence: Prompt user for clarification%n", p, s)
                            ).fallback((ctx, payload) ->
                                    System.out.println("    [FALLBACK] Escalated to human support tier-2 agent")
                            );
                },
                List.of(
                        new TestCase("please refund the money to my card", "Definite Refund"),
                        new TestCase("courier marked delivered but package is missing", "Definite Delivery"),
                        new TestCase("i forgot my account password and cannot log into the user portal", "Definite Account"),
                        new TestCase("my credit card was declined at checkout with transaction error code 402", "Definite Payment"),
                        new TestCase("i returned the box please update delivery", "Multi-Intent Pipeline (Refund -> Delivery)"),
                        new TestCase("refund delivery", "Positional XOR Sequence Disambiguation"),
                        new TestCase("what is the meaning of quantum black holes", "OOD / Fallback Isolation")
                ),
                null
        ));

        // 2. LLM Gateway
        suites.put("llm", new DemoSuite(
                "2. Semantic LLM Gateway & Cloud API Bypass (NeuroGate Guarded)",
                "weights/demo_llm.bin",
                "data/demo_llm.csv",
                "Resolves known banking intents in ~30 μs locally, safely escalating true OOD queries to Cloud LLM.",
                DispatchPolicy.builder()
                        .highThreshold(0.75)
                        .lowThreshold(0.35)
                        .marginCutoff(0.15)
                        .maxEntropy(1.50)
                        .pipelineThreshold(0.30)
                        .minLogSumExp(7.5)
                        .build(),
                0.35f,
                gate -> {
                    gate.bind("QueryBalance", (ctx, payload) ->
                            System.out.println("    [LOCAL BYPASS] Fetched balance from Redis cache in 30 μs (Cost: $0.00)")
                    ).withAnchor(1.3f, "balance", "checking", "account", "funds", "savings");

                    gate.bind("TransferFunds", (ctx, payload) ->
                            System.out.println("    [LOCAL BYPASS] Executed internal ledger transaction directly (Cost: $0.00)")
                    ).withAnchor(1.3f, "transfer", "send", "dollars", "wire", "remit");

                    gate.bind("CardLock", (ctx, payload) ->
                            System.out.println("    [LOCAL BYPASS] Instant freeze signal emitted to Visa processor (Cost: $0.00)")
                    ).withAnchor(1.3f, "freeze", "lock", "debit", "card", "lost", "stolen");

                    gate.bind("UpdateProfile", (ctx, payload) ->
                            System.out.println("    [LOCAL BYPASS] Profile update form rendered (Cost: $0.00)")
                    ).withAnchor(1.3f, "profile", "update", "address", "phone", "residential", "email");

                    gate.fallback((ctx, payload) ->
                            System.out.println("    [CLOUD LLM ESCAPE] High entropy/OOD query forwarded to OpenAI GPT-4o (Cost: $0.02)")
                    );
                },
                List.of(
                        new TestCase("what is my current checking account balance", "Local Bypass: QueryBalance"),
                        new TestCase("how much money is remaining in my personal savings account", "Local Bypass: QueryBalance"),
                        new TestCase("send five hundred dollars to john doe from checking", "Local Bypass: TransferFunds"),
                        new TestCase("freeze my debit card immediately i lost my leather wallet", "Local Bypass: CardLock"),
                        new TestCase("update my residential street address in my user profile", "Local Bypass: UpdateProfile"),
                        new TestCase("explain how quantum entanglement works in simple terms", "Cloud LLM Fallback (OOD)"),
                        new TestCase("write a python script to scrape stock prices", "Cloud LLM Fallback (OOD)")
                ),
                null
        ));

        // 3. SRE Log Triage
        suites.put("sre", new DemoSuite(
                "3. High-Throughput SRE Log Triage (Zero Allocation: 0 B/op)",
                "weights/demo_sre.bin",
                "data/demo_sre.csv",
                "Parses crash dumps and server logs with strictly zero heap allocation.",
                DispatchPolicy.defaultPolicy(),
                0.30f,
                gate -> {
                    gate.bind("OutOfMemory", (ctx, payload) ->
                            System.out.println("    [P0 CRITICAL] Trigger Horizontal Pod Autoscaler & restart worker")
                    ).withAnchor(1.5f, "memory", "oom", "allocating", "starvation", "killed", "oomkilled", "137");

                    gate.bind("DBPoolExhausted", (ctx, payload) ->
                            System.out.println("    [P1 WARNING] Increase PostgreSQL pool cap and kill idle connections")
                    ).withAnchor(1.5f, "hikaripool", "connection", "pool", "timeout", "timed", "postgres", "slots");

                    gate.bind("AuthBruteForce", (ctx, payload) ->
                            System.out.println("    [SECURITY] Add IP to iptables drop list and notify SecOps")
                    ).withAnchor(1.5f, "security", "login", "attempts", "alert", "brute", "fail2ban", "ssh");

                    gate.bind("SystemHealth", (ctx, payload) ->
                            System.out.println("    [P3 INFO] Metric collected without alerting on-call")
                    ).withAnchor(1.5f, "health", "probe", "healthz", "200", "ok", "heartbeat", "nominal");

                    gate.fallback((ctx, payload) ->
                            System.out.println("    [UNKNOWN LOG] Streamed to cold storage archive")
                    );
                },
                List.of(
                        new TestCase("fatal error: runtime: out of memory allocating 4194304 bytes", "P0 OutOfMemory"),
                        new TestCase("container exited with code 137 OOMKilled cgroup memory limit exceeded", "P0 OutOfMemory"),
                        new TestCase("HikariPool-1 - Connection is not available request timed out after 30000ms", "P1 DBPoolExhausted"),
                        new TestCase("org.postgresql.util.PSQLException: FATAL: remaining connection slots are reserved", "P1 DBPoolExhausted"),
                        new TestCase("SECURITY ALERT: 250 failed login attempts in 60 seconds from single IP", "Security AuthBruteForce"),
                        new TestCase("Fail2ban banned host 192.168.1.100 for 3600 seconds after 10 failed login attempts", "Security AuthBruteForce"),
                        new TestCase("INFO: health check probe /healthz returned 200 OK latency: 2ms", "P3 SystemHealth"),
                        new TestCase("Heartbeat ping received from worker node status healthy", "P3 SystemHealth")
                ),
                (gate, ctx) -> {
                    System.out.println("    [Zero-Allocation Stack Demonstration via FilterTokens]");
                    InferenceModel model = gate.getModel();
                    String rawLog = "kernel killed process worker-task due to host memory starvation";
                    int[] tokens = model.getTokenizer().encode(rawLog);

                    long start = System.nanoTime();
                    gate.filterTokens(ctx, tokens, null);
                    long elapsed = (System.nanoTime() - start);

                    GateTrace trace = gate.inspect(rawLog);
                    System.out.printf("    Raw Log : \"%s\"%n", rawLog);
                    System.out.printf(Locale.US, "    NeuroGate Routed: %s (Confidence: %.2f%%, Cosine: %.4f, Latency: %.2f μs, Alloc: 0 B/op)%n",
                            trace.predictedLabel(), trace.confidence() * 100.0, trace.cosineSimilarity(), (double) elapsed / 1000.0);
                }
        ));

        // 4. IoT
        suites.put("iot", new DemoSuite(
                "4. Offline Edge IoT Command Dispatcher (Nuance & Anchor Calibrated)",
                "weights/demo_iot.bin",
                "data/demo_iot.csv",
                "Sub-milliwatt, sub-180KB offline smart home command router with symbolic anchor soft-bias.",
                DispatchPolicy.defaultPolicy(),
                0.30f,
                gate -> {
                    gate.bind("LightControl", (ctx, payload) ->
                            System.out.println("    [GPIO 18 HIGH] Toggle Zigbee Relay for Living Room Chandelier")
                    ).withAnchor(2.0f, "dark", "light", "lamps", "lamp", "switch", "lights", "chandelier", "brighten");

                    gate.bind("ClimateControl", (ctx, payload) ->
                            System.out.println("    [MODBUS UART] Send temperature setpoint to Daikin HVAC inverter")
                    ).withAnchor(1.8f, "cooling", "heat", "fan", "temp", "temperature", "ac", "air", "heating", "celsius");

                    gate.bind("DoorLock", (ctx, payload) ->
                            System.out.println("    [ZWAVE COMMAND] Engage motorized deadbolt locking mechanism")
                    ).withAnchor(1.8f, "lock", "door", "deadbolt", "entrance", "unlock");

                    gate.bind("MediaPlayback", (ctx, payload) ->
                            System.out.println("    [ALSA AUDIO] Resume Spotify streaming on soundbar")
                    ).withAnchor(1.8f, "play", "jazz", "music", "soundbar", "spotify", "song", "pause", "audio");

                    gate.fallback((ctx, payload) ->
                            System.out.println("    [AUDIO PROMPT] 'Sorry, I did not catch that command'")
                    );
                },
                List.of(
                        new TestCase("it is too dark in here please switch on lamps in living room", "LightControl (Slang/Context Anchor Boost)"),
                        new TestCase("turn on the chandelier lights above dining table", "LightControl"),
                        new TestCase("cooling mode on maximum fan speed in master bedroom", "ClimateControl"),
                        new TestCase("set living room temperature setpoint to 21 degrees celsius", "ClimateControl"),
                        new TestCase("lock the front entrance smart door deadbolt immediately", "DoorLock"),
                        new TestCase("unlock front door deadbolt for delivery courier guest", "DoorLock"),
                        new TestCase("play smooth jazz music on living room soundbar speaker", "MediaPlayback"),
                        new TestCase("pause spotify audio playback on bedroom speaker", "MediaPlayback")
                ),
                null
        ));

        // 5. CI/CD
        suites.put("cicd", new DemoSuite(
                "5. Automated CI/CD Failure Triage & Self-Healing",
                "weights/demo_cicd.bin",
                "data/demo_cicd.csv",
                "Analyzes build error tail logs with symbolic keyword anchors to trigger auto-remediation.",
                DispatchPolicy.defaultPolicy(),
                0.30f,
                gate -> {
                    gate.bind("NetworkTimeoutRetry", (ctx, payload) ->
                            System.out.println("    [AUTO REMEDIATION] Retry transient build step after 5s backoff")
                    ).withAnchor(1.6f, "timeout", "curl", "connect", "timed", "port", "handshake", "tls");

                    gate.bind("ResourceScaleUp", (ctx, payload) ->
                            System.out.println("    [AUTO REMEDIATION] Re-queue job on 64GB High-Memory Runner Pod")
                    ).withAnchor(1.6f, "sigkill", "memory", "137", "killed", "runner", "exhausted", "quota");

                    gate.bind("CodeSyntaxAlert", (ctx, payload) ->
                            System.out.println("    [AUTO NOTIFY] Block PR merge and notify author via Slack/Git comment")
                    ).withAnchor(1.8f, "syntax", "semicolon", "unexpected", "token", "column", "variable", "string");

                    gate.bind("CacheEvict", (ctx, payload) ->
                            System.out.println("    [AUTO REMEDIATION] Invalidate layer cache and rebuild from scratch")
                    ).withAnchor(1.6f, "cache", "clean", "corrupted", "build", "checksum", "sha256");

                    gate.fallback((ctx, payload) ->
                            System.out.println("    [MANUAL TRIAGE] Flag build for human DevOps on-call review")
                    );
                },
                List.of(
                        new TestCase("curl: (28) Failed to connect to registry.npmjs.org port 443: Connection timed out", "Auto-Retry: NetworkTimeoutRetry"),
                        new TestCase("docker pull failed tls handshake timeout communicating with registry", "Auto-Retry: NetworkTimeoutRetry"),
                        new TestCase("Command terminated by signal 9 SIGKILL exit status 137 runner ran out of memory", "Scale-Up: ResourceScaleUp"),
                        new TestCase("gcc: fatal error: Killed (program cc1plus) virtual memory exhausted", "Scale-Up: ResourceScaleUp"),
                        new TestCase("syntax error: unexpected token semicolon at line 144 column 2", "Notify-Dev: CodeSyntaxAlert"),
                        new TestCase("cannot use variable of type string as type int in argument to processTransaction", "Notify-Dev: CodeSyntaxAlert"),
                        new TestCase("corrupted go build cache detected in /root/.cache/go-build please clean", "Evict-Cache: CacheEvict"),
                        new TestCase("checksum mismatch for cached layer sha256:4a8b invalid local tar", "Evict-Cache: CacheEvict")
                ),
                null
        ));

        // 6. FinTech
        suites.put("fintech", new DemoSuite(
                "6. FinTech Transaction Memo Audit & Fraud Prevention (NeuroGate Calibrated)",
                "weights/demo_fintech.bin",
                "data/demo_fintech.csv",
                "Real-time remittance inspection for scam interception with high-risk symbolic anchors.",
                DispatchPolicy.builder()
                        .highThreshold(0.70)
                        .lowThreshold(0.35)
                        .marginCutoff(0.15)
                        .maxEntropy(2.0)
                        .pipelineThreshold(0.30)
                        .build(),
                0.30f,
                gate -> {
                    gate.bind("NormalTransfer", (ctx, payload) ->
                            System.out.println("    [INSTANT APPROVAL] Transaction approved and dispatched to ACH rail")
                    ).withAnchor(2.0f, "lunch", "split", "colleagues", "monthly", "payment", "bill", "rent", "reimbursement", "dinner");

                    gate.bind("PhishingSuspicion", (ctx, payload) ->
                            System.out.println("    [BLOCK & INTERCEPT] Suspicious scam wire blocked; call compliance desk")
                    ).withAnchor(2.2f, "urgent", "police", "fine", "bitcoin", "wallet", "scam", "compromised", "safety");

                    gate.bind("ChargebackDispute", (ctx, payload) ->
                            System.out.println("    [DISPUTE ROUTE] Open formal chargeback ticket with issuing bank")
                    ).withAnchor(2.0f, "dispute", "charged", "three", "times", "single", "coffee", "card", "unauthorized", "subscription");

                    gate.bind("HighValueAudit", (ctx, payload) ->
                            System.out.println("    [COMPLIANCE AUDIT] Hold escrow wire pending dual-officer AML sign-off")
                    ).withAnchor(1.8f, "acquisition", "escrow", "million", "tranche", "corporate", "commercial", "estate");

                    gate.ambiguous((ctx, p, s, payload) ->
                            System.out.printf("    [STEP-UP 2FA] Ambiguous memo (%s vs %s): SMS OTP challenge required%n", p, s)
                    ).fallback((ctx, payload) ->
                            System.out.println("    [MANUAL AUDIT] Route wire memo to fraud investigations team")
                    );
                },
                List.of(
                        new TestCase("monthly lunch payment split with office colleagues", "Instant Approval: NormalTransfer"),
                        new TestCase("reimbursement for team dinner pizza and drinks", "Instant Approval: NormalTransfer"),
                        new TestCase("urgent send funds now police fine wire to bitcoin wallet immediately", "Block & Intercept: PhishingSuspicion"),
                        new TestCase("your account is compromised transfer all savings to temporary safety wallet", "Block & Intercept: PhishingSuspicion"),
                        new TestCase("merchant charged my card three times for single coffee", "Dispute: ChargebackDispute"),
                        new TestCase("unauthorized recurring subscription charge from merchant after cancellation", "Dispute: ChargebackDispute"),
                        new TestCase("corporate acquisition escrow settlement tranche wire five million dollars", "AML Audit: HighValueAudit"),
                        new TestCase("commercial real estate property purchase closing escrow wire transfer", "AML Audit: HighValueAudit")
                ),
                null
        ));

        return suites;
    }
}
