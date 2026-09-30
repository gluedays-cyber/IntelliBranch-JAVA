package com.intellibranch.examples.enterprise.iot;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E41] Embedded Smart Home Voice Actuator
 * Domain: IoT & Edge Computing
 * Routes home speech commands to Lighting, HVAC, or Security sub-controllers
 */
public class E41_SmartHomeVoiceActuator extends BaseDemo {

    @Override
    public String getId() { return "E41"; }

    @Override
    public String getTitle() { return "Embedded Smart Home Voice Actuator"; }

    @Override
    public String getCategory() { return "IoT & Edge Computing"; }

    @Override
    public String getDescription() { return "Routes home speech commands to Lighting, HVAC, or Security sub-controllers"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "actuate_lighting", "actuate_hvac", "actuate_security", "fallback", "set_thermostat", "heater_on", "cooling_fan", "turn_lights", "dim_lamp", "brighten_room", "lock_front_door", "arm_alarm", "close_garage", "turn", "lights", "dim", "lamp", "brighten", "room", "set", "thermostat", "heater", "on", "cooling", "fan", "lock", "front", "door", "arm", "alarm", "close", "garage", "background", "tv", "noise" };
        String[] classes = new String[]{ "actuate_lighting", "actuate_hvac", "actuate_security", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("actuate_hvac", new String[]{"set_thermostat", "heater_on", "cooling_fan"});
            anchorMap.put("actuate_lighting", new String[]{"turn_lights", "dim_lamp", "brighten_room"});
            anchorMap.put("actuate_security", new String[]{"lock_front_door", "arm_alarm", "close_garage"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "turn lights dim lamp brighten room");
        evaluateAndPrint(bundle, "set thermostat heater on cooling fan");
        evaluateAndPrint(bundle, "lock front door arm alarm close garage");
        evaluateAndPrint(bundle, "background tv noise");
    }

    public static void main(String[] args) {
        new E41_SmartHomeVoiceActuator().execute();
    }
}