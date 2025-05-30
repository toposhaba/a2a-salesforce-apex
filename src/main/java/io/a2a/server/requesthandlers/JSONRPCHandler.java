package io.a2a.server.requesthandlers;

import static io.a2a.util.AsyncUtils.convertingProcessor;

import java.util.concurrent.Flow;

import io.a2a.spec.PublicAgentCard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.EventKind;
import io.a2a.spec.GetTaskPushNotificationConfigRequest;
import io.a2a.spec.GetTaskPushNotificationConfigResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.SendStreamingMessageRequest;
import io.a2a.spec.SendStreamingMessageResponse;
import io.a2a.spec.SetTaskPushNotificationConfigRequest;
import io.a2a.spec.SetTaskPushNotificationConfigResponse;
import io.a2a.spec.StreamingEventKind;
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskResubscriptionRequest;
import mutiny.zero.ZeroPublisher;

@ApplicationScoped
public class JSONRPCHandler {

    private AgentCard agentCard;
    private RequestHandler requestHandler;

    @Inject
    public JSONRPCHandler(@PublicAgentCard AgentCard agentCard, RequestHandler requestHandler) {
        this.agentCard = agentCard;
        this.requestHandler = requestHandler;
    }

    public SendMessageResponse onMessageSend(SendMessageRequest request) {
        try {
            EventKind taskOrMessage = requestHandler.onMessageSend(request.getParams());
            return new SendMessageResponse(request.getId(), taskOrMessage);
        } catch (JSONRPCError e) {
            return new SendMessageResponse(request.getId(), e);
        }
    }


    public Flow.Publisher<SendStreamingMessageResponse> onMessageSendStream(SendStreamingMessageRequest request) {
        Flow.Publisher<StreamingEventKind> publisher = requestHandler.onMessageSendStream(request.getParams());
        return convertingProcessor(publisher, event -> {
            try {
                return new SendStreamingMessageResponse(request.getId(), event);
            } catch (JSONRPCError error) {
                return new SendStreamingMessageResponse(request.getId(), error);
            }
        });
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
        Flow.Publisher<StreamingEventKind> publisher;
        try {
            publisher = requestHandler.onResubscribeToTask(request.getParams());
        } catch (JSONRPCError e) {
            return ZeroPublisher.fromItems(new SendStreamingMessageResponse(request.getId(), e));
        }
        return convertingProcessor(publisher, streamingEventType -> {
            try {
                return new SendStreamingMessageResponse(request.getId(), streamingEventType);
            } catch (JSONRPCError error) {
                return new SendStreamingMessageResponse(request.getId(), error);
            }
        });
    }

    public GetTaskPushNotificationConfigResponse getPushNotification(GetTaskPushNotificationConfigRequest request) {
        try {
            TaskPushNotificationConfig config = requestHandler.onGetTaskPushNotificationConfig(request.getParams());
            return new GetTaskPushNotificationConfigResponse(request.getId().toString(), config);
        } catch (JSONRPCError e) {
            return new GetTaskPushNotificationConfigResponse(request.getId().toString(), e);
        }
    }

    public SetTaskPushNotificationConfigResponse setPushNotification(SetTaskPushNotificationConfigRequest request) {
        try {
            TaskPushNotificationConfig config = requestHandler.onSetTaskPushNotificationConfig(request.getParams());
            return new SetTaskPushNotificationConfigResponse(request.getId().toString(), config);
        } catch (JSONRPCError e) {
            return new SetTaskPushNotificationConfigResponse(request.getId(), e);
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

    public AgentCard getAgentCard() {
        return agentCard;
    }
}
