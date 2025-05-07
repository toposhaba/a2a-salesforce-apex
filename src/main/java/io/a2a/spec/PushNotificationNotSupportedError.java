package io.a2a.spec;

public class PushNotificationNotSupportedError extends JSONRPCError {
    public PushNotificationNotSupportedError(int code, String message, Object data) {
        super(code, message, data);
    }
}
