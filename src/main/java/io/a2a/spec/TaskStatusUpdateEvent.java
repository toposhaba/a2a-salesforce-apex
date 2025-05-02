package io.a2a.spec;

import java.util.Map;

import io.a2a.util.Assert;

/**
 * A task status update event.
 */
public record TaskStatusUpdateEvent(String id, TaskStatus status, boolean isFinal, Map<String, Object> metadata) {

    public TaskStatusUpdateEvent {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("status", status);
    }
}
