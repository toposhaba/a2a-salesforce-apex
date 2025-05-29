package io.a2a.server.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.Event;
import io.a2a.server.events.EventConsumer;
import io.a2a.server.events.EventQueue;
import io.a2a.server.events.InMemoryQueueManager;
import io.a2a.server.events.QueueManager;
import io.a2a.server.tasks.InMemoryPushNotifier;
import io.a2a.server.tasks.InMemoryTaskStore;
import io.a2a.server.tasks.PushNotifier;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Artifact;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskPushNotificationRequest;
import io.a2a.spec.GetTaskPushNotificationResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.PushNotificationConfig;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.SendStreamingMessageRequest;
import io.a2a.spec.SendStreamingMessageResponse;
import io.a2a.spec.SetTaskPushNotificationRequest;
import io.a2a.spec.SetTaskPushNotificationResponse;
import io.a2a.spec.StreamingEventType;
import io.a2a.spec.Task;
import io.a2a.spec.TaskArtifactUpdateEvent;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;
import io.a2a.spec.TextPart;
import io.a2a.spec.UnsupportedOperationError;
import mutiny.zero.ZeroPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

public class JSONRPCHandlerTest {

    private static final AgentCard CARD = createAgentCard(true, true, true);

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
    RequestHandler requestHandler;
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

