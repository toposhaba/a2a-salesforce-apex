package io.a2a.spec;

import static io.a2a.spec.A2A.SEND_STREAMING_MESSAGE_METHOD;
import static io.a2a.spec.A2A.SEND_TASK_RESUBSCRIPTION_METHOD;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a streaming JSON-RPC request.
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
        @JsonSubTypes.Type(value = TaskResubscriptionRequest.class, name = SEND_TASK_RESUBSCRIPTION_METHOD),
        @JsonSubTypes.Type(value = SendStreamingMessageRequest.class, name = SEND_STREAMING_MESSAGE_METHOD)
})
public abstract sealed class StreamingJSONRPCRequest<T> extends JSONRPCRequest<T> permits TaskResubscriptionRequest,
        SendStreamingMessageRequest {

}
