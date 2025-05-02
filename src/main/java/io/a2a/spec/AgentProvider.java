package io.a2a.spec;

import io.a2a.util.Assert;

/**
 * An agent provider.
 */
public record AgentProvider(String organization, String url) {

    public AgentProvider {
        Assert.checkNotNullParam("organization", organization);
    }
}
