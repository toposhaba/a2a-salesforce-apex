package io.a2a.spec;

public class InternalError extends JSONRPCError {
    public InternalError(int code, String message, Object data) {
        super(code, message, data);
    }
}
