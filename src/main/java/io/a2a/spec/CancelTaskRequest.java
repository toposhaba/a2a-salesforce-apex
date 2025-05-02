package io.a2a.spec;

import static io.a2a.spec.A2A.CANCEL_TASK_REQUEST;
import static io.a2a.util.Utils.OBJECT_MAPPER;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * A request that can be used to cancel a task.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CancelTaskRequest extends JSONRPCRequest {

    public CancelTaskRequest(String jsonrpc, Object id, String method, TaskIdParams params) {
        Assert.checkNotNullParam("jsonrpc", jsonrpc);
        Assert.checkNotNullParam("method", method);
        Assert.checkNotNullParam("params", params);

        if (! method.equals(CANCEL_TASK_REQUEST)) {
            throw new IllegalArgumentException("Invalid CancelTaskRequest method");
        }

        Map<String, Object> paramsMap = OBJECT_MAPPER.convertValue(params, Map.class);
        this.jsonrpc = jsonrpc;
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.method = method;
        this.params = paramsMap;
    }
}
