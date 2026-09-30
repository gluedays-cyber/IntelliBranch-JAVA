# IntelliBranch-JAVA
<p align="center">
  <strong>Ultra-Low Latency Neural Conditional Branching & 3-Head Geometric NeuroGate Runtime in Pure Java</strong><br>
  <em>Directly creates, trains, and executes its own domain-specific neural network in pure Java 17+. Zero external dependencies, zero JNI, zero native bindings. Routes execution flow in ~30 μs with 0 B/op heap allocation on hot paths.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Latency-~30_μs-brightgreen.svg" alt="Latency">
  <img src="https://img.shields.io/badge/Allocs-0_B/op_(ThreadLocal)-blue.svg" alt="Allocations">
  <img src="https://img.shields.io/badge/Format-v2_Positional_IBRN-orange.svg" alt="Format v2">
  <img src="https://img.shields.io/badge/Runtime-Pure_Java_17+-007396.svg" alt="Java Version">
  <img src="https://img.shields.io/badge/Dependencies-Zero_External-success.svg" alt="Zero Dependencies">
  <img src="https://img.shields.io/badge/License-MIT-lightgrey.svg" alt="License">
</p>

<p align="center">
  <a href="docs/MANUAL.md"><strong>📖 Read the Full Developer Manual & Production Guide →</strong></a>
</p>

---

## Overview

**IntelliBranch-JAVA does NOT depend on, borrow, or lease external cloud LLMs or heavyweight ONNX/Python runtimes. It directly generates, trains, and runs its own domain artificial intelligence completely within the standard JVM.**

Traditional code branching relies on brittle `switch` statements, regex tables, or massive external LLMs. IntelliBranch-JAVA introduces **neural conditional dispatching**: it trains a domain-specific embedded network directly from your text dataset in under 2 seconds. The resulting sub-180KB network is loaded into memory, mapping slang, typos, colloquialisms, and inverted syntax into continuous latent space—routing execution flow directly to your bound Java methods in **~30 microseconds with strictly 0 B/op heap allocation on hot paths**.

Because it is written in 100% pure Java without native libraries (no CGO, no JNI, no Python, no CUDA bindings), it runs natively on **Windows, Linux, macOS, and containerized microservices**, and can be embedded seamlessly into JVM languages (Java, Kotlin, Scala) or accessed via C# / .NET through IKVM or interop pipelines.

```
Incoming Request ("bruh can u refund order #49281")
                     │
                     ▼
       [ In-Memory BPE Tokenizer ]
                     │
                     ▼
  [ Dense (D=64) + Positional (P=32x64) ]
                     │
                     ▼
  [ Non-Linear GELU Mean Pooling (D=64) ]
                     │
                     ▼
      [ Hidden Projection (D=128) ]
                     │
                     ▼
   [ Softmax + Shannon Entropy Calibrated Guard ]
                     │
     ┌───────────────┼───────────────┬────────────────┐
     ▼               ▼               ▼                ▼
(Score ≥ 0.75)  (Score ≥ 0.30)  (Margin < 0.15)  (Entropy > 2.0 / UNK ≥ 0.5)
[DEFINITE ROUTE] [PIPELINE]      [AMBIGUOUS]      [FALLBACK ISOLATION]
```

---

## Architectural Comparison Matrix

