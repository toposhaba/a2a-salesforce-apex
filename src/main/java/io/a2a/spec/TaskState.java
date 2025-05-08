package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents the state of a task.
 */
public enum TaskState {
    SUBMITTED("submitted"),
    WORKING("working"),
    INPUT_REQUIRED("input-required"),
    COMPLETED("completed"),
    CANCELED("canceled"),
    FAILED("failed"),
    UNKNOWN("unknown");

    private final String state;

    TaskState(String state) {
        this.state = state;
    }

    public String asString() {
        return state;
    }

    @JsonCreator
    public static TaskState fromString(String state) {
        switch (state) {
            case "submitted":
                return SUBMITTED;
            case "working":
                return WORKING;
            case "input-required":
                return INPUT_REQUIRED;
            case "completed":
                return COMPLETED;
            case "canceled":
                return CANCELED;
            case "failed":
                return FAILED;
            case "unknown":
                return UNKNOWN;
            default:
                throw new IllegalArgumentException("Invalid TaskState: " + state);
        }
    }
}