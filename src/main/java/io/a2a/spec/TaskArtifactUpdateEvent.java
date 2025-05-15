package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * A task artifact update event.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskArtifactUpdateEvent implements EventType, StreamingEventType {

    static final String ARTIFACT_UPDATE = "artifact-update";
    private final String id;
    private final Artifact artifact;
    private final String contextId;
    private final Map<String, Object> metadata;
    private final String type;

    public TaskArtifactUpdateEvent(String id, Artifact artifact, String contextId, Map<String, Object> metadata) {
        this(id, artifact, contextId, metadata, ARTIFACT_UPDATE);
    }

    @JsonCreator
    public TaskArtifactUpdateEvent(@JsonProperty("id") String id, @JsonProperty("artifact") Artifact artifact,
                                   @JsonProperty("contextId") String contextId, @JsonProperty("metadata") Map<String, Object> metadata,
                                   @JsonProperty("type") String type) {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("artifact", artifact);
        Assert.checkNotNullParam("contextId", contextId);
        this.id = id;
        this.artifact = artifact;
        this.contextId = contextId;
        this.metadata = metadata;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public String getContextId() {
        return contextId;
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
        private Artifact artifact;
        private String contextId;
        private Map<String, Object> metadata;
        private String type;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder artifact(Artifact artifact) {
            this.artifact = artifact;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
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

        public TaskArtifactUpdateEvent build() {
            return new TaskArtifactUpdateEvent(id, artifact, contextId, metadata);
        }
    }
}
