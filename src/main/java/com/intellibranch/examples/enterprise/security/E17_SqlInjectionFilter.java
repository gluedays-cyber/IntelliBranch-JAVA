package com.intellibranch.examples.enterprise.security;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E17] Zero-Latency SQLi Pre-Execution Guard
 * Domain: Security & Compliance
 * Inspects dynamic query inputs and immediately blocks SQL injection syntax
 */
public class E17_SqlInjectionFilter extends BaseDemo {

    @Override
    public String getId() { return "E17"; }

    @Override
    public String getTitle() { return "Zero-Latency SQLi Pre-Execution Guard"; }

    @Override
    public String getCategory() { return "Security & Compliance"; }

    @Override
    public String getDescription() { return "Inspects dynamic query inputs and immediately blocks SQL injection syntax"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "block_sqli", "allow_clean", "quarantine_inspect", "fallback", "filter_id", "sort_name", "page", "union_select", "drop_table", "or_1_equals_1", "special_char", "escape", "hex", "union", "select", "password", "from", "users", "or", "1", "equals", "filter", "id", "sort", "name", "2", "special", "char", "encoding", "string", "regular", "customer", "username" };
        String[] classes = new String[]{ "block_sqli", "allow_clean", "quarantine_inspect", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("allow_clean", new String[]{"filter_id", "sort_name", "page"});
            anchorMap.put("block_sqli", new String[]{"union_select", "drop_table", "or_1_equals_1"});
            anchorMap.put("quarantine_inspect", new String[]{"special_char", "escape", "hex"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "union select password from users or 1 equals 1");
        evaluateAndPrint(bundle, "filter id sort name page 2");
        evaluateAndPrint(bundle, "special char escape hex encoding string");
        evaluateAndPrint(bundle, "regular customer username");
    }

    public static void main(String[] args) {
        new E17_SqlInjectionFilter().execute();
    }
}