package io.a2a.spec;

import static io.a2a.spec.A2A.JSONRPC_VERSION;
import static io.a2a.spec.A2A.SET_TASK_PUSH_NOTIFICATION_REQUEST;
import static io.a2a.util.Utils.OBJECT_MAPPER;
import static io.a2a.util.Utils.defaultIfNull;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * Used to set a task push notification request.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SetTaskPushNotificationRequest extends JSONRPCRequest {

    @JsonCreator
    public SetTaskPushNotificationRequest(@JsonProperty("jsonrpc") String jsonrpc, @JsonProperty("id") Object id,
                                          @JsonProperty("method") String method, @JsonProperty("params") TaskPushNotificationConfig params) {
        Assert.checkNotNullParam("method", method);
        Assert.checkNotNullParam("params", params);

        if (! method.equals(SET_TASK_PUSH_NOTIFICATION_REQUEST)) {
            throw new IllegalArgumentException("Invalid SetTaskPushNotificationRequest method");
        }

        Map<String, Object> paramsMap = OBJECT_MAPPER.convertValue(params, Map.class);
        this.jsonrpc = defaultIfNull(jsonrpc, JSONRPC_VERSION);
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.method = method;
        this.params = paramsMap;
    }

    public static class Builder {
        private String jsonrpc;
        private Object id;
        private String method;
        private TaskPushNotificationConfig params;

        public SetTaskPushNotificationRequest.Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
            return this;
        }

        public SetTaskPushNotificationRequest.Builder id(Object id) {
            this.id = id;
            return this;
        }

        public SetTaskPushNotificationRequest.Builder method(String method) {
            this.method = method;
            return this;
        }

        public SetTaskPushNotificationRequest.Builder params(TaskPushNotificationConfig params) {
            this.params = params;
            return this;
        }

        public SetTaskPushNotificationRequest build() {
            return new SetTaskPushNotificationRequest(jsonrpc, id, method, params);
        }
    }
}
