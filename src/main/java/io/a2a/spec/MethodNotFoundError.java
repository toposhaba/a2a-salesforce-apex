package io.a2a.spec;

public class MethodNotFoundError extends JSONRPCError {
    public MethodNotFoundError(int code, String message, Object data) {
        super(code, message, data);
    }
}
