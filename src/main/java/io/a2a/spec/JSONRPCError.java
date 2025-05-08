package io.a2a.spec;

import io.a2a.util.Assert;

/**
 * Represents a JSONRPC error.
 */
public class JSONRPCError {

    private final Integer code;
    private final String message;
    private final Object data;

    public JSONRPCError(Integer code, String message, Object data) {
        Assert.checkNotNullParam("code", code);
        Assert.checkNotNullParam("message", message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * Gets the error code
     *
     * @return the error code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Gets the error message
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the data associated with the error.
     *
     * @return the data. May be {@code null}
     */
    public Object getData() {
        return data;
    }
}