| Dimension | Discrete Branching (`if` / Regex) | Cloud LLMs (OpenAI / Anthropic) | Local LLMs (Ollama / Llama.cpp) | **IntelliBranch-JAVA v2.0** |
| :--- | :--- | :--- | :--- | :--- |
| **Inference Latency** | < 1 μs | 300 ms – 2,500 ms (Network bound) | 30 ms – 300 ms (Compute bound) | **~30 μs (In-Memory JVM)** |
| **Throughput (per core)** | > 500,000 req/sec | ~50 req/sec (Rate limited) | ~20–50 req/sec (CPU saturated) | **> 33,000 req/sec (Zero Alloc)** |
| **Runtime Allocation** | 0 B/op | High (HTTP payload serialization) | High (Native buffers & JNI) | **0 B/op (ThreadLocal scratch)** |
| **System Memory (RAM)** | Negligible | External service | **4.5 GB – 8.0 GB+ (VRAM / RAM)** | **< 180 KB binary weights** |
| **Token Order Awareness** | Rigid regex position | ✅ Transformer Attention | ✅ Transformer Attention | ✅ **Learned Positional Embeddings** |
| **Deployment Complexity** | Plain Java code | Third-party API client | C++ Shared Libs, Daemons, Python | **Zero Dependencies (`pom.xml` / `build.gradle`)** |
| **Operational Cost** | $0.00 | $0.0015+ per API call | Expensive GPU hosting | **$0.00 (Self-contained)** |
| **Hot Weight Reload** | Application restart | API model string switch | Multi-second model load | **Lock-Free Atomic Reference (`0 ms` stop)** |
| **Active Learning Loop** | None | Manual logging | None | **Built-in Ring Buffer Telemetry** |
| **Platform Portability** | Universal | Universal | OS / Architecture dependent | **Universal JVM (Windows, Linux, macOS)** |

---

## Key Features

1. **100% Pure Java with Zero External Dependencies**:
   Requires only the standard Java 17+ JDK. No native shared libraries (`.so`, `.dll`, `.dylib`), no external machine learning engines, and no third-party runtime JARs.
2. **Sub-35 Microsecond Dispatching with Zero Heap Allocations**:
   By pairing flattened row-major weight matrices with reusable `ThreadLocal<InferenceBuffer>` scratch arrays, inference avoids triggering Garbage Collection (GC) pauses on critical execution hot paths.
3. **Semantic Positional XOR Disambiguation**:
   Format v2 incorporates 32 learned positional vectors combined via non-linear projection:
   $$\text{Pooled} = \frac{1}{L} \sum_{i=0}^{L-1} \text{GELU}(E_{\text{tok}_i} + P_i)$$
   This breaks the commutative property of bag-of-words, enabling the engine to distinguish between `"refund delivery"` and `"delivery refund"`.
4. **3-Head Geometric NeuroGate**:
   - **Head 1 (Geometric L2 Cosine Guard)**: Rejects Out-of-Domain (OOD) inputs by comparing normalized manifold embeddings against domain centroids.
   - **Head 2 (Bitwise Anchor Soft-Bias)**: Employs 64-bit mask operations to inject symbolic soft-bias into targeted logits at microsecond speeds.
   - **Head 3 (Softmax, Margin, LogSumExp, & Shannon Entropy)**: Evaluates calibrated confidence, top-1/top-2 margin gaps, and free energy.
5. **3-Tier Decision Pipeline**:
   Requests are deterministically routed to **Definite** (execution), **Ambiguous** (step-up clarification), or **Fallback** (safety isolation) handlers.
6. **Multi-Intent Pipeline Support**:
   When secondary intent confidence meets configured criteria, composite pipeline handlers (`Refund -> Delivery`) execute automatically.
7. **Lock-Free Hot-Swap & Telemetry Feedback**:
   Model weights can be hot-reloaded dynamically on live production traffic with zero downtime using `AtomicReference<InferenceModel>`, and borderline queries are captured in a bounded `TelemetryRingBuffer` for active learning retraining.

---

## Quickstart (5 Minutes)

### Prerequisites
- JDK 17 or higher (`javac`, `java`)
- Maven or Gradle (optional; can also run directly with standard JDK)

### 1. Project Setup (`pom.xml`)
IntelliBranch-JAVA requires **zero runtime dependencies**. Include it in your Maven build:

```xml
<dependency>
    <groupId>com.intellibranch</groupId>
    <artifactId>intellibranch</artifactId>
    <version>2.0.0</version>
</dependency>
```

Or with Gradle (`build.gradle`):
```groovy
implementation 'com.intellibranch:intellibranch:2.0.0'
```

### 2. Basic Server Routing Example

