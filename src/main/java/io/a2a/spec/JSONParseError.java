package io.a2a.spec;

public class JSONParseError extends JSONRPCError {
    public JSONParseError(int code, String message, Object data) {
        super(code, message, data);
    }
}
