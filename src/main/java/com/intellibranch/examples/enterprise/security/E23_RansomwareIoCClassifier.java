package com.intellibranch.examples.enterprise.security;

import com.intellibranch.examples.enterprise.BaseDemo;
import com.intellibranch.neurogate.GateTrace;

import java.util.HashMap;
import java.util.Map;

/**
 * [E23] Ransomware Indicator-of-Compromise (IoC) Filter
 * Domain: Security & Compliance
 * Monitors file system operations and halts mass extension renaming / rapid entropy spikes
 */
public class E23_RansomwareIoCClassifier extends BaseDemo {

    @Override
    public String getId() { return "E23"; }

    @Override
    public String getTitle() { return "Ransomware Indicator-of-Compromise (IoC) Filter"; }

    @Override
    public String getCategory() { return "Security & Compliance"; }

    @Override
    public String getDescription() { return "Monitors file system operations and halts mass extension renaming / rapid entropy spikes"; }

    @Override
    public void execute() {
        printHeader();

        String[] vocab = new String[]{ "isolate_host", "throttle_io", "allow_filesystem", "fallback", "word_document", "save_file", "normal_edit", "high_write_burst", "zip_archive", "temp", "mass_rename", "lockbit_extension", "vssadmin_delete", "mass", "rename", "lockbit", "extension", "vssadmin", "delete", "shadow", "high", "write", "burst", "zip", "archive", "creation", "word", "document", "save", "file", "normal", "edit", "routine", "background", "scan" };
        String[] classes = new String[]{ "isolate_host", "throttle_io", "allow_filesystem", "fallback" };

        Map<String, String[]> anchorMap = new HashMap<>();
            anchorMap.put("allow_filesystem", new String[]{"word_document", "save_file", "normal_edit"});
            anchorMap.put("throttle_io", new String[]{"high_write_burst", "zip_archive", "temp"});
            anchorMap.put("isolate_host", new String[]{"mass_rename", "lockbit_extension", "vssadmin_delete"});

        GateBundle bundle = buildDomainGate(vocab, classes, anchorMap);

        evaluateAndPrint(bundle, "mass rename lockbit extension vssadmin delete shadow");
        evaluateAndPrint(bundle, "high write burst zip archive temp creation");
        evaluateAndPrint(bundle, "word document save file normal edit");
        evaluateAndPrint(bundle, "routine background scan");
    }

    public static void main(String[] args) {
        new E23_RansomwareIoCClassifier().execute();
    }
}