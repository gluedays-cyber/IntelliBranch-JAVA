package com.intellibranch.routing;

/**
 * RouteAction defines the execution handler signature for a matched branch.
 */
@FunctionalInterface
public interface RouteAction {
    void execute(Object context, Object payload) throws Exception;
}
