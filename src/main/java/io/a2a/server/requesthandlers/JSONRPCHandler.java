package io.a2a.server.requesthandlers;

import java.util.concurrent.Flow;

import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
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

public class JSONRPCHandler {
    private final AgentCard agentCard;
    private final RequestHandler requestHandler;

    public JSONRPCHandler(AgentCard agentCard, RequestHandler requestHandler) {
        this.agentCard = agentCard;
        this.requestHandler = requestHandler;
    }

    public SendMessageResponse onMessageSend(SendMessageRequest request) {
        return null;
    }

    public SendStreamingMessageResponse onMessageSendStream(SendStreamingMessageRequest request) {
        return null;
    }

    public CancelTaskResponse onCancelTask(CancelTaskRequest request) {
        return null;
    }

    public Flow.Publisher<SendStreamingMessageResponse> onResubscribeToTask(TaskResubscriptionRequest request) {
        return null;
    }

    public GetTaskPushNotificationResponse getPushNotification(GetTaskPushNotificationRequest request) {
        return null;
    }

    public SetTaskPushNotificationResponse setPushNotification(SetTaskPushNotificationRequest request) {
        return null;
    }

    public GetTaskResponse onGetTask(GetTaskRequest request) {
        return null;
    }
}
