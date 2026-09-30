package com.intellibranch.examples.enterprise.iot;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E43] Commercial Fleet Telematics & Geofence Guard
 * Domain: IoT & Edge Computing
 * Triages vehicle GPS telemetry into Route Drift, Harsh Braking, or Speeding incidents
 */
public class E43_FleetVehicleGpsAnomaly extends BaseDemo {

    @Override
    public String getId() { return "E43"; }

    @Override
    public String getTitle() { return "Commercial Fleet Telematics & Geofence Guard"; }

    @Override
    public String getCategory() { return "IoT & Edge Computing"; }

    @Override
    public String getDescription() { return "Triages vehicle GPS telemetry into Route Drift, Harsh Braking, or Speeding incidents"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "geofence_breach", "harsh_maneuver", "nominal_cruise", "fallback", "restricted_zone", "off_route", "border_cross", "highway_speed", "cruise_control", "lane_center", "hard_braking", "rapid_accel", "roll_angle", "off", "route", "restricted", "zone", "border", "cross", "geofence", "hard", "braking", "rapid", "accel", "roll", "angle", "event", "highway", "speed", "cruise", "control", "lane", "center", "steady", "satellite", "lost", "signal" };
        String[] classes = new String[]{ "geofence_breach", "harsh_maneuver", "nominal_cruise", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("geofence_breach", new String[]{"restricted_zone", "off_route", "border_cross"});
            anchorMap.put("nominal_cruise", new String[]{"highway_speed", "cruise_control", "lane_center"});
            anchorMap.put("harsh_maneuver", new String[]{"hard_braking", "rapid_accel", "roll_angle"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "off route restricted zone border cross geofence");
        evaluateAndPrint(bundle, "hard braking rapid accel roll angle event");
        evaluateAndPrint(bundle, "highway speed cruise control lane center steady");
        evaluateAndPrint(bundle, "satellite lost signal");
    }

    public static void main(String[] args) {
        new E43_FleetVehicleGpsAnomaly().execute();
    }
}