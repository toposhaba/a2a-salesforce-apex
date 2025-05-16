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