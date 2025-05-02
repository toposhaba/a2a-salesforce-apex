package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The response after receiving a set task push notification request.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SetTaskPushNotificationResponse extends JSONRPCResponse {

    public SetTaskPushNotificationResponse(String jsonrpc, Object id, TaskPushNotificationConfig result, JSONRPCError error) {
        super(jsonrpc, id, result, error);
    }
}
