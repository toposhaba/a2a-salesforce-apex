package io.a2a.spec;

import static io.a2a.spec.A2A.GET_TASK_PUSH_NOTIFICATION_REQUEST;
import static io.a2a.util.Utils.OBJECT_MAPPER;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * A get task push notification request.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GetTaskPushNotificationRequest extends JSONRPCRequest {

    public GetTaskPushNotificationRequest(String jsonrpc, Object id, String method, TaskIdParams params) {
        Assert.checkNotNullParam("jsonrpc", jsonrpc);
        Assert.checkNotNullParam("method", method);


        if (! method.equals(GET_TASK_PUSH_NOTIFICATION_REQUEST)) {
            throw new IllegalArgumentException("Invalid GetTaskPushNotificationRequest method");
        }

        Map<String, Object> paramsMap = OBJECT_MAPPER.convertValue(params, Map.class);
        this.jsonrpc = jsonrpc;
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.method = method;
        this.params = paramsMap;
    }
}
