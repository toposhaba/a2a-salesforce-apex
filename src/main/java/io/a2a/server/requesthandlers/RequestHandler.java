package io.a2a.server.requesthandlers;

import java.util.concurrent.Flow;

import io.a2a.server.JSONRPCException;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskPushNotificationRequest;
import io.a2a.spec.GetTaskPushNotificationResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.SendStreamingMessageRequest;
import io.a2a.spec.SendStreamingMessageResponse;
import io.a2a.spec.SetTaskPushNotificationRequest;
import io.a2a.spec.SetTaskPushNotificationResponse;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskResubscriptionRequest;

public interface RequestHandler {
    GetTaskResponse onGetTask(TaskQueryParams params) throws JSONRPCException;

    CancelTaskResponse onCancelTask(TaskIdParams params) throws JSONRPCException;

     SendMessageResponse onMessageSend(MessageSendParams params) throws JSONRPCException;

    SendStreamingMessageResponse onMessageSendStream(MessageSendParams params) throws JSONRPCException;

    SetTaskPushNotificationResponse onSetTaskPushNotificationConfig(TaskPushNotificationConfig params) throws JSONRPCException;

    GetTaskPushNotificationResponse onGetTaskPushNotificationConfig(TaskIdParams params) throws JSONRPCException;

    Flow.Publisher<SendStreamingMessageResponse> onResubscribeToTask(TaskIdParams params) throws JSONRPCException;
}
