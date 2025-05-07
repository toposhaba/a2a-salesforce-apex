package io.a2a.spec;

public class TaskNotCancelableError extends JSONRPCError {
    public TaskNotCancelableError(int code, String message, Object data) {
        super(code, message, data);
    }
}
