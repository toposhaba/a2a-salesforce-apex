package io.a2a.spec;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * Represents a JSONRPC request.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public sealed class JSONRPCRequest implements JSONRPCMessage permits SendTaskRequest, SendTaskStreamingRequest,
        GetTaskRequest, CancelTaskRequest, SetTaskPushNotificationRequest, GetTaskPushNotificationRequest,
        TaskResubscriptionRequest {

    protected String jsonrpc;
    protected Object id;
    protected String method;
    protected Map<String, Object> params;

    public JSONRPCRequest() {
    }

    public JSONRPCRequest(String jsonrpc, Object id, String method, Map<String, Object> params) {
        Assert.checkNotNullParam("jsonrpc", jsonrpc);
        Assert.checkNotNullParam("method", method);
        this.jsonrpc = jsonrpc;
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.method = method;
        this.params = params;
    }

    @Override
    public String jsonrpc() {
        return this.jsonrpc;
    }

    @Override
    public Object id() {
        return this.id;
    }

    public String method() {
        return this.method;
    }

    public Map<String, Object> params() {
        return this.params;
    }
}
