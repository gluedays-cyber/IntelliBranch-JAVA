package com.intellibranch.neurogate;

import java.util.List;

/**
 * AnchorRule defines a symbolic soft-bias injected into a specific class logit upon bitmask match.
 */
public record AnchorRule(
        int classIndex,
        long mask,
        float weight,
        List<String> keywords
) {
}
