package io.a2a.spec;

import io.a2a.util.Assert;

/**
 * Represents a JSONRPC error.
 */
public record JSONRPCError(int code, String message, Object data) {

    public JSONRPCError {
        Assert.checkNotNullParam("code", code);
        Assert.checkNotNullParam("message", message);
    }
}
