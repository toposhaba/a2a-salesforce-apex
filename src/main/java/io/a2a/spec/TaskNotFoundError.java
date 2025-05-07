package io.a2a.spec;

public class TaskNotFoundError extends JSONRPCError {
    public TaskNotFoundError(int code, String message, Object data) {
        super(code, message, data);
    }
}
