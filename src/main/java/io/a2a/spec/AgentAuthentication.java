package io.a2a.spec;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * The authentication requirements for an agent.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentAuthentication(List<String> schemes, String credentials) {

    public AgentAuthentication {
        Assert.checkNotNullParam("schemes", schemes);
    }
}
