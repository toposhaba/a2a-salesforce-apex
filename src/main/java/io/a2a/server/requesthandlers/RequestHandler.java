package io.a2a.server.requesthandlers;

import java.util.concurrent.Flow;

import io.a2a.server.events.Event;
import io.a2a.spec.A2AServerException;
import io.a2a.spec.EventType;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;

public interface RequestHandler {
    Task onGetTask(TaskQueryParams params) throws A2AServerException;

    Task onCancelTask(TaskIdParams params) throws A2AServerException;

    EventType onMessageSend(MessageSendParams params) throws A2AServerException;

    Flow.Publisher<Event> onMessageSendStream(MessageSendParams params) throws A2AServerException;

    TaskPushNotificationConfig onSetTaskPushNotificationConfig(TaskPushNotificationConfig params) throws A2AServerException;

    TaskPushNotificationConfig onGetTaskPushNotificationConfig(TaskIdParams params) throws A2AServerException;

    Flow.Publisher<Event> onResubscribeToTask(TaskIdParams params) throws A2AServerException;
}
