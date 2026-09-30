package com.intellibranch.neurogate;

import com.intellibranch.routing.RouteAction;

/**
 * GateRouteBuilder provides fluent API chaining for binding routes and anchor soft biases.
 */
public class GateRouteBuilder {
    private final NeuroGate gate;
    private final int classIndex;
    private final String label;

    public GateRouteBuilder(NeuroGate gate, int classIndex, String label) {
        this.gate = gate;
        this.classIndex = classIndex;
        this.label = label;
    }

    /**
     * Registers anchor keywords that inject a soft additive bias into this class's logit upon keyword match.
     */
    public GateRouteBuilder withAnchor(float weight, String... keywords) {
        gate.registerAnchor(classIndex, weight, keywords);
        return this;
    }

    /**
     * Binds another route handler fluently.
     */
    public GateRouteBuilder bind(String label, RouteAction handler) {
        return gate.bind(label, handler);
    }
}
