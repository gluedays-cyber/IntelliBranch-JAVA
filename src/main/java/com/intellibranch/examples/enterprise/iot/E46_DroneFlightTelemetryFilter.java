package com.intellibranch.examples.enterprise.iot;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E46] Autonomous UAV Flight Controller Route Guard
 * Domain: IoT & Edge Computing
 * Inspects IMU and LiDAR telemetry to trigger collision avoidance or safe return-to-home
 */
public class E46_DroneFlightTelemetryFilter extends BaseDemo {

    @Override
    public String getId() { return "E46"; }

    @Override
    public String getTitle() { return "Autonomous UAV Flight Controller Route Guard"; }

    @Override
    public String getCategory() { return "IoT & Edge Computing"; }

    @Override
    public String getDescription() { return "Inspects IMU and LiDAR telemetry to trigger collision avoidance or safe return-to-home"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "collision_evasion", "return_to_home", "continue_mission", "fallback", "obstacle_detected", "lidar_proximity", "tree_canopy", "waypoint_nav", "altitude_hold", "nominal_wind", "battery_critical", "gps_glitch", "comm_loss", "obstacle", "detected", "lidar", "proximity", "tree", "canopy", "evasive", "battery", "critical", "gps", "glitch", "comm", "loss", "rth", "waypoint", "nav", "altitude", "hold", "nominal", "wind", "mission", "optical", "flow", "dark", "noise" };
        String[] classes = new String[]{ "collision_evasion", "return_to_home", "continue_mission", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("collision_evasion", new String[]{"obstacle_detected", "lidar_proximity", "tree_canopy"});
            anchorMap.put("continue_mission", new String[]{"waypoint_nav", "altitude_hold", "nominal_wind"});
            anchorMap.put("return_to_home", new String[]{"battery_critical", "gps_glitch", "comm_loss"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "obstacle detected lidar proximity tree canopy evasive");
        evaluateAndPrint(bundle, "battery critical gps glitch comm loss rth");
        evaluateAndPrint(bundle, "waypoint nav altitude hold nominal wind mission");
        evaluateAndPrint(bundle, "optical flow dark noise");
    }

    public static void main(String[] args) {
        new E46_DroneFlightTelemetryFilter().execute();
    }
}