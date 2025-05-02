package io.a2a.spec;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * Represents a JSONRPC response.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public sealed class JSONRPCResponse implements JSONRPCMessage permits SendTaskResponse, SendTaskStreamingResponse,
        GetTaskResponse, CancelTaskResponse, SetTaskPushNotificationResponse, GetTaskPushNotificationResponse {

    @JsonProperty("jsonrpc")
    String jsonrpc;

    @JsonProperty("id")
    Object id;

    @JsonProperty("result")
    Object result;

    @JsonProperty("error")
    JSONRPCError error;

    JSONRPCResponse() {
    }

    public JSONRPCResponse(String jsonrpc, Object id, Object result, JSONRPCError error) {
        Assert.checkNotNullParam("jsonrpc", jsonrpc);
        this.jsonrpc = jsonrpc;
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.result = result;
        this.error = error;
    }

    public String jsonrpc() {
        return this.jsonrpc;
    }

    public Object id() {
        return this.id;
    }

    public Object result() {
        return this.result;
    }

    public JSONRPCError error() {
        return this.error;
    }
}
