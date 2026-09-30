package com.intellibranch.examples.enterprise;

/**
 * Common interface for all 80+ enterprise demonstration cases.
 */
public interface EnterpriseDemo {
    String getId();
    String getTitle();
    String getCategory();
    String getDescription();
    void execute();
}