```java
package com.example;

import com.intellibranch.core.InferenceModel;
import com.intellibranch.routing.Router;
import java.nio.file.Path;

public class ServerApp {
    public static void main(String[] args) throws Exception {
        // 1. Load the compiled binary weights into memory
        Router router = Router.load(Path.of("weights/intent.bin"), 0.60);

        // 2. Bind business logic handlers to class labels
        router
            .bind("Refund", (ctx, payload) -> {
                System.out.println("Processing refund for: " + payload);
            })
            .bind("Delivery", (ctx, payload) -> {
                System.out.println("Tracking delivery for: " + payload);
            })
            .bind("Account", (ctx, payload) -> {
                System.out.println("Initiating account security for: " + payload);
            })
            .fallback((ctx, payload) -> {
                System.out.println("Isolated low-confidence request to safety: " + payload);
            });

        // 3. Dispatch queries in microseconds
        router.dispatch(null, "can u cancel order #49281? i bought it by mistake", "Order #49281");
        router.dispatch(null, "where is my shipment package tracking", "Tracking Query");
        router.dispatch(null, "unrecognized gibberish noise 99999", "Noise Payload");
    }
}
```

---

## 3-Step Lifecycle

### Step 1: Prepare Domain Knowledge (`data/sample_dataset.csv`)
Author a simple two-column CSV mapping user phrases to target intent classes:

```csv
text,label
I want to cancel my payment and request a refund,Refund
Where is my package and delivery tracking,Delivery
Forgot my account password please reset,Account
sent the return box a week ago when do i get my money back,Refund
yo i typed the wrong apt number please update address,Delivery
locked out of my account after 3 tries help pls,Account
```

### Step 2: Offline Model Training (`TrainerCli`)
Train your subword BPE vocabulary and neural weights into a compact Little-Endian binary (`intent.bin`) with SHA-256 integrity verification:

```bash
# Using standard Java command line
java -cp target/classes com.intellibranch.cli.TrainerCli \
    --data data/sample_dataset.csv \
    --out weights/intent.bin \
    --epochs 50 \
    --lr 0.005 \
    --vocab 250
```

Or invoke the training API directly inside Java code:
```java
List<DataSample> samples = Trainer.loadCSVDataset(Path.of("data/sample_dataset.csv"));
TrainConfig cfg = TrainConfig.defaultConfig()
        .setEpochs(50)
        .setLearningRate(0.005f)
        .setTargetVocabSize(250);

InferenceModel model = Trainer.trainModel(samples, cfg);
BinaryModel.save(Path.of("weights/intent.bin"), model);
```

### Step 3: High-Performance Routing (`Router` & `NeuroGate`)
Execute high-speed in-memory dispatching in your application with full 3-tier routing or 3-head geometric guards.

---

## NeuroGate: 3-Head Geometric Gating

For mission-critical production gateways, **NeuroGate** coordinates a shared neural backbone with three complementary geometric heads:

```java
NeuroGate gate = NeuroGate.load(Path.of("weights/demo_cs.bin"));

// Configure 3-Tier Policy & L2 Cosine Domain Boundary
gate.setPolicy(DispatchPolicy.builder()
        .highThreshold(0.70)
        .lowThreshold(0.35)
        .marginCutoff(0.15)
        .maxEntropy(0.70)
        .pipelineThreshold(0.25)
        .minLogSumExp(7.0)
        .build());
gate.setMinCosineSim(0.35f);

// Bind routes with Symbolic Keyword Anchors (Head 2)
gate.bind("Refund", (ctx, payload) -> {
    System.out.println("Refund processed.");
}).withAnchor(1.3f, "refund", "money", "card", "charge", "return");

gate.bind("Delivery", (ctx, payload) -> {
    System.out.println("Courier GPS queried.");
}).withAnchor(1.3f, "courier", "delivered", "package", "delivery", "box");

// Bind Multi-Intent Pipeline
gate.bindPipeline("Refund", "Delivery", (ctx, p, s, payload) -> {
    System.out.printf("Pipeline executed: %s -> %s%n", p, s);
});

// Ambiguous & Fallback Handlers
gate.ambiguous((ctx, p, s, payload) -> {
    System.out.printf("Ambiguous query (%s vs %s): Request clarification%n", p, s);
}).fallback((ctx, payload) -> {
    System.out.println("Isolated to human escalation.");
});

// Microsecond Dispatch
gate.filterPipeline(null, "i returned the box please update delivery", null);
```

