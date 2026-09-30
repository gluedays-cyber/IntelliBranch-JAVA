package com.intellibranch.examples.enterprise.web;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E06] WebSocket Bidirectional Channel Classifier
 * Domain: Web & API Services
 * Routes full-duplex socket messages to trading, gaming, or telemetry engines
 */
public class E06_WebSocketStreamRouter extends BaseDemo {

    @Override
    public String getId() { return "E06"; }

    @Override
    public String getTitle() { return "WebSocket Bidirectional Channel Classifier"; }

    @Override
    public String getCategory() { return "Web & API Services"; }

    @Override
    public String getDescription() { return "Routes full-duplex socket messages to trading, gaming, or telemetry engines"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "orderbook_ws", "multiplayer_ws", "telemetry_ws", "fallback", "heartbeat", "cpu", "ping", "move", "spawn", "shoot", "bid", "ask", "ticker", "crypto", "quote", "player", "action", "device", "socket", "handshake" };
        String[] classes = new String[]{ "orderbook_ws", "multiplayer_ws", "telemetry_ws", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("telemetry_ws", new String[]{"heartbeat", "cpu", "ping"});
            anchorMap.put("multiplayer_ws", new String[]{"move", "spawn", "shoot"});
            anchorMap.put("orderbook_ws", new String[]{"bid", "ask", "ticker"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "crypto ticker bid ask quote");
        evaluateAndPrint(bundle, "player move spawn action");
        evaluateAndPrint(bundle, "device heartbeat cpu ping");
        evaluateAndPrint(bundle, "socket handshake ping");
    }

    public static void main(String[] args) {
        new E06_WebSocketStreamRouter().execute();
    }
}