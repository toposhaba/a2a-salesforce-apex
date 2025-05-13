package io.a2a.spec;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * A central unit of work.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Task(String id, String contextId, TaskStatus status, List<Artifact> artifacts,
                   List<Message> history, Map<String, Object> metadata) {

    public Task {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("contextId", contextId);
        Assert.checkNotNullParam("status", status);
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
