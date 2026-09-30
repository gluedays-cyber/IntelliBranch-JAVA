package com.intellibranch.examples.enterprise.web;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E02] GraphQL Subgraph Field Router
 * Domain: Web & API Services
 * Dispatches federated GraphQL query ASTs to specialized subgraphs based on field semantics
 */
public class E02_GraphQLResolverRouter extends BaseDemo {

    @Override
    public String getId() { return "E02"; }

    @Override
    public String getTitle() { return "GraphQL Subgraph Field Router"; }

    @Override
    public String getCategory() { return "Web & API Services"; }

    @Override
    public String getDescription() { return "Dispatches federated GraphQL query ASTs to specialized subgraphs based on field semantics"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "catalog_subgraph", "review_subgraph", "order_subgraph", "fallback", "product", "sku", "category", "rating", "feedback", "stars", "checkout", "cart", "fulfill", "lookup", "submit", "5", "customer", "random", "introspect", "query" };
        String[] classes = new String[]{ "catalog_subgraph", "review_subgraph", "order_subgraph", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("catalog_subgraph", new String[]{"product", "sku", "category"});
            anchorMap.put("review_subgraph", new String[]{"rating", "feedback", "stars"});
            anchorMap.put("order_subgraph", new String[]{"checkout", "cart", "fulfill"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "lookup product sku category");
        evaluateAndPrint(bundle, "submit 5 stars customer feedback");
        evaluateAndPrint(bundle, "fulfill customer checkout cart");
        evaluateAndPrint(bundle, "random introspect query");
    }

    public static void main(String[] args) {
        new E02_GraphQLResolverRouter().execute();
    }
}