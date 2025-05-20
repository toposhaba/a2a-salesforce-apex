package io.a2a.server.requesthandlers;

import java.util.concurrent.Flow;

import io.a2a.server.JSONRPCException;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskPushNotificationResponse;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.SendStreamingMessageResponse;
import io.a2a.spec.SetTaskPushNotificationResponse;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;

public class DefaultRequestHandler implements RequestHandler {
    @Override
    public GetTaskResponse onGetTask(TaskQueryParams params) throws JSONRPCException {
        return null;
    }

    @Override
    public CancelTaskResponse onCancelTask(TaskIdParams params) throws JSONRPCException {
        return null;
    }

    @Override
    public SendMessageResponse onMessageSend(MessageSendParams params) throws JSONRPCException {
        return null;
    }

    @Override
    public SendStreamingMessageResponse onMessageSendStream(MessageSendParams params) throws JSONRPCException {
        return null;
    }

    @Override
    public SetTaskPushNotificationResponse onSetTaskPushNotificationConfig(TaskPushNotificationConfig params) throws JSONRPCException {
        return null;
    }

    @Override
    public GetTaskPushNotificationResponse onGetTaskPushNotificationConfig(TaskIdParams params) throws JSONRPCException {
        return null;
    }

    @Override
    public Flow.Publisher<SendStreamingMessageResponse> onResubscribeToTask(TaskIdParams params) throws JSONRPCException {
        return null;
    }
}
