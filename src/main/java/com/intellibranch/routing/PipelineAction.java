package com.intellibranch.routing;

/**
 * PipelineAction defines the execution signature when both primary and secondary intents are eligible for multi-intent handling.
 */
@FunctionalInterface
public interface PipelineAction {
    void execute(Object context, String primary, String secondary, Object payload) throws Exception;
}
