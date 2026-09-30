package com.intellibranch.examples.enterprise.web;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E08] Dynamic Content-Negotiation Serializer
 * Domain: Web & API Services
 * Selects optimal serialization pipeline (Protobuf, Avro, JSON, XML) in microseconds
 */
public class E08_ContentNegotiationRouter extends BaseDemo {

    @Override
    public String getId() { return "E08"; }

    @Override
    public String getTitle() { return "Dynamic Content-Negotiation Serializer"; }

    @Override
    public String getCategory() { return "Web & API Services"; }

    @Override
    public String getDescription() { return "Selects optimal serialization pipeline (Protobuf, Avro, JSON, XML) in microseconds"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "protobuf_pipe", "avro_pipe", "json_pipe", "fallback", "schema", "record", "compact", "proto", "binary", "grpc", "json", "text", "human", "packet", "avro", "stream", "representation", "unknown", "content", "type" };
        String[] classes = new String[]{ "protobuf_pipe", "avro_pipe", "json_pipe", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("avro_pipe", new String[]{"schema", "record", "compact"});
            anchorMap.put("protobuf_pipe", new String[]{"proto", "binary", "grpc"});
            anchorMap.put("json_pipe", new String[]{"json", "text", "human"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "proto binary compact packet");
        evaluateAndPrint(bundle, "avro schema record stream");
        evaluateAndPrint(bundle, "human json text representation");
        evaluateAndPrint(bundle, "unknown content type");
    }

    public static void main(String[] args) {
        new E08_ContentNegotiationRouter().execute();
    }
}