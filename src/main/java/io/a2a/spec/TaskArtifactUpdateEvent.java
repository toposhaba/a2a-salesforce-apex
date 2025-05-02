package io.a2a.spec;

import java.util.Map;

import io.a2a.util.Assert;

/**
 * A task artifact update event.
 */
public record TaskArtifactUpdateEvent(String id, Artifact artifact, Map<String, Object> metadata) {

    public TaskArtifactUpdateEvent {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("artifact", artifact);
    }
}
