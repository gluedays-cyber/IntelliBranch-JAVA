package com.intellibranch.examples;

import com.intellibranch.cli.DemoRunner;

/**
 * Master Enterprise Example Gallery for IntelliBranch-JAVA.
 * Runs all advanced practical architectures or selectively executes targeted patterns.
 */
public class ExampleGallery {

    public static void main(String[] args) {
        String target = args.length > 0 ? args[0].toLowerCase() : "all";

        System.out.println("================================================================================");
        System.out.println("       INTELLIBRANCH-JAVA ENTERPRISE PRACTICAL EXAMPLES GALLERY");
        System.out.println("================================================================================");

        try {
            if ("all".equals(target) || "spring".equals(target) || "1".equals(target)) {
                SpringWebRoutingDemo.main(new String[0]);
                System.out.println();
            }
            if ("all".equals(target) || "kafka".equals(target) || "2".equals(target)) {
                KafkaStreamQoSDemo.main(new String[0]);
                System.out.println();
            }
            if ("all".equals(target) || "llm".equals(target) || "3".equals(target)) {
                LLMSemanticCacheDemo.main(new String[0]);
                System.out.println();
            }
            if ("all".equals(target) || "fintech".equals(target) || "4".equals(target)) {
                FinTechFraudGuardDemo.main(new String[0]);
                System.out.println();
            }
            if ("all".equals(target) || "waf".equals(target) || "5".equals(target)) {
                SecurityWafInspectionDemo.main(new String[0]);
                System.out.println();
            }
            if ("all".equals(target) || "iot".equals(target) || "6".equals(target)) {
                IoTEdgeActuatorDemo.main(new String[0]);
                System.out.println();
            }
            if ("all".equals(target) || "multitenant".equals(target) || "7".equals(target)) {
                MultiTenantCascadingDemo.main(new String[0]);
                System.out.println();
            }
            if ("all".equals(target) || "domains".equals(target) || "8".equals(target)) {
                DemoRunner.main(new String[]{"--domain", "all"});
                System.out.println();
            }
            if ("all".equals(target) || "enterprise".equals(target) || "mega".equals(target) || "9".equals(target)) {
                com.intellibranch.examples.enterprise.MegaGalleryRunner.runVerification();
                System.out.println();
            }

            System.out.println("================================================================================");
            System.out.println("  ALL ENTERPRISE EXAMPLES COMPLETED SUCCESSFULLY IN MICROSECONDS");
            System.out.println("================================================================================");
        } catch (Exception e) {
            System.err.println("Fatal execution error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
