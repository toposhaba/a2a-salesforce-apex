package io.a2a.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.a2a.client.sse.SSEEventListener;
import io.a2a.spec.Artifact;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendConfiguration;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskArtifactUpdateEvent;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatusUpdateEvent;
import io.a2a.spec.TextPart;

public class A2AClientStreamingTest {

    // We're just going to test the core functionality rather than the network streaming
    @Test
    public void testSSEEventListenerHandlesTaskEvent() throws Exception {
        // Create test data
        AtomicBoolean eventHandled = new AtomicBoolean(false);
        
        // Set up event listener
        SSEEventListener listener = new SSEEventListener(
            event -> {
                if (event instanceof Task) {
                    Task task = (Task) event;
                    assertEquals("task-123", task.getId());
                    assertEquals("context-456", task.getContextId());
                    assertEquals(TaskState.WORKING, task.getStatus().state());
                    eventHandled.set(true);
                }
            },
            error -> {},
            () -> {}
        );
        
        // Simulate an event
        String eventData = JsonStreamingMessages.STREAMING_TASK_EVENT.substring(
                JsonStreamingMessages.STREAMING_TASK_EVENT.indexOf("{"));
        listener.onEvent(null, "id", "message", eventData);
        
        // Verify the event was processed
        assertTrue(eventHandled.get(), "Task event should have been handled");
    }
    
    @Test
    public void testSSEEventListenerHandlesMessageEvent() throws Exception {
        // Create test data
        AtomicBoolean eventHandled = new AtomicBoolean(false);
        
        // Set up event listener
        SSEEventListener listener = new SSEEventListener(
            event -> {
                if (event instanceof Message) {
                    Message message = (Message) event;
                    assertEquals("msg-123", message.getMessageId());
                    assertEquals(Message.Role.AGENT, message.getRole());
                    assertEquals("context-456", message.getContextId());
                    assertEquals(1, message.getParts().size());
                    assertEquals(Part.Type.TEXT, message.getParts().get(0).getType());
                    assertEquals("Hello, world!", ((TextPart) message.getParts().get(0)).getText());
                    eventHandled.set(true);
                }
            },
            error -> {},
            () -> {}
        );
        
        // Simulate an event
        String eventData = JsonStreamingMessages.STREAMING_MESSAGE_EVENT.substring(
                JsonStreamingMessages.STREAMING_MESSAGE_EVENT.indexOf("{"));
        listener.onEvent(null, "id", "message", eventData);
        
        // Verify the event was processed
        assertTrue(eventHandled.get(), "Message event should have been handled");
    }

    @Test
    public void testSSEEventListenerHandlesTaskStatusUpdateEventEvent() throws Exception {
        // Create test data
        AtomicBoolean eventHandled = new AtomicBoolean(false);

        // Set up event listener
        SSEEventListener listener = new SSEEventListener(
                event -> {
                    if (event instanceof TaskStatusUpdateEvent) {
                        TaskStatusUpdateEvent taskStatusUpdateEvent = (TaskStatusUpdateEvent) event;
                        assertEquals("1", taskStatusUpdateEvent.getTaskId());
                        assertEquals("2", taskStatusUpdateEvent.getContextId());
                        assertFalse(taskStatusUpdateEvent.isFinal());
                        assertEquals(TaskState.SUBMITTED, taskStatusUpdateEvent.getStatus().state());
                        eventHandled.set(true);
                    }
                },
                error -> {},
                () -> {}
        );

        // Simulate an event
        String eventData = JsonStreamingMessages.STREAMING_STATUS_UPDATE_EVENT.substring(
                JsonStreamingMessages.STREAMING_STATUS_UPDATE_EVENT.indexOf("{"));
        listener.onEvent(null, "id", "message", eventData);

        // Verify the event was processed
        assertTrue(eventHandled.get(), "Status update event should have been handled");
    }

    @Test
    public void testSSEEventListenerHandlesTaskArtifactUpdateEventEvent() throws Exception {
        // Create test data
        AtomicBoolean eventHandled = new AtomicBoolean(false);

        // Set up event listener
        SSEEventListener listener = new SSEEventListener(
                event -> {
                    if (event instanceof TaskArtifactUpdateEvent) {
                        TaskArtifactUpdateEvent taskArtifactUpdateEvent = (TaskArtifactUpdateEvent) event;
                        assertEquals("1", taskArtifactUpdateEvent.getTaskId());
                        assertEquals("2", taskArtifactUpdateEvent.getContextId());
                        assertFalse(taskArtifactUpdateEvent.isAppend());
                        assertTrue(taskArtifactUpdateEvent.isLastChunk());
                        Artifact artifact = taskArtifactUpdateEvent.getArtifact();
                        assertEquals("artifact-1", artifact.artifactId());
                        assertEquals(1, artifact.parts().size());
                        assertEquals(Part.Type.TEXT, artifact.parts().get(0).getType());
                        assertEquals("Why did the chicken cross the road? To get to the other side!", ((TextPart) artifact.parts().get(0)).getText());
                        eventHandled.set(true);
                    }
                },
                error -> {},
                () -> {}
        );

        // Simulate an event
        String eventData = JsonStreamingMessages.STREAMING_ARTIFACT_UPDATE_EVENT.substring(
                JsonStreamingMessages.STREAMING_ARTIFACT_UPDATE_EVENT.indexOf("{"));
        listener.onEvent(null, "id", "message", eventData);

        // Verify the event was processed
        assertTrue(eventHandled.get(), "Artifact update event should have been handled");
    }


    @Test
    public void testSSEEventListenerHandlesErrorEvent() throws Exception {
        // Create test data
        AtomicBoolean errorHandled = new AtomicBoolean(false);
        
        // Set up event listener
        SSEEventListener listener = new SSEEventListener(
            event -> {},
            error -> {
                assertEquals(-32602, error.getCode());
                assertEquals("Invalid parameters", error.getMessage());
                assertEquals("Missing required field", error.getData());
                errorHandled.set(true);
            },
            () -> {}
        );
        
        // Simulate an event
        String eventData = JsonStreamingMessages.STREAMING_ERROR_EVENT.substring(
                JsonStreamingMessages.STREAMING_ERROR_EVENT.indexOf("{"));
        listener.onEvent(null, "id", "message", eventData);
        
        // Verify the error was processed
        assertTrue(errorHandled.get(), "Error event should have been handled");
    }
    
    @Test
    public void testSendStreamingMessageParams() {
        // The goal here is just to verify the correct parameters are being used
        // This is a unit test of the parameter construction, not the streaming itself
        
        Message message = new Message.Builder()
                .role(Message.Role.USER)
                .parts(Collections.singletonList(new TextPart("test message")))
                .contextId("context-test")
                .messageId("message-test")
                .build();
        
        MessageSendConfiguration configuration = new MessageSendConfiguration.Builder()
                .acceptedOutputModes(List.of("text"))
                .blocking(false)
                .build();
        
        MessageSendParams params = new MessageSendParams.Builder()
                .id("test-id")
                .message(message)
                .configuration(configuration)
                .build();
        
        assertNotNull(params);
        assertEquals("test-id", params.id());
        assertEquals(message, params.message());
        assertEquals(configuration, params.configuration());
        assertEquals(Message.Role.USER, params.message().getRole());
        assertEquals("test message", ((TextPart) params.message().getParts().get(0)).getText());
    }
} 