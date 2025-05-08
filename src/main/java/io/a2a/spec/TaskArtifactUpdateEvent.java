package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * A task artifact update event.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskArtifactUpdateEvent(String id, Artifact artifact, Map<String, Object> metadata) {

    public TaskArtifactUpdateEvent {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("artifact", artifact);
    }
}
