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
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;
import io.a2a.spec.UnsupportedOperationError;
import org.junit.jupiter.api.AfterEach;
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
    AgentExecutorMethod agentExecutorExecute;
    AgentExecutorMethod agentExecutorCancel;


    @BeforeEach
    public void init() {
        AgentExecutor executor = new AgentExecutor() {
            @Override
            public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                if (agentExecutorExecute != null) {
                    agentExecutorExecute.invoke(context, eventQueue);
                }
            }

            @Override
            public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                if (agentExecutorCancel != null) {
                    agentExecutorCancel.invoke(context, eventQueue);
                }
            }
        };

        taskStore = new InMemoryTaskStore();
        QueueManager queueManager = new InMemoryQueueManager();
        PushNotifier pushNotifier = new InMemoryPushNotifier();

        RequestHandler requestHandler = new DefaultRequestHandler(executor, taskStore, queueManager, pushNotifier);
        handler = new JSONRPCHandler(CARD, requestHandler);
    }

    @AfterEach
    public void cleanup() {
        agentExecutorExecute = null;
        agentExecutorCancel = null;
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

    @Test
    public void testOnCancelTaskSuccess() throws Exception {
        taskStore.save(MINIMAL_TASK);

        agentExecutorCancel = (context, eventQueue) -> {
            // We need to cancel the task or the EventConsumer never finds a 'final' event.
            // Looking at the Python implementation, they typically use AgentExecutors that
            // don't support cancellation. So my theory is the Agent updates the task to the CANCEL status
            Task task = context.getTask();
            Task updated = new Task.Builder(task)
                    .status(new TaskStatus(TaskState.CANCELED))
                    .build();

            eventQueue.enqueueEvent(updated);
        };

        CancelTaskRequest request = new CancelTaskRequest("111", new TaskIdParams(MINIMAL_TASK.getId()));
        CancelTaskResponse response = handler.onCancelTask(request);

        assertNull(response.getError());
        assertEquals(request.getId(), response.getId());
        Task task = response.getResult();
        assertEquals(MINIMAL_TASK.getId(), task.getId());
        assertEquals(MINIMAL_TASK.getContextId(), task.getContextId());
        assertEquals(TaskState.CANCELED, task.getStatus().state());
    }

    @Test
    public void testOnCancelTaskNotSupported() {
        taskStore.save(MINIMAL_TASK);
        agentExecutorCancel = (context, eventQueue) -> {
            throw new UnsupportedOperationError();
        };

        CancelTaskRequest request = new CancelTaskRequest("1", new TaskIdParams(MINIMAL_TASK.getId()));
        CancelTaskResponse response = handler.onCancelTask(request);
        assertEquals(request.getId(), response.getId());
        assertNull(response.getResult());
        assertInstanceOf(UnsupportedOperationError.class, response.getError());
    }

    @Test
    public void testOnCancelTaskNotFound() {
        CancelTaskRequest request = new CancelTaskRequest("1", new TaskIdParams(MINIMAL_TASK.getId()));
        CancelTaskResponse response = handler.onCancelTask(request);
        assertEquals(request.getId(), response.getId());
        assertNull(response.getResult());
        assertInstanceOf(TaskNotFoundError.class, response.getError());
    }

    @Test
    public void testOnMessageNewMessageSuccess() {
        agentExecutorExecute = (context, eventQueue) -> {
            eventQueue.enqueueEvent(context.getMessage());
        };
        Message message = new Message.Builder(MESSAGE)
                .taskId(MINIMAL_TASK.getId())
                .contextId(MINIMAL_TASK.getContextId())
                .build();
        SendMessageRequest request = new SendMessageRequest("1", new MessageSendParams("1", message, null, null));
        SendMessageResponse response = handler.onMessageSend(request);
        assertNull(response.getError());
        // The Python implementation returns a Task here, but then again they are using hardcoded mocks and
        // bypassing the whole EventQueue.
        // If we were to send a Task in agentExecutorExecute EventConsumer.consumeAll() would not exit due to
        // the Task not having a 'final' state
        assertSame(message, response.getResult());
    }

    @Test
    public void testOnMessageNewMessageWithExistingTaskSuccess() {
        taskStore.save(MINIMAL_TASK);
        agentExecutorExecute = (context, eventQueue) -> {
            eventQueue.enqueueEvent(context.getMessage());
        };
        Message message = new Message.Builder(MESSAGE)
                .taskId(MINIMAL_TASK.getId())
                .contextId(MINIMAL_TASK.getContextId())
                .build();
        SendMessageRequest request = new SendMessageRequest("1", new MessageSendParams("1", message, null, null));
        SendMessageResponse response = handler.onMessageSend(request);
        assertNull(response.getError());
        // The Python implementation returns a Task here, but then again they are using hardcoded mocks and
        // bypassing the whole EventQueue.
        // If we were to send a Task in agentExecutorExecute EventConsumer.consumeAll() would not exit due to
        // the Task not having a 'final' state
        assertSame(message, response.getResult());

    }

    @Disabled
    @Test
    public void testOnMessageError() {
        // TODO This test is disabled because sending an Error doesn't end up breaking out of the
        //  EventConsumer.consumeAll() loop, since Errors are currently not considered a 'final' state.
        //  The Python implementation uses a mock for EventConsumer.consumeAll() which is why their tests pass
        agentExecutorExecute = (context, eventQueue) -> {
            eventQueue.enqueueEvent(new UnsupportedOperationError());
        };
        Message message = new Message.Builder(MESSAGE)
                .taskId(MINIMAL_TASK.getId())
                .contextId(MINIMAL_TASK.getContextId())
                .build();
        SendMessageRequest request = new SendMessageRequest("1", new MessageSendParams("1", message, null, null));
        SendMessageResponse response = handler.onMessageSend(request);
        assertInstanceOf(UnsupportedOperationError.class, response.getResult());
        assertNull(response.getResult());

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

    private interface AgentExecutorMethod {
        void invoke(RequestContext context, EventQueue eventQueue) throws JSONRPCError;
    }

}
