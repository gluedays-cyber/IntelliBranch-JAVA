# IntelliBranch-JAVA: Embedded Neural AI Manual & Production Guide

This document provides a comprehensive technical manual and architectural reference for **IntelliBranch-JAVA: An Ultra-Low Latency Embedded Neural AI Runtime in Pure Java**. Stop borrowing external models or leasing expensive cloud APIs. Learn how to train lightweight neural networks from scratch in seconds and execute microsecond AI-driven control flow directly inside the standard JVM with zero external dependencies.

---

## Table of Contents

1. [Architectural Mental Model for Java Developers](#1-architectural-mental-model-for-java-developers)
   - [1.1. Discrete String Equality vs. Continuous Latent Space](#11-discrete-string-equality-vs-continuous-latent-space)
   - [1.2. Two Lifecycle Phases: Compilation vs. In-Memory Routing](#12-two-lifecycle-phases-compilation-vs-in-memory-routing)
2. [The 4-Step Operational Workflow](#2-the-4-step-operational-workflow)
3. [Core API Reference Manual](#3-core-api-reference-manual)
   - [3.1. Router Construction & Initialization](#31-router-construction--initialization)
   - [3.2. 3-Tier Criteria: `DispatchPolicy`](#32-3-tier-criteria-dispatchpolicy)
   - [3.3. Route Binding: `bind`](#33-route-binding-bind)
   - [3.4. Borderline Safety: `ambiguous`](#34-borderline-safety-ambiguous)
   - [3.5. Multi-Intent Routing: `bindPipeline` & `defaultPipeline`](#35-multi-intent-routing-bindpipeline--defaultpipeline)
   - [3.6. Safety Isolation: `fallback`](#36-safety-isolation-fallback)
   - [3.7. Inference Execution: `dispatch` & `dispatchPipeline`](#37-inference-execution-dispatch--dispatchpipeline)
   - [3.8. Whitebox Observability: `inspect` & `RouteTrace`](#38-whitebox-observability-inspect--routetrace)
   - [3.9. Lock-Free Hot-Swap: `reload` & `swapModel`](#39-lock-free-hot-swap-reload--swapmodel)
   - [3.10. Active Learning: `enableTelemetry` & `drainTelemetry`](#310-active-learning-enabletelemetry--draintelemetry)
   - [3.11. 3-Head Geometric NeuroGate Engine](#311-3-head-geometric-neurogate-engine)
4. [End-to-End Production Tutorial](#4-end-to-end-production-tutorial)
   - [Step 1: AI Knowledge Design (`dataset.csv`)](#step-1-ai-knowledge-design-datasetcsv)
   - [Step 2: Model Training & Binary Export (`TrainerCli`)](#step-2-model-training--binary-export-trainercli)
   - [Step 3: In-Memory Router Deployment](#step-3-in-memory-router-deployment)
5. [Advanced Enterprise Integration Recipes](#5-advanced-enterprise-integration-recipes)
   - [5.1. Spring Boot & Quarkus Integration](#51-spring-boot--quarkus-integration)
   - [5.2. High-Throughput Kafka Stream QoS Partitioning](#52-high-throughput-kafka-stream-qos-partitioning)
   - [5.3. Semantic LLM Gateway & Cloud API Cost Reduction](#53-semantic-llm-gateway--cloud-api-cost-reduction)
   - [5.4. Virtual Threads (Project Loom) & Zero Allocation](#54-virtual-threads-project-loom--zero-allocation)
   - [5.5. C# / .NET & Cross-Platform Interop Guidance](#55-c--net--cross-platform-interop-guidance)
6. [Low-Level JVM Runtime Internals](#6-low-level-jvm-runtime-internals)
   - [6.1. ThreadLocal Zero-Allocation Memory Mechanics](#61-threadlocal-zero-allocation-memory-mechanics)
   - [6.2. Flat 1D Row-Major Layout & CPU Cache Line Exploitation](#62-flat-1d-row-major-layout--cpu-cache-line-exploitation)
   - [6.3. IBRN Binary Wire Format Specification](#63-ibrn-binary-wire-format-specification)
   - [6.4. Cryptographic SHA-256 Model Verification](#64-cryptographic-sha-256-model-verification)
7. [Enterprise Architectural Blueprints](#7-enterprise-architectural-blueprints)
   - [7.1. SRE Automated Crash Dump & Log Triage](#71-sre-automated-crash-dump--log-triage)
   - [7.2. Offline Edge IoT Appliance Command Dispatcher](#72-offline-edge-iot-appliance-command-dispatcher)
   - [7.3. FinTech Transaction Memo Audit & Fraud Prevention](#73-fintech-transaction-memo-audit--fraud-prevention)
   - [7.4. Automated CI/CD Failure Triage & Self-Healing](#74-automated-cicd-failure-triage--self-healing)

---

## 1. Architectural Mental Model for Java Developers

### 1.1. Discrete String Equality vs. Continuous Latent Space

In classical Java programming, conditional branching relies on exact string equality or fragile regular expressions:

```java
// ❌ CLASSICAL JAVA BRANCHING: Collapses under colloquial phrasing, typos, and syntax permutations
if (input.contains("refund") || input.contains("cancel")) {
    // FAILS on: "sent the return box a week ago when do i get my money back"
    // FAILS on: "can u reverse the charge?" (synonyms, slang)
    // MISROUTES on: "cancel delivery delay alerts" (keyword collision)
}
```

**IntelliBranch-JAVA** replaces discrete character matching with **continuous latent space proximity**:

```text
[ Input Text ] ("can u refund order #49281")
     │
     ▼
[ BPE Tokenizer ] ────── Splits text into learned subword tokens (e.g., "ref", "und") -> immune to typos
     │
     ▼
[ 64-D Latent Embeddings ] ── Semantically similar business intents map to adjacent vector coordinates
     │
     ▼
[ 128-D GELU Layer ] ── Evaluates context combinations (distinguishes "cancel order" from "cancel notifications")
     │
     ▼
[ Softmax Distribution ] ── Produces calibrated probabilities (Refund: 0.98, Delivery: 0.01)
     │
     ▼
[ Branch Dispatch ] ───── Directly invokes your bound Java lambda or method reference in ~30 microseconds
```

### 1.2. Two Lifecycle Phases: Compilation vs. In-Memory Routing

IntelliBranch divides work cleanly into two decoupled phases:

| Dimension | Phase 1: Model Compilation (Offline Training) | Phase 2: In-Memory Routing (Live Hot Path) |
| :--- | :--- | :--- |
| **Action** | Parses raw CSV dataset, learns BPE subwords, trains MLP weights via AdamW | Loads `.bin` model into RAM once and evaluates queries in microseconds |
| **Duration** | **1.5 – 2.0 seconds** on standard CPU | **~30 microseconds** per query |
| **Output** | Compact Little-Endian binary (`intent.bin`, < 180 KB) | Immediate dispatch of bound Java handler (`RouteAction`) |
| **Resource Usage** | Run once during CI/CD build or server bootstrap | Consumes < 180 KB RAM, **0 B/op heap allocation**, and 0% background CPU |

---

## 2. The 4-Step Operational Workflow

```text
┌────────────────────────┐       ┌────────────────────────┐       ┌────────────────────────┐       ┌────────────────────────┐
│ 1. AI Knowledge Design │ ────▶ │ 2. Build Your Own AI   │ ────▶ │ 3. Wire Java Handlers  │ ────▶ │ 4. Microsecond Routing │
│    (CSV Dataset)       │       │    (TrainerCli / API)  │       │    (router.bind)       │       │    (dispatch / ~30μs)  │
└────────────────────────┘       └────────────────────────┘       └────────────────────────┘       └────────────────────────┘
```

1. **AI Knowledge Design**: Author a two-column CSV mapping user phrases to target intent classes (typically 30–150 rows per intent).
2. **Build Your Own AI**: Run `TrainerCli` to compile subwords and neural weights into an `IBRN` binary with SHA-256 verification.
3. **Wire Java Handlers**: Load the model into a `Router` or `NeuroGate` instance and bind class labels to business lambdas.
4. **Microsecond Routing**: Dispatch incoming requests with zero heap allocation using thread-local scratch buffers.

---

## 3. Core API Reference Manual

### 3.1. Router Construction & Initialization

```java
// Load directly from filesystem with default confidence threshold (e.g., 0.60)
Router router = Router.load(Path.of("weights/intent.bin"), 0.60);
```

### 3.2. 3-Tier Criteria: `DispatchPolicy`

IntelliBranch-JAVA classifies all incoming queries into three safety tiers using mathematically sound criteria:

```java
DispatchPolicy policy = DispatchPolicy.builder()
        .highThreshold(0.75)       // Minimum score for definite execution (default: 0.75)
        .lowThreshold(0.40)        // Score cutoff below which query routes to fallback (default: 0.40)
        .marginCutoff(0.15)        // Minimum required gap between Top-1 and Top-2 (default: 0.15)
        .maxEntropy(2.0)           // Maximum Shannon entropy before OOD isolation (default: 2.0)
        .pipelineThreshold(0.30)   // Minimum secondary score for multi-intent pipelines (default: 0.30)
        .minLogSumExp(0.0)         // Free energy threshold (0 disables)
        .build();

router.setPolicy(policy);
```

### 3.3. Route Binding: `bind`

Binds a class label to a Java functional interface (`RouteAction`):

```java
router.bind("Refund", (ctx, payload) -> {
    Order order = (Order) payload;
    paymentService.processRefund(order.getId());
});
```

### 3.4. Borderline Safety: `ambiguous`

Handles queries with competing top-2 predictions or narrow margin gaps:

```java
router.ambiguous((ctx, primary, secondary, payload) -> {
    System.out.printf("Ambiguous query between [%s] and [%s]. Requesting user clarification.%n", primary, secondary);
});
```

### 3.5. Multi-Intent Routing: `bindPipeline` & `defaultPipeline`

Executes sequential logic when both primary and secondary predictions qualify:

```java
router.bindPipeline("Refund", "Delivery", (ctx, p, s, payload) -> {
    refundService.approveReturn(payload);
    deliveryService.updateReshipment(payload);
});
```

### 3.6. Safety Isolation: `fallback`

Executes when a request has confidence below `lowThreshold`, unknown token ratio $\ge 0.50$, or high prediction entropy:

```java
router.fallback((ctx, payload) -> {
    System.out.println("Low-confidence or Out-of-Domain query escalated to tier-2 human support.");
});
```

### 3.7. Inference Execution: `dispatch` & `dispatchPipeline`

```java
// Dispatches with standard 3-tier routing
router.dispatch(null, "please cancel my purchase and refund card", orderPayload);

// Dispatches with multi-intent pipeline evaluation
router.dispatchPipeline(null, "i returned the item please update shipment", orderPayload);
```

### 3.8. Whitebox Observability: `inspect` & `RouteTrace`

Returns complete diagnostic metadata for audit logging without executing handlers:

```java
RouteTrace trace = router.inspect("what is my current checking balance");
System.out.printf("Predicted: %s (Confidence: %.2f%%, Margin: %.4f, Entropy: %.4f, Latency: %d μs)%n",
        trace.predictedLabel(), trace.confidence() * 100.0, trace.margin(), trace.entropy(), trace.latencyMicros());
```

### 3.9. Lock-Free Hot-Swap: `reload` & `swapModel`

Hot-swap neural weights dynamically in production without stopping traffic:

```java
// Thread-safe, lock-free model swap
router.reload(Path.of("weights/intent_v2.bin"));
```

### 3.10. Active Learning: `enableTelemetry` & `drainTelemetry`

Configures a bounded FIFO ring buffer to capture borderline queries for automated retraining:

```java
router.enableTelemetry(2048);

// Periodically drained by a background worker
List<TelemetryEvent> events = router.drainTelemetry();
```

---

### 3.11. 3-Head Geometric NeuroGate Engine

For high-security or mission-critical gateways, `NeuroGate` combines the shared neural backbone with 3 complementary geometric heads:

```
                  Incoming Query Text
                           │
                           ▼
               [ Shared Neural Backbone ]
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
       [ Head 1: OOD ] [ Head 2: Anchor ] [ Head 3: Softmax ]
       L2 Cosine Guard   Bitmask Soft-Bias  Entropy & Margin
             │             │             │
             └─────────────┬─────────────┘
                           ▼
                  [ Decision Router ]
```

- **Head 1: Geometric L2 Cosine Guard**: Rejects queries outside the latent training manifold:
  $$\text{CosineSim} = \frac{\mathbf{v} \cdot \mathbf{c}}{\|\mathbf{v}\|_2 \|\mathbf{c}\|_2} < \tau_{\text{cosine}} \implies \text{OOD Rejection}$$
- **Head 2: 1-Cycle Bitwise Anchor Soft-Bias**: Injects symbolic additive bias into targeted class logits using bitwise masks:
  $$\text{Logit}_c = \text{Logit}_c + \text{weight} \times \text{popcount}(\text{textMask} \ \& \ \text{anchorMask}_c)$$
- **Head 3: Stack Softmax, Margin, LogSumExp, & Entropy**:
  $$\text{FreeEnergy} = -\log \sum_i e^{\text{Logit}_i}$$

```java
NeuroGate gate = NeuroGate.load(Path.of("weights/demo_cs.bin"));

// Calibrate true manifold center from training dataset
gate.calibrateDomainCentroid(samples);
gate.setMinCosineSim(0.35f);

// Bind routes with Symbolic Keyword Anchors
gate.bind("Refund", (ctx, payload) -> {
    System.out.println("Processing refund.");
}).withAnchor(1.3f, "refund", "money", "card", "charge", "return");

// Execute zero-allocation filtering
gate.filterPipeline(null, "please refund the money to my card", null);
```

---

## 4. End-to-End Production Tutorial

### Step 1: AI Knowledge Design (`dataset.csv`)
Create `data/support.csv`:
```csv
text,label
i want my money back please refund card,Refund
cancel transaction and reverse the charge,Refund
where is my shipment tracking status,Delivery
courier says delivered but nothing is in mailbox,Delivery
forgot my login password please reset access,Account
locked out of my account after three failed attempts,Account
```

### Step 2: Model Training & Binary Export (`TrainerCli`)
Execute offline training:
```bash
java -cp target/classes com.intellibranch.cli.TrainerCli \
    --data data/support.csv \
    --out weights/support.bin \
    --epochs 50 \
    --lr 0.005 \
    --vocab 200
```

### Step 3: In-Memory Router Deployment
```java
public class SupportService {
    private final Router router;

    public SupportService() throws IOException {
        this.router = Router.load(Path.of("weights/support.bin"), 0.65);
        this.router
            .bind("Refund", this::handleRefund)
            .bind("Delivery", this::handleDelivery)
            .bind("Account", this::handleAccount)
            .fallback(this::handleFallback);
    }

    public void processMessage(String customerMessage) throws Exception {
        router.dispatch(null, customerMessage, customerMessage);
    }

    private void handleRefund(Object ctx, Object payload) { /* ... */ }
    private void handleDelivery(Object ctx, Object payload) { /* ... */ }
    private void handleAccount(Object ctx, Object payload) { /* ... */ }
    private void handleFallback(Object ctx, Object payload) { /* ... */ }
}
```

---

## 5. Advanced Enterprise Integration Recipes

### 5.1. Spring Boot & Quarkus Integration

Register the `Router` as a singleton Spring bean:

```java
@Configuration
public class IntelliBranchConfig {

    @Bean
    public Router intelliBranchRouter() throws IOException {
        Router router = Router.load(Path.of("weights/intent.bin"), 0.70);
        router.enableTelemetry(4096);
        return router;
    }
}
```

Inject and route in a Spring `@RestController`:

```java
@RestController
@RequestMapping("/api/v1/dispatch")
public class DispatchController {

    private final Router router;

    public DispatchController(Router router) {
        this.router = router;
    }

    @PostMapping
    public ResponseEntity<RouteResponse> dispatch(@RequestBody UserRequest request) {
        RouteTrace trace = router.inspect(request.getText());
        return ResponseEntity.ok(new RouteResponse(trace.predictedLabel(), trace.confidence(), trace.latencyMicros()));
    }
}
```

### 5.2. High-Throughput Kafka Stream QoS Partitioning

Route streaming events at microsecond latency directly inside a Kafka consumer loop:

```java
@KafkaListener(topics = "incoming-tickets")
public void onTicket(ConsumerRecord<String, String> record) throws Exception {
    router.dispatch(null, record.value(), record);
}
```

### 5.3. Semantic LLM Gateway & Cloud API Cost Reduction

Resolve high-frequency known intents locally in ~30 μs ($0.00 cost), escalating only genuine Out-of-Domain queries to cloud LLMs (e.g. OpenAI GPT-4o):

```java
NeuroGate gate = NeuroGate.load(Path.of("weights/demo_llm.bin"));

gate.bind("QueryBalance", (ctx, payload) -> {
    // Resolved in 30 μs via Redis cache (Cost: $0.00)
    return redisClient.getBalance(payload);
});

gate.fallback((ctx, payload) -> {
    // Only true OOD / high entropy queries hit external cloud LLMs (Cost: $0.02)
    return openAiClient.chatCompletion(payload);
});
```

### 5.4. Virtual Threads (Project Loom) & Zero Allocation

IntelliBranch-JAVA utilizes `ThreadLocal<InferenceBuffer>` for scratch arrays. In high-concurrency environments running millions of Virtual Threads, the scratch buffer footprint is minimal (~2 KB per thread) and fully garbage-collected when the virtual thread terminates.

### 5.5. C# / .NET & Cross-Platform Interop Guidance

For C# / .NET developers who need intelligent branching:
1. **IKVM Compiler**: The pure Java JAR produced by IntelliBranch-JAVA can be converted directly into a native .NET DLL using [IKVM](https://github.com/ikvm-revived/ikvm):
   ```bash
   ikvmc -target:library intellibranch-2.0.0.jar
   ```
2. **Microservice Sidecar**: Run IntelliBranch-JAVA as an embedded gRPC or HTTP microservice responding in < 1 millisecond.

---

## 6. Low-Level JVM Runtime Internals

### 6.1. ThreadLocal Zero-Allocation Memory Mechanics

Traditional JVM machine learning wrappers allocate temporary objects during every forward pass:
- Float wrapper objects (`Float[]`)
- Intermediate layer arrays (`new float[...]`)
- Iterators and boxed collections

IntelliBranch-JAVA eliminates all allocations on hot paths:
```java
public class InferenceBuffer {
    public final float[] pooled;
    public final float[] hidden;
    public final float[] logits;
    public final float[] probs;
}
```
Each worker thread retains its own scratch buffer. The forward pass writes directly to primitive indices with zero memory allocations (`0 B/op`).

### 6.2. Flat 1D Row-Major Layout & CPU Cache Line Exploitation

Weight matrices are laid out in flat 1D primitive arrays (`float[] w1`, `float[] w2`) rather than multi-dimensional arrays (`float[][]`). This guarantees contiguous memory alignment, allowing modern x86-64 and ARM64 CPUs to prefetch data across 64-byte L1 cache lines without pointer indirection.

### 6.3. IBRN Binary Wire Format Specification

```
0                   1                   2                   3
0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|       'I'     |       'B'     |       'R'     |       'N'     | (Magic)
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       Version (uint32 LE)                     | (2)
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      VocabSize (uint32 LE)                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                     EmbeddingDim (uint32 LE)                  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      HiddenDim (uint32 LE)                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      NumClasses (uint32 LE)                   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                         Labels Block                          |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       Vocabulary Block                        |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       Merge Rules Block                       |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                 Tensor Blocks (float32 LE)                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                SHA-256 Checksum (32 bytes)                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

### 6.4. Cryptographic SHA-256 Model Verification

Every binary model is verified against an appended SHA-256 digest on startup. Tampered weights, corrupted downloads, or incomplete writes are rejected instantly with an `IOException`.

---

## 7. Enterprise Architectural Blueprints

### 7.1. SRE Automated Crash Dump & Log Triage
Classifies system logs (OOMKilled, Connection pool exhaustion, Auth brute-force) in ~30 μs with 0 B/op heap allocation.

### 7.2. Offline Edge IoT Appliance Command Dispatcher
Executes local device commands (Lighting, HVAC, Motorized deadbolts, Media) on low-power devices with sub-milliwatt footprint.

### 7.3. FinTech Transaction Memo Audit & Fraud Prevention
Inspects payment memos for social engineering, unauthorized charges, and high-value AML escrow holds in real time.

### 7.4. Automated CI/CD Failure Triage & Self-Healing
Parses tail build failure logs to trigger automated step retries, memory pod scale-ups, or build cache invalidation.

---

## 8. Enterprise Executable Example Gallery

IntelliBranch-JAVA provides 7 standalone enterprise example suites in `src/main/java/com/intellibranch/examples`:

| Example File | Key Classes & Methods | Target Architecture Pattern | Command Line Run |
| :--- | :--- | :--- | :--- |
| [`SpringWebRoutingDemo.java`](file:///c:/Users/sezzi/programming/IntelliBranch-JAVA/src/main/java/com/intellibranch/examples/SpringWebRoutingDemo.java) | `MockSpringRouterService.handleIncomingRequest` | Spring Boot / Quarkus REST gateway intent routing with telemetry drift monitoring. | `java -cp target/classes com.intellibranch.examples.ExampleGallery spring` |
| [`KafkaStreamQoSDemo.java`](file:///c:/Users/sezzi/programming/IntelliBranch-JAVA/src/main/java/com/intellibranch/examples/KafkaStreamQoSDemo.java) | `NeuroGate.filter` | Apache Kafka / Pulsar stream consumer triage (P0 Critical, P1 Warning, P3 Metric). | `java -cp target/classes com.intellibranch.examples.ExampleGallery kafka` |
| [`LLMSemanticCacheDemo.java`](file:///c:/Users/sezzi/programming/IntelliBranch-JAVA/src/main/java/com/intellibranch/examples/LLMSemanticCacheDemo.java) | `NeuroGate.bind`, `gate.fallback` | Resolves 80%+ intents locally in 30 μs ($0.00), safely escalating OOD to OpenAI ($0.02). | `java -cp target/classes com.intellibranch.examples.ExampleGallery llm` |
| [`FinTechFraudGuardDemo.java`](file:///c:/Users/sezzi/programming/IntelliBranch-JAVA/src/main/java/com/intellibranch/examples/FinTechFraudGuardDemo.java) | `gate.bind("PhishingSuspicion")` | Real-time ACH/Fedwire memo audit, scam wire blocking, and step-up 2FA challenges. | `java -cp target/classes com.intellibranch.examples.ExampleGallery fintech` |
| [`SecurityWafInspectionDemo.java`](file:///c:/Users/sezzi/programming/IntelliBranch-JAVA/src/main/java/com/intellibranch/examples/SecurityWafInspectionDemo.java) | `gate.bind("SQLInjection")` | WAF / API Gateway payload inspection blocking SQLi, LFI, and RCE in microseconds. | `java -cp target/classes com.intellibranch.examples.ExampleGallery waf` |
| [`IoTEdgeActuatorDemo.java`](file:///c:/Users/sezzi/programming/IntelliBranch-JAVA/src/main/java/com/intellibranch/examples/IoTEdgeActuatorDemo.java) | `NeuroGate.filterTokens` | Embedded smart home hub command routing with strictly 0 B/op zero-alloc execution. | `java -cp target/classes com.intellibranch.examples.ExampleGallery iot` |
| [`MultiTenantCascadingDemo.java`](file:///c:/Users/sezzi/programming/IntelliBranch-JAVA/src/main/java/com/intellibranch/examples/MultiTenantCascadingDemo.java) | `tier1Gate.bind(...) -> tier2Gate.filter(...)` | Hierarchical SaaS routing: Tier-1 Tenant Classifier -> Tier-2 Domain NeuroGate. | `java -cp target/classes com.intellibranch.examples.ExampleGallery multitenant` |
| [`ExampleGallery.java`](file:///c:/Users/sezzi/programming/IntelliBranch-JAVA/src/main/java/com/intellibranch/examples/ExampleGallery.java) | `ExampleGallery.main` | Master runner executing all 7 enterprise scenarios and the 6-domain test suite. | `java -cp target/classes com.intellibranch.examples.ExampleGallery all` |

