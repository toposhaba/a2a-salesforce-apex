package io.a2a.spec;

import static io.a2a.spec.A2A.SEND_TASK_REQUEST;
import static io.a2a.util.Utils.OBJECT_MAPPER;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * Used to initiate a task.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SendTaskRequest extends JSONRPCRequest {

    public SendTaskRequest(String jsonrpc, Object id, String method, TaskSendParams params) {
        Assert.checkNotNullParam("jsonrpc", jsonrpc);
        Assert.checkNotNullParam("method", method);
        Assert.checkNotNullParam("params", params);

        if (! method.equals(SEND_TASK_REQUEST)) {
            throw new IllegalArgumentException("Invalid SendTaskRequest method");
        }

        Map<String, Object> paramsMap = OBJECT_MAPPER.convertValue(params, Map.class);
        this.jsonrpc = jsonrpc;
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.method = method;
        this.params = paramsMap;
    }

    public static class Builder {
        private String jsonrpc;
        private Object id;
        private String method;
        private TaskSendParams params;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder params(TaskSendParams params) {
            this.params = params;
            return this;
        }

        public SendTaskRequest build() {
            return new SendTaskRequest(jsonrpc, id, method, params);
        }
    }
}
