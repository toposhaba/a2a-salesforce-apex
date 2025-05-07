package io.a2a.spec;

public class InvalidParamsError extends JSONRPCError {
    public InvalidParamsError(int code, String message, Object data) {
        super(code, message, data);
    }
}
