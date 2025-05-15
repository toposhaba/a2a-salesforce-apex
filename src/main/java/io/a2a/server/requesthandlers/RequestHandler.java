package io.a2a.server.requesthandlers;

import java.util.concurrent.Flow;

import io.a2a.server.JSONRPCException;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskPushNotificationRequest;
import io.a2a.spec.GetTaskPushNotificationResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.SendStreamingMessageRequest;
import io.a2a.spec.SendStreamingMessageResponse;
import io.a2a.spec.SetTaskPushNotificationRequest;
import io.a2a.spec.SetTaskPushNotificationResponse;
import io.a2a.spec.TaskResubscriptionRequest;

public interface RequestHandler {
    GetTaskResponse onGetTask(GetTaskRequest request) throws JSONRPCException;

    CancelTaskResponse onCancelTask(GetTaskRequest request) throws JSONRPCException;

     SendMessageResponse onMessageSend(SendMessageRequest request) throws JSONRPCException;

    SendStreamingMessageResponse onMessageSendStream(SendStreamingMessageRequest request) throws JSONRPCException;

    SetTaskPushNotificationResponse onSetTaskPushNotificationConfig(SetTaskPushNotificationRequest request) throws JSONRPCException;

    GetTaskPushNotificationResponse onGetTaskPushNotificationConfig(GetTaskPushNotificationRequest request) throws JSONRPCException;

    Flow.Publisher<SendStreamingMessageResponse> onResubscribeToTask(TaskResubscriptionRequest request) throws JSONRPCException;
}
