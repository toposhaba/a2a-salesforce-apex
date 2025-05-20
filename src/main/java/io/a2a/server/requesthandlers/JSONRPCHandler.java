package io.a2a.server.requesthandlers;

import java.util.concurrent.Flow;

import io.a2a.spec.A2AServerException;
import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.EventType;
import io.a2a.spec.GetTaskPushNotificationRequest;
import io.a2a.spec.GetTaskPushNotificationResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.SendStreamingMessageRequest;
import io.a2a.spec.SendStreamingMessageResponse;
import io.a2a.spec.SetTaskPushNotificationRequest;
import io.a2a.spec.SetTaskPushNotificationResponse;
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskResubscriptionRequest;

public class JSONRPCHandler {
    private final AgentCard agentCard;
    private final RequestHandler requestHandler;

    public JSONRPCHandler(AgentCard agentCard, RequestHandler requestHandler) {
        this.agentCard = agentCard;
        this.requestHandler = requestHandler;
    }

    public SendMessageResponse onMessageSend(SendMessageRequest request) {
        try {
            EventType taskOrMessage = requestHandler.onMessageSend(request.getParams());
            return new SendMessageResponse(request.getId(), taskOrMessage);
        } catch (JSONRPCError e) {
            return new SendMessageResponse(request.getId(), e);
        }
    }


    public SendStreamingMessageResponse onMessageSendStream(SendStreamingMessageRequest request) {
        return null;
    }

    public CancelTaskResponse onCancelTask(CancelTaskRequest request) {
        try {
            Task task = requestHandler.onCancelTask(request.getParams());
            if (task != null) {
                return new CancelTaskResponse(request.getId(), task);
            }
            return new CancelTaskResponse(request.getId(), new TaskNotFoundError());
        } catch (JSONRPCError e) {
            return new CancelTaskResponse(request.getId(), e);
        }
    }

    public Flow.Publisher<SendStreamingMessageResponse> onResubscribeToTask(TaskResubscriptionRequest request) {
        return null;
    }

    public GetTaskPushNotificationResponse getPushNotification(GetTaskPushNotificationRequest request) {
        try {
            TaskPushNotificationConfig config = requestHandler.onGetTaskPushNotificationConfig(request.getParams());
            return new GetTaskPushNotificationResponse(request.getId().toString(), config);
        } catch (JSONRPCError e) {
            return new GetTaskPushNotificationResponse(request.getId().toString(), e);
        }
    }

    public SetTaskPushNotificationResponse setPushNotification(SetTaskPushNotificationRequest request) {
        try {
            TaskPushNotificationConfig config = requestHandler.onSetTaskPushNotificationConfig(request.getParams());
            return new SetTaskPushNotificationResponse(request.getId().toString(), config);
        } catch (JSONRPCError e) {
            return new SetTaskPushNotificationResponse(request.getId(), e);
        }
    }

    public GetTaskResponse onGetTask(GetTaskRequest request) {
        try {
            Task task = requestHandler.onGetTask(request.getParams());
            if (task != null) {
                return new GetTaskResponse(request.getId(), task);
            }
        } catch (JSONRPCError e) {
            return new GetTaskResponse(request.getId(), e);
        }
        return null;
    }
}
