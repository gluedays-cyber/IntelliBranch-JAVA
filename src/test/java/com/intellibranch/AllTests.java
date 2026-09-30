package com.intellibranch;

public class AllTests {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING INTELLIBRANCH-JAVA TEST SUITE");
        System.out.println("=================================================");
        try {
            OpsTest.runTests();
            BPETokenizerTest.runTests();
            BinaryModelTest.runTests();
            RouterTest.runTests();
            NeuroGateTest.runTests();
            System.out.println("=================================================");
            System.out.println("  ALL INTELLIBRANCH-JAVA UNIT TESTS PASSED (100%)");
            System.out.println("=================================================");
        } catch (Throwable t) {
            System.err.println("Test failure: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
