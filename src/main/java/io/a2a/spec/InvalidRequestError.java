package io.a2a.spec;

public class InvalidRequestError extends JSONRPCError {
    public InvalidRequestError(int code, String message, Object data) {
        super(code, message, data);
    }
}
