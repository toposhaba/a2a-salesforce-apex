/**
 * Enum representing possible states of an A2A task
 * Converted from Java: io.a2a.spec.TaskState
 */
public enum A2ATaskState {
    SUBMITTED,   // Task has been submitted but not yet started
    RUNNING,     // Task is currently being processed
    COMPLETED,   // Task has completed successfully
    FAILED,      // Task has failed
    CANCELLED    // Task has been cancelled
}