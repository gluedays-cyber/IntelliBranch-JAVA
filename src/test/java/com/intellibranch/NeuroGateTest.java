package com.intellibranch;

import com.intellibranch.core.InferenceModel;
import com.intellibranch.neurogate.GateTrace;
import com.intellibranch.neurogate.NeuroGate;
import com.intellibranch.training.DataSample;
import com.intellibranch.training.TrainConfig;
import com.intellibranch.training.Trainer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NeuroGateTest {
    public static void runTests() {
        testNeuroGateAnchorsAndOod();
        System.out.println("  ✓ NeuroGateTest passed");
    }

    private static void testNeuroGateAnchorsAndOod() {
        List<DataSample> samples = List.of(
                new DataSample("turn on living room lamps", "LightControl"),
                new DataSample("it is too dark here please switch on light", "LightControl"),
                new DataSample("cooling mode maximum fan temperature bedroom", "ClimateControl"),
                new DataSample("set ac thermostat cool air heat", "ClimateControl")
        );

        TrainConfig cfg = TrainConfig.defaultConfig()
                .setEpochs(50)
                .setEmbeddingDim(32)
                .setHiddenDim(32)
                .setTargetVocabSize(64);

        InferenceModel model = Trainer.trainModel(samples, cfg);
        NeuroGate gate = new NeuroGate(model);

        AtomicBoolean lightTriggered = new AtomicBoolean(false);
        AtomicBoolean fallbackTriggered = new AtomicBoolean(false);

        gate.bind("LightControl", (ctx, payload) -> lightTriggered.set(true))
                .withAnchor(1.5f, "lamp", "lamps", "dark", "light");

        gate.bind("ClimateControl", (ctx, payload) -> {})
                .withAnchor(1.5f, "cooling", "ac", "fan", "temp");

        gate.fallback((ctx, payload) -> fallbackTriggered.set(true));

        try {
            // 1. Verify anchor boost reinforces LightControl
            lightTriggered.set(false);
            gate.filter(null, "it is too dark in here please switch on lamps", null);
            if (!lightTriggered.get()) {
                throw new AssertionError("Expected LightControl to trigger via anchor soft-bias");
            }

            // 2. Verify inspection trace
            GateTrace trace = gate.inspect("it is too dark in here please switch on lamps");
            if (!"LightControl".equals(trace.predictedLabel())) {
                throw new AssertionError("Expected predicted label LightControl, got: " + trace.predictedLabel());
            }
            if (trace.triggeredAnchors().isEmpty()) {
                throw new AssertionError("Expected triggered anchors to be recorded");
            }

            // 3. Verify strict OOD boundary isolation
            gate.setMinCosineSim(0.99f); // Impose strict threshold
            fallbackTriggered.set(false);
            gate.filter(null, "what is the meaning of quantum black holes", null);
            if (!fallbackTriggered.get()) {
                throw new AssertionError("Expected strict domain boundary to isolate OOD query to Fallback");
            }
        } catch (Exception e) {
            throw new AssertionError("NeuroGate test failed: " + e.getMessage(), e);
        }
    }
}
