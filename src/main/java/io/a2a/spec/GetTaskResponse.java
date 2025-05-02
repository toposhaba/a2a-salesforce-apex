package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The response for a get task request.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GetTaskResponse extends JSONRPCResponse {

    public GetTaskResponse(String jsonrpc, Object id, Task result, JSONRPCError error) {
        super(jsonrpc, id, result, error);
    }
}