        requestHandler = new DefaultRequestHandler(executor, taskStore, queueManager, pushNotifier);
    }

    @AfterEach
    public void cleanup() {
        agentExecutorExecute = null;
        agentExecutorCancel = null;
    }

    @Test
    public void testOnGetTaskSuccess() throws Exception {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        taskStore.save(MINIMAL_TASK);
        GetTaskRequest request = new GetTaskRequest("1", new TaskQueryParams(MINIMAL_TASK.getId()));
        GetTaskResponse response = handler.onGetTask(request);
        assertEquals(request.getId(), response.getId());
        assertSame(MINIMAL_TASK, response.getResult());
        assertNull(response.getError());
    }

    @Test
    public void testOnGetTaskNotFound() throws Exception {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        GetTaskRequest request = new GetTaskRequest("1", new TaskQueryParams(MINIMAL_TASK.getId()));
        GetTaskResponse response = handler.onGetTask(request);
        assertEquals(request.getId(), response.getId());
        assertInstanceOf(TaskNotFoundError.class, response.getError());
        assertNull(response.getResult());
    }

    @Test
    public void testOnCancelTaskSuccess() throws Exception {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
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
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
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
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        CancelTaskRequest request = new CancelTaskRequest("1", new TaskIdParams(MINIMAL_TASK.getId()));
        CancelTaskResponse response = handler.onCancelTask(request);
        assertEquals(request.getId(), response.getId());
        assertNull(response.getResult());
        assertInstanceOf(TaskNotFoundError.class, response.getError());
    }

    @Test
    public void testOnMessageNewMessageSuccess() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
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
        //
        // See testOnMessageNewMessageSuccessMocks() for a test more similar to the Python implementation
        assertSame(message, response.getResult());
    }

    @Test
    public void testOnMessageNewMessageSuccessMocks() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);

        Message message = new Message.Builder(MESSAGE)
                .taskId(MINIMAL_TASK.getId())
                .contextId(MINIMAL_TASK.getContextId())
                .build();

        SendMessageRequest request = new SendMessageRequest("1", new MessageSendParams("1", message, null, null));
        SendMessageResponse response;
        try (MockedConstruction<EventConsumer> mocked = Mockito.mockConstruction(
                EventConsumer.class,
                (mock, context) -> {Mockito.doReturn(ZeroPublisher.fromItems(MINIMAL_TASK)).when(mock).consumeAll();})){
            response = handler.onMessageSend(request);
        }
        assertNull(response.getError());
        assertSame(MINIMAL_TASK, response.getResult());
    }

    @Test
    public void testOnMessageNewMessageWithExistingTaskSuccess() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
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
        //
        // See testOnMessageNewMessageWithExistingTaskSuccessMocks() for a test more similar to the Python implementation
        assertSame(message, response.getResult());
    }

    @Test
    public void testOnMessageNewMessageWithExistingTaskSuccessMocks() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        taskStore.save(MINIMAL_TASK);

        Message message = new Message.Builder(MESSAGE)
                .taskId(MINIMAL_TASK.getId())
                .contextId(MINIMAL_TASK.getContextId())
                .build();
        SendMessageRequest request = new SendMessageRequest("1", new MessageSendParams("1", message, null, null));
        SendMessageResponse response;
        try (MockedConstruction<EventConsumer> mocked = Mockito.mockConstruction(
                EventConsumer.class,
                (mock, context) -> {
                    Mockito.doReturn(ZeroPublisher.fromItems(MINIMAL_TASK)).when(mock).consumeAll();})){
            response = handler.onMessageSend(request);
        }
        assertNull(response.getError());
        assertSame(MINIMAL_TASK, response.getResult());

    }

    @Test
    public void testOnMessageError() {
        // See testMessageOnErrorMocks() for a test more similar to the Python implementation, using mocks for
        // EventConsumer.consumeAll()
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        agentExecutorExecute = (context, eventQueue) -> {
            eventQueue.enqueueEvent(new UnsupportedOperationError());
        };
        Message message = new Message.Builder(MESSAGE)
                .taskId(MINIMAL_TASK.getId())
                .contextId(MINIMAL_TASK.getContextId())
                .build();
        SendMessageRequest request = new SendMessageRequest(
                "1", new MessageSendParams("1", message, null, null));
        SendMessageResponse response = handler.onMessageSend(request);
        assertInstanceOf(UnsupportedOperationError.class, response.getError());
        assertNull(response.getResult());
    }

    @Test
    public void testOnMessageErrorMocks() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        Message message = new Message.Builder(MESSAGE)
                .taskId(MINIMAL_TASK.getId())
                .contextId(MINIMAL_TASK.getContextId())
                .build();
        SendMessageRequest request = new SendMessageRequest(
                "1", new MessageSendParams("1", message, null, null));
        SendMessageResponse response;
        try (MockedConstruction<EventConsumer> mocked = Mockito.mockConstruction(
                EventConsumer.class,
                (mock, context) -> {
                    Mockito.doReturn(ZeroPublisher.fromItems(new UnsupportedOperationError())).when(mock).consumeAll();})){
            response = handler.onMessageSend(request);
        }

        assertInstanceOf(UnsupportedOperationError.class, response.getError());
        assertNull(response.getResult());
    }

    @Test
    public void testOnMessageStreamNewMessageSuccess() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        agentExecutorExecute = (context, eventQueue) -> {
            eventQueue.enqueueEvent(context.getTask() != null ? context.getTask() : context.getMessage());
        };

        Message message = new Message.Builder(MESSAGE)
            .taskId(MINIMAL_TASK.getId())
            .contextId(MINIMAL_TASK.getContextId())
            .build();

        SendStreamingMessageRequest request = new SendStreamingMessageRequest(
                "1", new MessageSendParams("1", message, null, null));
        Flow.Publisher<SendStreamingMessageResponse> response = handler.onMessageSendStream(request);

        List<StreamingEventType> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        response.subscribe(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(SendStreamingMessageResponse item) {
                results.add(item.getResult());
                subscription.request(1);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });

        // The Python implementation has several events emitted since it uses mocks. Also, in the
        // implementation, a Message is considered a 'final' Event in EventConsumer.consumeAll()
        // so there would be no more Events.
        //
        // See testOnMessageStreamNewMessageSuccessMocks() for a test more similar to the Python implementation
        assertEquals(1, results.size());
        assertSame(message, results.get(0));
    }

    @Test
    public void testOnMessageStreamNewMessageSuccessMocks() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);

        // This is used to send events from a mock
        List<Event> events = List.of(
                MINIMAL_TASK,
                new TaskArtifactUpdateEvent.Builder()
                        .taskId(MINIMAL_TASK.getId())
                        .contextId(MINIMAL_TASK.getContextId())
                        .artifact(new Artifact.Builder()
                                .artifactId("art1")
                                .parts(new TextPart("text"))
                                .build())
                        .build(),
                new TaskStatusUpdateEvent.Builder()
                        .taskId(MINIMAL_TASK.getId())
                        .contextId(MINIMAL_TASK.getContextId())
                        .status(new TaskStatus(TaskState.COMPLETED))
                        .build());

        Message message = new Message.Builder(MESSAGE)
            .taskId(MINIMAL_TASK.getId())
            .contextId(MINIMAL_TASK.getContextId())
            .build();

        SendStreamingMessageRequest request = new SendStreamingMessageRequest(
                "1", new MessageSendParams("1", message, null, null));
        Flow.Publisher<SendStreamingMessageResponse> response;
        try (MockedConstruction<EventConsumer> mocked = Mockito.mockConstruction(
                EventConsumer.class,
                (mock, context) -> {
                    Mockito.doReturn(ZeroPublisher.fromIterable(events)).when(mock).consumeAll();})){
            response = handler.onMessageSendStream(request);
        }

        List<Event> results = new ArrayList<>();

        response.subscribe(new Flow.Subscriber<SendStreamingMessageResponse>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(SendStreamingMessageResponse item) {
                results.add((Event) item.getResult());
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        assertEquals(events, results);
    }

    @Test
    public void testOnMessageStreamNewMessageExistingTaskSuccess() throws Exception {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        agentExecutorExecute = (context, eventQueue) -> {
            eventQueue.enqueueEvent(context.getTask() != null ? context.getTask() : context.getMessage());
        };

        Task task = new Task.Builder(MINIMAL_TASK)
                .history(new ArrayList<>())
                .build();
        taskStore.save(task);

        Message message = new Message.Builder(MESSAGE)
            .taskId(task.getId())
            .contextId(task.getContextId())
            .build();


        SendStreamingMessageRequest request = new SendStreamingMessageRequest(
                "1", new MessageSendParams("1", message, null, null));
        Flow.Publisher<SendStreamingMessageResponse> response = handler.onMessageSendStream(request);

        // TODO - this Publisher never completes so we subscribe in a new thread.
        //  I _think_ that is as expected but we need to be sure
        List<StreamingEventType> results = new ArrayList<>();
        AtomicReference<Flow.Subscription> subscriptionRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Executors.newSingleThreadExecutor().execute(() -> {
            response.subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscriptionRef.set(subscription);
                    subscription.request(1);
                }

                @Override
                public void onNext(SendStreamingMessageResponse item) {
                    results.add(item.getResult());
                    subscriptionRef.get().request(1);
                    latch.countDown();
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriptionRef.get().cancel();
                }

                @Override
                public void onComplete() {
                    subscriptionRef.get().cancel();
                }
            });
        });

        latch.await(1, TimeUnit.SECONDS);
        subscriptionRef.get().cancel();
        // The Python implementation has several events emitted since it uses mocks.
        //
        // See testOnMessageStreamNewMessageExistingTaskSuccessMocks() for a test more similar to the Python implementation
        Task expected = new Task.Builder(task)
                .history(message)
                .build();
        assertEquals(1, results.size());
        StreamingEventType receivedType = results.get(0);
        assertInstanceOf(Task.class, receivedType);
        Task received = (Task) receivedType;
        assertEquals(expected.getId(), received.getId());
        assertEquals(expected.getContextId(), received.getContextId());
        assertEquals(expected.getStatus(), received.getStatus());
        assertEquals(expected.getHistory(), received.getHistory());
    }

    @Test
    public void testOnMessageStreamNewMessageExistingTaskSuccessMocks() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);

        Task task = new Task.Builder(MINIMAL_TASK)
                .history(new ArrayList<>())
                .build();
        taskStore.save(task);

        // This is used to send events from a mock
        List<Event> events = List.of(
                new TaskArtifactUpdateEvent.Builder()
                        .taskId(task.getId())
                        .contextId(task.getContextId())
                        .artifact(new Artifact.Builder()
                                .artifactId("11")
                                .parts(new TextPart("text"))
                                .build())
                        .build(),
                new TaskStatusUpdateEvent.Builder()
                        .taskId(task.getId())
                        .contextId(task.getContextId())
                        .status(new TaskStatus(TaskState.WORKING))
                        .build());

        Message message = new Message.Builder(MESSAGE)
            .taskId(task.getId())
            .contextId(task.getContextId())
            .build();

        SendStreamingMessageRequest request = new SendStreamingMessageRequest(
                "1", new MessageSendParams("1", message, null, null));
        Flow.Publisher<SendStreamingMessageResponse> response;
        try (MockedConstruction<EventConsumer> mocked = Mockito.mockConstruction(
                EventConsumer.class,
                (mock, context) -> {
                    Mockito.doReturn(ZeroPublisher.fromIterable(events)).when(mock).consumeAll();})){
            response = handler.onMessageSendStream(request);
        }

        List<Event> results = new ArrayList<>();

        // Unlike testOnMessageStreamNewMessageExistingTaskSuccess() the ZeroPublisher.fromIterable()
        // used to mock the events completes once it has sent all the items. So no special thread
        // handling is needed.
        response.subscribe(new Flow.Subscriber<SendStreamingMessageResponse>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(SendStreamingMessageResponse item) {
                results.add((Event) item.getResult());
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });

        assertEquals(events, results);
    }


    @Test
    public void testSetPushNotificationSuccess() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        taskStore.save(MINIMAL_TASK);

        TaskPushNotificationConfig taskPushConfig =
                new TaskPushNotificationConfig(
                        MINIMAL_TASK.getId(), new
                        PushNotificationConfig("http://example.con", null, null));
        SetTaskPushNotificationRequest request = new SetTaskPushNotificationRequest("1", taskPushConfig);
        SetTaskPushNotificationResponse response = handler.setPushNotification(request);
        assertSame(taskPushConfig, response.getResult());
    }

    @Test
    public void testGetPushNotificationSuccess() {
        JSONRPCHandler handler = new JSONRPCHandler(CARD, requestHandler);
        taskStore.save(MINIMAL_TASK);
        agentExecutorExecute = (context, eventQueue) -> {
            eventQueue.enqueueEvent(context.getTask() != null ? context.getTask() : context.getMessage());
        };


        TaskPushNotificationConfig taskPushConfig =
                new TaskPushNotificationConfig(
                        MINIMAL_TASK.getId(), new
                        PushNotificationConfig("http://example.con", null, null));

        SetTaskPushNotificationRequest request = new SetTaskPushNotificationRequest("1", taskPushConfig);
        handler.setPushNotification(request);

        GetTaskPushNotificationRequest getRequest =
                new GetTaskPushNotificationRequest("111", new TaskIdParams(MINIMAL_TASK.getId()));
        GetTaskPushNotificationResponse getResponse = handler.getPushNotification(getRequest);

        assertEquals(taskPushConfig, getResponse.getResult());
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

    private static AgentCard createAgentCard(boolean streaming, boolean pushNotifications, boolean stateTransitionHistory) {
        return new AgentCard(
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
    }

    private interface AgentExecutorMethod {
        void invoke(RequestContext context, EventQueue eventQueue) throws JSONRPCError;
    }

}
