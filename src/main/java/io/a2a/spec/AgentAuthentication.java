package io.a2a.spec;

import java.util.List;

import io.a2a.util.Assert;

/**
 * The authentication requirements for an agent.
 */
public record AgentAuthentication(List<String> schemes, String credentials) {

    public AgentAuthentication {
        Assert.checkNotNullParam("schemes", schemes);
    }
}
