package io.a2a.spec;

public class MissingAPIKeyError extends Exception {
    public MissingAPIKeyError() {
    }

    public MissingAPIKeyError(String message) {
        super(message);
    }
}
