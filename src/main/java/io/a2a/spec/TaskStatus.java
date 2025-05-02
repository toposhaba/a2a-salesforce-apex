package io.a2a.spec;

import java.time.LocalDateTime;

import io.a2a.util.Assert;

/**
 * Represents the status of a task.
 */
public record TaskStatus(TaskState state, Message message, LocalDateTime timestamp) {

    public TaskStatus {
        Assert.checkNotNullParam("state", state);
        timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
    }
}
