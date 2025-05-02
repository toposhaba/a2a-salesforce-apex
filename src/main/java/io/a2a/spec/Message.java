package io.a2a.spec;

import java.util.List;
import java.util.Map;

import io.a2a.util.Assert;

/**
 * An A2A message.
 */
public record Message(Role role, List<Part> parts, Map<String, Object> metadata) {

    public Message {
        Assert.checkNotNullParam("parts", parts);
    }

    public enum Role {
        USER("user"),
        AGENT("agent");

        private String role;

        Role(String role) {
            this.role = role;
        }

        public String asString() {
            return this.role;
        }
    }

    public static class Builder {

        private Role role;
        private List<Part> parts;
        private Map<String, Object> metadata;

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder parts(List<Part> parts) {
            this.parts = parts;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Message build() {
            return new Message(role, parts, metadata);
        }
    }
}
