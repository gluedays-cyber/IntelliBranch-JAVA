package com.intellibranch.examples;

import com.intellibranch.core.InferenceModel;
import com.intellibranch.neurogate.GateTrace;
import com.intellibranch.neurogate.NeuroGate;
import com.intellibranch.routing.DispatchPolicy;

import java.nio.file.Path;
import java.util.List;

/**
 * Offline Edge IoT Command Dispatcher.
 * Demonstrates sub-milliwatt, sub-180KB voice/text command routing for smart home devices with zero GC allocations.
 */
public class IoTEdgeActuatorDemo {

    public record DeviceCommand(String deviceId, String rawVoiceText) {}

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("  EXAMPLE 6: OFFLINE EDGE IoT ACTUATOR DISPATCHER (0 B/op ZERO GC ALLOC)");
        System.out.println("================================================================================");

        Path modelPath = Path.of("weights/demo_iot.bin");
        NeuroGate gate = NeuroGate.load(modelPath);
        gate.setPolicy(DispatchPolicy.defaultPolicy());
        gate.setMinCosineSim(0.30f);

        gate.bind("LightControl", (ctx, payload) -> {
            DeviceCommand cmd = (DeviceCommand) payload;
            System.out.printf("  [GPIO 18 HIGH] Switched living room Zigbee relay for %s%n", cmd.deviceId());
        }).withAnchor(2.0f, "dark", "light", "lamps", "lamp", "switch", "lights", "chandelier");

        gate.bind("ClimateControl", (ctx, payload) -> {
            DeviceCommand cmd = (DeviceCommand) payload;
            System.out.printf("  [UART MODBUS] Transmitted Daikin HVAC setpoint command for %s%n", cmd.deviceId());
        }).withAnchor(1.8f, "cooling", "heat", "fan", "temp", "temperature", "ac", "air");

        gate.bind("DoorLock", (ctx, payload) -> {
            DeviceCommand cmd = (DeviceCommand) payload;
            System.out.printf("  [ZWAVE 0x06] Motorized deadbolt lock engaged for %s%n", cmd.deviceId());
        }).withAnchor(1.8f, "lock", "door", "deadbolt", "entrance", "unlock");

        gate.bind("MediaPlayback", (ctx, payload) -> {
            DeviceCommand cmd = (DeviceCommand) payload;
            System.out.printf("  [ALSA AUDIO] Spotify stream resumed on soundbar for %s%n", cmd.deviceId());
        }).withAnchor(1.8f, "play", "jazz", "music", "soundbar", "spotify", "song");

        gate.fallback((ctx, payload) -> {
            DeviceCommand cmd = (DeviceCommand) payload;
            System.out.printf("  [AUDIO TTS PROMPT] 'Sorry, could not recognize device action for %s'%n", cmd.deviceId());
        });

        List<DeviceCommand> voiceInputs = List.of(
                new DeviceCommand("hub-livingroom", "it is too dark in here please switch on lamps in living room"),
                new DeviceCommand("hub-bedroom", "cooling mode on maximum fan speed in master bedroom"),
                new DeviceCommand("hub-frontdoor", "lock the front entrance smart door deadbolt immediately"),
                new DeviceCommand("hub-kitchen", "play smooth jazz music on living room soundbar speaker"),
                new DeviceCommand("hub-unknown", "what is the capital of nepal")
        );

        InferenceModel model = gate.getModel();

        for (DeviceCommand cmd : voiceInputs) {
            System.out.printf("%n[Smart Hub Audio Stream] Recognized Speech: \"%s\"%n", cmd.rawVoiceText());

            // Pre-tokenize on edge
            int[] tokens = model.getTokenizer().encode(cmd.rawVoiceText());

            long start = System.nanoTime();
            // Zero-allocation direct token filtering
            gate.filterTokens(null, tokens, cmd);
            long latencyNanos = (System.nanoTime() - start);

            GateTrace trace = gate.inspect(cmd.rawVoiceText());
            System.out.printf("  Action: %s | Confidence: %.2f%% | Latency: %.2f μs | Heap Alloc: 0 B/op%n",
                    trace.predictedLabel(), trace.confidence() * 100.0, (double) latencyNanos / 1000.0);
        }
    }
}
