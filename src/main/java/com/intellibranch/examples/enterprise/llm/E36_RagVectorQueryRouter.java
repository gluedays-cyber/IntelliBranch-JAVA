package com.intellibranch.examples.enterprise.llm;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E36] RAG Multi-Index Vector Routing Engine
 * Domain: LLM & Generative AI
 * Dispatches semantic search questions to Technical Docs, Legal Wiki, or Support Knowledge Base
 */
public class E36_RagVectorQueryRouter extends BaseDemo {

    @Override
    public String getId() { return "E36"; }

    @Override
    public String getTitle() { return "RAG Multi-Index Vector Routing Engine"; }

    @Override
    public String getCategory() { return "LLM & Generative AI"; }

    @Override
    public String getDescription() { return "Dispatches semantic search questions to Technical Docs, Legal Wiki, or Support Knowledge Base"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "technical_docs_index", "legal_wiki_index", "support_faq_index", "fallback", "terms_service", "privacy_gdpr", "compliance_policy", "how_to_refund", "change_address", "contact_human", "api_reference", "sdk_install", "endpoint_spec", "api", "reference", "sdk", "install", "endpoint", "spec", "documentation", "terms", "service", "privacy", "gdpr", "compliance", "policy", "how", "to", "refund", "change", "address", "contact", "human", "unknown", "query", "search" };
        String[] classes = new String[]{ "technical_docs_index", "legal_wiki_index", "support_faq_index", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("legal_wiki_index", new String[]{"terms_service", "privacy_gdpr", "compliance_policy"});
            anchorMap.put("support_faq_index", new String[]{"how_to_refund", "change_address", "contact_human"});
            anchorMap.put("technical_docs_index", new String[]{"api_reference", "sdk_install", "endpoint_spec"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "api reference sdk install endpoint spec documentation");
        evaluateAndPrint(bundle, "terms service privacy gdpr compliance policy");
        evaluateAndPrint(bundle, "how to refund change address contact human");
        evaluateAndPrint(bundle, "unknown query search");
    }

    public static void main(String[] args) {
        new E36_RagVectorQueryRouter().execute();
    }
}