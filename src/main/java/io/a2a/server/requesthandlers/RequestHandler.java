package io.a2a.server.requesthandlers;

import java.util.concurrent.Flow;

import io.a2a.server.events.Event;
import io.a2a.spec.EventType;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.StreamingEventType;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;

public interface RequestHandler {
    Task onGetTask(TaskQueryParams params) throws JSONRPCError;

    Task onCancelTask(TaskIdParams params) throws JSONRPCError;

    EventType onMessageSend(MessageSendParams params) throws JSONRPCError;

    Flow.Publisher<StreamingEventType> onMessageSendStream(MessageSendParams params) throws JSONRPCError;

    TaskPushNotificationConfig onSetTaskPushNotificationConfig(TaskPushNotificationConfig params) throws JSONRPCError;

    TaskPushNotificationConfig onGetTaskPushNotificationConfig(TaskIdParams params) throws JSONRPCError;

    Flow.Publisher<Event> onResubscribeToTask(TaskIdParams params) throws JSONRPCError;
}