---

## 6-Domain Production Demonstration

IntelliBranch-JAVA ships with an end-to-end multi-task demo covering 6 distinct enterprise scenarios:

```bash
# Run all 6 domains
java -cp target/classes com.intellibranch.cli.DemoRunner --domain all

# Or run a specific domain: cs, llm, sre, iot, cicd, fintech
java -cp target/classes com.intellibranch.cli.DemoRunner --domain sre
```

1. **E-Commerce CS Gateway**: Multi-intent pipelines (`Refund -> Delivery`) and positional XOR disambiguation (`refund delivery` vs `delivery refund`).
2. **Semantic LLM Gateway**: Resolves common banking intents in ~30 μs locally ($0.00 cost) while escalating genuine Out-of-Domain queries to OpenAI GPT-4o.
3. **High-Throughput SRE Log Triage**: Classifies system crash dumps (OOMKilled, HikariCP pool exhaustion, Brute Force attacks) with strictly 0 B/op heap allocation.
4. **Offline Edge IoT Command Dispatcher**: Dispatches smart home commands (Light, HVAC, Door Locks, Media) with sub-milliwatt footprint.
5. **Automated CI/CD Failure Triage**: Analyzes build error tail logs with symbolic anchors to trigger automated retries, memory pod scaling, or cache invalidation.
6. **FinTech Transaction Memo Audit**: Inspects wire memos for scam patterns, chargeback disputes, and high-value AML escrow audits in real time.

---

## Wire Format Specification (IBRN v2)

IntelliBranch models are compiled into a compact, self-contained Little-Endian binary with a 32-byte SHA-256 integrity checksum:

| Offset / Field | Type | Description |
| :--- | :--- | :--- |
| `0x00` Magic | `[4]byte` | Magic signature ASCII: `IBRN` |
| `0x04` Version | `uint32 LE` | Format version (`2` = Positional Encoding support) |
| `0x08` VocabSize | `uint32 LE` | Total subwords in vocabulary ($V$) |
| `0x0C` EmbeddingDim | `uint32 LE` | Latent vector dimension ($D$, typically 64) |
| `0x10` HiddenDim | `uint32 LE` | Layer 1 MLP dimension ($H$, typically 128) |
| `0x14` NumClasses | `uint32 LE` | Target class label count ($C$) |
| Labels Block | Dynamic | Label strings (prefixed with `uint32 LE` byte length) |
| Vocabulary Block | Dynamic | Subword strings (prefixed with `uint32 LE` byte length) |
| Merge Rules Block | Dynamic | BPE merge rules (`Token1`, `Token2`, `Target` uint32 LE) |
| Tensors: Embedding | `float32[] LE` | Dense token embeddings [$V \times D$] |
| Tensors: Positional | `float32[] LE` | Learned positional vectors [$128 \times D$] |
| Tensors: W1, B1 | `float32[] LE` | Layer 1 weights [$D \times H$] and biases [$H$] |
| Tensors: W2, B2 | `float32[] LE` | Layer 2 weights [$H \times C$] and biases [$C$] |
| Checksum (Tail) | `[32]byte` | Cryptographic SHA-256 digest of preceding bytes |

---

## Building and Testing

### Build with Maven
```bash
mvn clean package
mvn test
```

### Build with Gradle
```bash
gradle build
gradle test
```

### Direct Compilation with JDK
```bash
# Compile core and application classes
javac -d target/classes $(Get-ChildItem -Recurse -Path src/main/java/*.java | ForEach-Object { $_.FullName })

# Compile and run unit tests
javac -d target/classes -cp target/classes $(Get-ChildItem -Recurse -Path src/test/java/*.java | ForEach-Object { $_.FullName })
java -cp target/classes com.intellibranch.AllTests
```

---

## License

IntelliBranch-JAVA is open-sourced under the permissive [MIT License](LICENSE).
