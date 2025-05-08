package io.a2a.spec;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * Used to specify parameters when initiating a task.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskSendParams(String id, String sessionId, Message message, List<String> acceptedOutputModes,
                             PushNotificationConfig pushNotification, Integer historyLength, Map<String, Object> metadata) {

    public TaskSendParams {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("message", message);
        sessionId = sessionId == null ? UUID.randomUUID().toString() : sessionId;
    }

    public static class Builder {
        String id;
        String sessionId;
        Message message;
        List<String> acceptedOutputModes;
        PushNotificationConfig pushNotification;
        Integer historyLength;
        Map<String, Object> metadata;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder message(Message message) {
            this.message = message;
            return this;
        }

        public Builder acceptedOutputModes(List<String> acceptedOutputModes) {
            this.acceptedOutputModes = acceptedOutputModes;
            return this;
        }

        public Builder pushNotification(PushNotificationConfig pushNotification) {
            this.pushNotification = pushNotification;
            return this;
        }

        public Builder historyLength(Integer historyLength) {
            this.historyLength = historyLength;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public TaskSendParams build() {
            return new TaskSendParams(id, sessionId, message, acceptedOutputModes, pushNotification, historyLength, metadata);
        }
    }
}
