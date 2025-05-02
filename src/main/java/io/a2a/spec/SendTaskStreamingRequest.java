package io.a2a.spec;

import static io.a2a.spec.A2A.SEND_TASK_STREAMING_REQUEST;
import static io.a2a.util.Utils.OBJECT_MAPPER;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * Used to initiate a task with streaming.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SendTaskStreamingRequest extends JSONRPCRequest {

    public SendTaskStreamingRequest(String jsonrpc, Object id, String method, TaskSendParams params) {
        Assert.checkNotNullParam("jsonrpc", jsonrpc);
        Assert.checkNotNullParam("method", method);
        Assert.checkNotNullParam("params", params);

        if (! method.equals(SEND_TASK_STREAMING_REQUEST)) {
            throw new IllegalArgumentException("Invalid SendTaskStreamingRequest method");
        }

        Map<String, Object> paramsMap = OBJECT_MAPPER.convertValue(params, Map.class);
        this.jsonrpc = jsonrpc;
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.method = method;
        this.params = paramsMap;
    }
}
