package com.intellibranch;

import com.intellibranch.core.*;
import com.intellibranch.routing.DispatchPolicy;
import com.intellibranch.routing.RouteTrace;
import com.intellibranch.routing.Router;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RouterTest {
    public static void runTests() {
        testRouterDefiniteAndFallback();
        testTelemetryDrain();
        System.out.println("  ✓ RouterTest passed");
    }

    private static InferenceModel createTrainedDummyModel() {
        List<com.intellibranch.training.DataSample> samples = List.of(
                new com.intellibranch.training.DataSample("please refund money to card", "Refund"),
                new com.intellibranch.training.DataSample("i want refund cancel order", "Refund"),
                new com.intellibranch.training.DataSample("package delivery courier tracking", "Delivery"),
                new com.intellibranch.training.DataSample("where is my shipment package", "Delivery")
        );
        var cfg = com.intellibranch.training.TrainConfig.defaultConfig()
                .setEpochs(70)
                .setLearningRate(0.01f)
                .setEmbeddingDim(32)
                .setHiddenDim(32)
                .setTargetVocabSize(64);
        return com.intellibranch.training.Trainer.trainModel(samples, cfg);
    }

    private static void testRouterDefiniteAndFallback() {
        InferenceModel model = createTrainedDummyModel();
        Router router = new Router(model, 0.75);

        AtomicBoolean refundTriggered = new AtomicBoolean(false);
        AtomicBoolean fallbackTriggered = new AtomicBoolean(false);

        router.bind("Refund", (ctx, payload) -> refundTriggered.set(true))
                .bind("Delivery", (ctx, payload) -> {})
                .fallback((ctx, payload) -> fallbackTriggered.set(true));

        try {
            refundTriggered.set(false);
            fallbackTriggered.set(false);
            router.dispatch(null, "please refund money to card", null);
            if (!refundTriggered.get()) {
                throw new AssertionError("Expected Refund handler to trigger");
            }

            refundTriggered.set(false);
            fallbackTriggered.set(false);
            router.dispatch(null, "999999999999999999 !@#$%^&*()", null);
            if (!fallbackTriggered.get()) {
                throw new AssertionError("Expected Fallback handler for out of domain noise");
            }

            RouteTrace trace = router.inspect("please refund money to card");
            if (trace.isFallback()) {
                throw new AssertionError("Inspection should not mark refund as fallback");
            }
        } catch (Exception e) {
            throw new AssertionError("Router test failed: " + e.getMessage(), e);
        }
    }

    private static void testTelemetryDrain() {
        InferenceModel model = createTrainedDummyModel();
        Router router = new Router(model, 0.99); // High threshold forces fallback/ambiguous
        router.enableTelemetry(50);

        try {
            router.dispatch(null, "test telemetry trigger", null);
            var events = router.drainTelemetry();
            if (events.isEmpty()) {
                throw new AssertionError("Telemetry ring buffer should have recorded fallback event");
            }
            if (!router.drainTelemetry().isEmpty()) {
                throw new AssertionError("Telemetry ring buffer should be empty after drain");
            }
        } catch (Exception e) {
            throw new AssertionError("Telemetry test failed: " + e.getMessage(), e);
        }
    }
}
