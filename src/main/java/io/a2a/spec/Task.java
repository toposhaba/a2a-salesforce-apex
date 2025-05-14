package io.a2a.spec;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * A central unit of work.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task implements EventType, StreamingEventType {

    static final String TASK = "task";
    private final String id;
    private final String contextId;
    private final TaskStatus status;
    private final List<Artifact> artifacts;
    private final List<Message> history;
    private final Map<String, Object> metadata;
    private final String type;

    public Task(String id, String contextId, TaskStatus status, List<Artifact> artifacts,
                List<Message> history, Map<String, Object> metadata) {
        this(id, contextId, status, artifacts, history, metadata, TASK);
    }

    @JsonCreator
    public Task(@JsonProperty("id") String id, @JsonProperty("contextId") String contextId, @JsonProperty("status") TaskStatus status,
                @JsonProperty("artifacts") List<Artifact> artifacts, @JsonProperty("history") List<Message> history,
                @JsonProperty("metadata") Map<String, Object> metadata, @JsonProperty("type") String type) {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("contextId", contextId);
        Assert.checkNotNullParam("status", status);
        this.id = id;
        this.contextId = contextId;
        this.status = status;
        this.artifacts = artifacts;
        this.history = history;
        this.metadata = metadata;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getContextId() {
        return contextId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public List<Message> getHistory() {
        return history;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getType() {
        return type;
    }

    public static class Builder {
        private String id;
        private String contextId;
        private TaskStatus status;
        private List<Artifact> artifacts;
        private List<Message> history;
        private Map<String, Object> metadata;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder artifacts(List<Artifact> artifacts) {
            this.artifacts = artifacts;
            return this;
        }

        public Builder history(List<Message> history) {
            this.history = history;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Task build() {
            return new Task(id, contextId, status, artifacts, history, metadata);
        }
    }
}
