package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.a2a.util.Assert;

/**
 * Represents a JSONRPC error.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JSONRPCError {

    private final Integer code;
    private final String message;
    private final Object data;

    @JsonCreator
    public JSONRPCError(
            @JsonProperty("code") Integer code,
            @JsonProperty("message") String message,
            @JsonProperty("data") Object data) {
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
