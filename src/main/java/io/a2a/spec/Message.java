package io.a2a.spec;

import static io.a2a.spec.A2A.JSONRPC_VERSION;
import static io.a2a.util.Utils.defaultIfNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import io.a2a.util.Assert;

/**
 * An A2A message.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(Role role, List<Part> parts, String messageId, String contextId, String taskId, Map<String, Object> metadata) {

    public Message {
        Assert.checkNotNullParam("parts", parts);
        messageId = messageId == null ? UUID.randomUUID().toString() : messageId;
    }

    public enum Role {
        USER("user"),
        AGENT("agent");

        private String role;

        Role(String role) {
            this.role = role;
        }

        @JsonValue
        public String asString() {
            return this.role;
        }
    }

    public static class Builder {

        private Role role;
        private List<Part> parts;
        private String messageId;
        private String contextId;
        private String taskId;
        private Map<String, Object> metadata;

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder parts(List<Part> parts) {
            this.parts = parts;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Message build() {
            return new Message(role, parts, messageId, contextId, taskId, metadata);
        }
    }
}
