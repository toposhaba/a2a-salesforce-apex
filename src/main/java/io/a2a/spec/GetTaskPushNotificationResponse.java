package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A response for a get task push notification request.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GetTaskPushNotificationResponse extends JSONRPCResponse {

    public GetTaskPushNotificationResponse(String jsonrpc, Object id, TaskPushNotificationConfig result, JSONRPCError error) {
        super(jsonrpc, id, result, error);
    }
}
