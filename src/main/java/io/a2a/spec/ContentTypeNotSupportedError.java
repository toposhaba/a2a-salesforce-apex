package io.a2a.spec;

public class ContentTypeNotSupportedError extends JSONRPCError {
    public ContentTypeNotSupportedError(int code, String message, Object data) {
        super(code, message, data);
    }
}
