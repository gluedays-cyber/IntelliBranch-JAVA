package com.intellibranch.examples.enterprise.security;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E22] API Scope Privilege Escalation Detector
 * Domain: Security & Compliance
 * Catches unauthorized attempt to invoke privileged administrative API routes
 */
public class E22_ApiTokenPrivilegeRouter extends BaseDemo {

    @Override
    public String getId() { return "E22"; }

    @Override
    public String getTitle() { return "API Scope Privilege Escalation Detector"; }

    @Override
    public String getCategory() { return "Security & Compliance"; }

    @Override
    public String getDescription() { return "Catches unauthorized attempt to invoke privileged administrative API routes"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "block_privilege_escalation", "allow_authorized", "audit_log", "fallback", "change_password", "rotate_key", "export", "read_order", "view_items", "list", "sudo_exec", "grant_role", "revoke_audit", "sudo", "exec", "grant", "role", "revoke", "audit", "call", "read", "order", "view", "items", "catalog", "change", "password", "rotate", "key", "config", "normal", "telemetry", "check" };
        String[] classes = new String[]{ "block_privilege_escalation", "allow_authorized", "audit_log", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("audit_log", new String[]{"change_password", "rotate_key", "export"});
            anchorMap.put("allow_authorized", new String[]{"read_order", "view_items", "list"});
            anchorMap.put("block_privilege_escalation", new String[]{"sudo_exec", "grant_role", "revoke_audit"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "sudo exec grant role revoke audit call");
        evaluateAndPrint(bundle, "read order view items list catalog");
        evaluateAndPrint(bundle, "change password rotate key export config");
        evaluateAndPrint(bundle, "normal telemetry check");
    }

    public static void main(String[] args) {
        new E22_ApiTokenPrivilegeRouter().execute();
    }
}