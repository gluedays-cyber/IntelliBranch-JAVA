package com.intellibranch.routing;

/**
 * AmbiguousAction defines the handler signature for ambiguous requests with competing top-2 predictions.
 */
@FunctionalInterface
public interface AmbiguousAction {
    void execute(Object context, String primary, String secondary, Object payload) throws Exception;
}
