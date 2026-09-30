package com.intellibranch.examples.enterprise.security;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E19] JWT Token Role-Based Gateway Router
 * Domain: Security & Compliance
 * Routes API callers to Admin, Partner, or Public microservices based on decoded claims
 */
public class E19_JwtClaimVerificationRouter extends BaseDemo {

    @Override
    public String getId() { return "E19"; }

    @Override
    public String getTitle() { return "JWT Token Role-Based Gateway Router"; }

    @Override
    public String getCategory() { return "Security & Compliance"; }

    @Override
    public String getDescription() { return "Routes API callers to Admin, Partner, or Public microservices based on decoded claims"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "admin_microservice", "partner_gateway", "public_api", "fallback", "superadmin", "root_role", "system_scope", "anonymous", "guest", "read_only", "b2b_scope", "vendor_role", "partner", "root", "role", "system", "scope", "token", "b2b", "vendor", "api", "read", "only", "client", "corrupt", "bearer", "header" };
        String[] classes = new String[]{ "admin_microservice", "partner_gateway", "public_api", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("admin_microservice", new String[]{"superadmin", "root_role", "system_scope"});
            anchorMap.put("public_api", new String[]{"anonymous", "guest", "read_only"});
            anchorMap.put("partner_gateway", new String[]{"b2b_scope", "vendor_role", "partner"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "superadmin root role system scope token");
        evaluateAndPrint(bundle, "b2b scope vendor role partner api");
        evaluateAndPrint(bundle, "anonymous guest read only client");
        evaluateAndPrint(bundle, "corrupt bearer token header");
    }

    public static void main(String[] args) {
        new E19_JwtClaimVerificationRouter().execute();
    }
}