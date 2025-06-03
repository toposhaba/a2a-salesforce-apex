package io.a2a.spec;

import static io.a2a.spec.A2A.CANCEL_TASK_METHOD;
import static io.a2a.spec.A2A.GET_TASK_METHOD;
import static io.a2a.spec.A2A.GET_TASK_PUSH_NOTIFICATION_CONFIG_METHOD;
import static io.a2a.spec.A2A.SEND_MESSAGE_METHOD;
import static io.a2a.spec.A2A.SET_TASK_PUSH_NOTIFICATION_CONFIG_METHOD;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a non-streaming JSON-RPC request.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "method",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GetTaskRequest.class, name = GET_TASK_METHOD),
        @JsonSubTypes.Type(value = CancelTaskRequest.class, name = CANCEL_TASK_METHOD),
        @JsonSubTypes.Type(value = SetTaskPushNotificationConfigRequest.class, name = SET_TASK_PUSH_NOTIFICATION_CONFIG_METHOD),
        @JsonSubTypes.Type(value = GetTaskPushNotificationConfigRequest.class, name = GET_TASK_PUSH_NOTIFICATION_CONFIG_METHOD),
        @JsonSubTypes.Type(value = SendMessageRequest.class, name = SEND_MESSAGE_METHOD),
})
public abstract sealed class NonStreamingJSONRPCRequest<T> extends JSONRPCRequest<T> permits GetTaskRequest,
        CancelTaskRequest, SetTaskPushNotificationConfigRequest, GetTaskPushNotificationConfigRequest,
        SendMessageRequest {
}
