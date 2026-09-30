package com.intellibranch.examples;

import com.intellibranch.core.BinaryModel;
import com.intellibranch.core.InferenceModel;
import com.intellibranch.routing.DispatchPolicy;
import com.intellibranch.routing.RouteTrace;
import com.intellibranch.routing.Router;
import com.intellibranch.training.DataSample;
import com.intellibranch.training.TrainConfig;
import com.intellibranch.training.Trainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Enterprise REST API Controller & Service Router pattern with IntelliBranch.
 * Demonstrates simulated Spring Boot / Quarkus web gateway intent dispatching.
 */
public class SpringWebRoutingDemo {

    public record WebRequest(String traceId, String userId, String body) {}
    public record WebResponse(String traceId, String status, String routedHandler, long latencyMicros) {}

    public static class MockSpringRouterService {
        private final Router router;

        public MockSpringRouterService(Path modelPath) throws Exception {
            this.router = Router.load(modelPath, 0.65);
            this.router.enableTelemetry(512);

            // Bind business service methods
            this.router
                .bind("Refund", (ctx, payload) -> {
                    WebRequest req = (WebRequest) payload;
                    System.out.printf("  [Spring Controller] Processing Refund for user %s (Trace: %s)%n", req.userId(), req.traceId());
                })
                .bind("Delivery", (ctx, payload) -> {
                    WebRequest req = (WebRequest) payload;
                    System.out.printf("  [Spring Controller] Querying Delivery logistics for user %s (Trace: %s)%n", req.userId(), req.traceId());
                })
                .bind("Account", (ctx, payload) -> {
                    WebRequest req = (WebRequest) payload;
                    System.out.printf("  [Spring Controller] Initiating Account Security for user %s (Trace: %s)%n", req.userId(), req.traceId());
                })
                .ambiguous((ctx, p, s, payload) -> {
                    WebRequest req = (WebRequest) payload;
                    System.out.printf("  [Spring Controller] AMBIGUOUS INTENT (%s vs %s) for user %s: Requesting disambiguation prompt%n",
                            p, s, req.userId());
                })
                .fallback((ctx, payload) -> {
                    WebRequest req = (WebRequest) payload;
                    System.out.printf("  [Spring Controller] FALLBACK ISOLATION for user %s: Forwarding to Tier-2 human agent%n", req.userId());
                });
        }

        public WebResponse handleIncomingRequest(WebRequest request) throws Exception {
            long start = System.nanoTime();
            RouteTrace trace = router.inspect(request.body());
            router.dispatch(null, request.body(), request);
            long latencyMicros = (System.nanoTime() - start) / 1000;
            return new WebResponse(request.traceId(), "SUCCESS", trace.predictedLabel(), latencyMicros);
        }

        public void printDriftReport() {
            var events = router.drainTelemetry();
            System.out.printf("%n[Telemetry Drift Monitor] Captured %d borderline/fallback events for active retraining.%n", events.size());
            for (var ev : events) {
                System.out.printf("  - Input: \"%s\" | Primary: %s (%.2f%%) | Ambiguous: %b | Fallback: %b%n",
                        ev.inputText(), ev.predictedLabel(), ev.confidence() * 100.0, ev.isAmbiguous(), ev.isFallback());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("  EXAMPLE 1: SPRING BOOT / ENTERPRISE WEB CONTROLLER INTENT ROUTER");
        System.out.println("================================================================================");

        Path modelPath = Path.of("weights/intent.bin");
        if (!Files.exists(modelPath)) {
            List<DataSample> samples = Trainer.loadCSVDataset(Path.of("data/sample_dataset.csv"));
            InferenceModel model = Trainer.trainModel(samples, TrainConfig.defaultConfig().setEpochs(50));
            BinaryModel.save(modelPath, model);
        }

        MockSpringRouterService service = new MockSpringRouterService(modelPath);

        List<WebRequest> requests = List.of(
                new WebRequest("req-101", "usr-881", "I need to cancel this order and get a full refund to my visa"),
                new WebRequest("req-102", "usr-442", "Package says delivered but my mailbox is completely empty"),
                new WebRequest("req-103", "usr-319", "Can you send the 2FA password reset link to my verified phone?"),
                new WebRequest("req-104", "usr-905", "refund or change delivery date"),
                new WebRequest("req-105", "usr-110", "1234567890 !@#$%^&*() complete nonsense query")
        );

        for (WebRequest req : requests) {
            System.out.printf("%n--> Incoming HTTP POST /api/v1/support (Trace: %s, User: %s)%n", req.traceId(), req.userId());
            System.out.printf("    Payload: \"%s\"%n", req.body());
            WebResponse resp = service.handleIncomingRequest(req);
            System.out.printf("    Response: HTTP 200 OK [Handler: %s, Latency: %d μs]%n", resp.routedHandler(), resp.latencyMicros());
        }

        service.printDriftReport();
    }
}
