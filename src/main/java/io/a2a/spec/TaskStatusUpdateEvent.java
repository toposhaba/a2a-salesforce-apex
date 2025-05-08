package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * A task status update event.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskStatusUpdateEvent(String id, TaskStatus status, boolean isFinal, Map<String, Object> metadata) {

    public TaskStatusUpdateEvent {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("status", status);
    }
}
