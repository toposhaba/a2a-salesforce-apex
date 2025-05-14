package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * A task status update event.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskStatusUpdateEvent implements EventType, StreamingEventType {

    static final String STATUS_UPDATE = "status-update";
    private final String id;
    private final TaskStatus status;
    private final String contextId;
    private final boolean isFinal;
    private final Map<String, Object> metadata;
    private final String type;


    public TaskStatusUpdateEvent(String id, TaskStatus status, String contextId, boolean isFinal,
                                 Map<String, Object> metadata) {
        this(id, status, contextId, isFinal, metadata, STATUS_UPDATE);
    }

    @JsonCreator
    public TaskStatusUpdateEvent(@JsonProperty("id") String id, @JsonProperty("status") TaskStatus status,
                                 @JsonProperty("contextId") String contextId, @JsonProperty("final") boolean isFinal,
                                 @JsonProperty("metadata") Map<String, Object> metadata, @JsonProperty("type") String type) {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("status", status);
        Assert.checkNotNullParam("contextId", contextId);
        this.id = id;
        this.status = status;
        this.contextId = contextId;
        this.isFinal = isFinal;
        this.metadata = metadata;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getContextId() {
        return contextId;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String getType() {
        return type;
    }

    public static class Builder {
        private String id;
        private TaskStatus status;
        private String contextId;
        private boolean isFinal;
        private Map<String, Object> metadata;
        private String type;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder isFinal(boolean isFinal) {
            this.isFinal = isFinal;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public TaskStatusUpdateEvent build() {
            return new TaskStatusUpdateEvent(id, status, contextId, isFinal, metadata);
        }
    }
}
