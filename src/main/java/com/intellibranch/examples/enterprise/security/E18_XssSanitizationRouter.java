package com.intellibranch.examples.enterprise.security;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E18] Cross-Site Scripting (XSS) Threat Classifier
 * Domain: Security & Compliance
 * Detects script tags, SVG onload vectors, and DOM-based injection attempts
 */
public class E18_XssSanitizationRouter extends BaseDemo {

    @Override
    public String getId() { return "E18"; }

    @Override
    public String getTitle() { return "Cross-Site Scripting (XSS) Threat Classifier"; }

    @Override
    public String getCategory() { return "Security & Compliance"; }

    @Override
    public String getDescription() { return "Detects script tags, SVG onload vectors, and DOM-based injection attempts"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "block_xss", "allow_input", "sanitize_html", "fallback", "plain_text", "article", "bio", "bold_tag", "italic", "link", "script_alert", "onerror_svg", "javascript_href", "script", "alert", "document", "cookie", "javascript", "href", "plain", "text", "user", "bold", "tag", "formatting", "innocuous", "raw", "comment" };
        String[] classes = new String[]{ "block_xss", "allow_input", "sanitize_html", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("allow_input", new String[]{"plain_text", "article", "bio"});
            anchorMap.put("sanitize_html", new String[]{"bold_tag", "italic", "link"});
            anchorMap.put("block_xss", new String[]{"script_alert", "onerror_svg", "javascript_href"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "script alert document cookie javascript href");
        evaluateAndPrint(bundle, "plain text bio user article");
        evaluateAndPrint(bundle, "bold tag italic link formatting");
        evaluateAndPrint(bundle, "innocuous raw comment");
    }

    public static void main(String[] args) {
        new E18_XssSanitizationRouter().execute();
    }
}