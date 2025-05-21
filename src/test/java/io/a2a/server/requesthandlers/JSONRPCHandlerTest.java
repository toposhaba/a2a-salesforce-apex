package io.a2a.server.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.events.InMemoryQueueManager;
import io.a2a.server.events.QueueManager;
import io.a2a.server.tasks.InMemoryPushNotifier;
import io.a2a.server.tasks.InMemoryTaskStore;
import io.a2a.server.tasks.PushNotifier;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.Message;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JSONRPCHandlerTest {
    private static final AgentCard CARD = new AgentCard(
            "test-card",
            "A test agent card",
            "http://example.con",
            null,
            "1.0",
            "http://example.con/docs",
            new AgentCapabilities(true, true, true),
            null,
            null,
            null,
            new ArrayList<>());

    private static final Task MINIMAL_TASK = new Task.Builder()
            .id("task-123")
            .contextId("session-xyz")
            .status(new TaskStatus(TaskState.SUBMITTED))
            .build();

    private static final Message MESSAGE = new Message.Builder()
            .messageId("111")
            .role(Message.Role.AGENT)
            .parts(new TextPart("test message"))
            .build();

    TaskStore taskStore;
    JSONRPCHandler handler;

    @BeforeEach
    public void init() {
        AgentExecutor executor = new AgentExecutor() {
            @Override
            public void execute(RequestContext context, EventQueue eventQueue) {

            }

            @Override
            public void cancel(RequestContext context, EventQueue eventQueue) {

            }
        };
        taskStore = new InMemoryTaskStore();
        QueueManager queueManager = new InMemoryQueueManager();
        PushNotifier pushNotifier = new InMemoryPushNotifier();

        RequestHandler requestHandler = new DefaultRequestHandler(executor, taskStore, queueManager, pushNotifier);
        handler = new JSONRPCHandler(CARD, requestHandler);
    }

    @Test
    public void testOnGetTaskSuccess() throws Exception {
        taskStore.save(MINIMAL_TASK);
        GetTaskRequest request = new GetTaskRequest("1", new TaskQueryParams(MINIMAL_TASK.getId()));
        GetTaskResponse response = handler.onGetTask(request);
        assertEquals(request.getId(), response.getId());
        assertSame(MINIMAL_TASK, response.getResult());
        assertNull(response.getError());
    }

    @Test
    public void testOnGetTaskNotFound() throws Exception {
        GetTaskRequest request = new GetTaskRequest("1", new TaskQueryParams(MINIMAL_TASK.getId()));
        GetTaskResponse response = handler.onGetTask(request);
        assertEquals(request.getId(), response.getId());
        assertInstanceOf(TaskNotFoundError.class, response.getError());
        assertNull(response.getResult());
    }

    @Disabled("Something not working with the queues")
    @Test
    public void testOnCancelTaskSuccess() throws Exception {
        taskStore.save(MINIMAL_TASK);
        CancelTaskRequest request = new CancelTaskRequest("111", new TaskIdParams(MINIMAL_TASK.getId()));
        CancelTaskResponse response = handler.onCancelTask(request);
        assertEquals(request.getId(), response.getId());
        // TODO more checks
    }

    @Disabled
    @Test
    public void testOnCancelTaskNotSupported() {
        // TODO
    }

    @Disabled
    @Test
    public void testOnCancelTaskNotFound() {
        // TODO
    }

    @Disabled
    @Test
    public void testOnMessageNewMessageSuccess() {
        // TODO
    }

    @Disabled
    @Test
    public void testOnMessageNewMessageWithExistingTaskSuccess() {
        // TODO
    }

    @Disabled
    @Test
    public void testOnMessageError() {
        // TODO
    }

    @Disabled
    @Test
    public void testOnMessageStreamNewMessageSuccess() {
        // TODO
    }

    @Disabled
    @Test
    public void testOnMessageStreamNewMessageExistingTaskSuccess() {
        // TODO
    }

    @Disabled
    @Test
    public void testSetPushNotificationSuccess() {
        // TODO
    }

    @Disabled
    @Test
    public void testGetPushNotificationSuccess() {
        // TODO
    }

    @Disabled
    @Test
    public void testOnMessageStreamNewMessageSendPushNotificationSuccess() {
        // TODO
    }

    @Disabled
    @Test
    public void testOnResubscribeExistingTaskSuccess() {

    }

    @Disabled
    @Test
    public void testOnResubscribeNoExistingTaskError() {
        
    }
}
