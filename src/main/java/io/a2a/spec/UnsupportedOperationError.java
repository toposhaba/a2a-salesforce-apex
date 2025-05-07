package io.a2a.spec;

public class UnsupportedOperationError extends JSONRPCError {

    public UnsupportedOperationError(int code, String message, Object data) {
        super(code, message, data);
    }
}
