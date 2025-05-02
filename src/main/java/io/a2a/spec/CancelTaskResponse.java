package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A response to a cancel task request.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CancelTaskResponse extends JSONRPCResponse {

    public CancelTaskResponse(String jsonrpc, Object id, Task result, JSONRPCError error) {
        super(jsonrpc, id, result, error);
    }
}
