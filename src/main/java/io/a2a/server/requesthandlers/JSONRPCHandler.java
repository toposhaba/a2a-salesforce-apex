package io.a2a.server.requesthandlers;

import static io.a2a.util.AsyncUtils.convertingProcessor;

import java.util.concurrent.Flow;

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
import io.a2a.spec.InternalError;
import io.a2a.spec.InvalidRequestError;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.PublicAgentCard;
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
        } catch (Throwable t) {
            return new SendMessageResponse(request.getId(), new InternalError(t.getMessage()));
        }
    }


    public Flow.Publisher<SendStreamingMessageResponse> onMessageSendStream(SendStreamingMessageRequest request) {
        if (!agentCard.capabilities().streaming()) {
            return ZeroPublisher.fromItems(
                    new SendStreamingMessageResponse(
                            request.getId(),
                            new InvalidRequestError("Streaming is not supported by the agent")));
        }

        try {
            Flow.Publisher<StreamingEventKind> publisher = requestHandler.onMessageSendStream(request.getParams());
            return convertingProcessor(publisher, event -> {
                try {
                    return new SendStreamingMessageResponse(request.getId(), event);
                } catch (JSONRPCError error) {
                    return new SendStreamingMessageResponse(request.getId(), error);
                } catch (Throwable t) {
                    return new SendStreamingMessageResponse(request.getId(), new InternalError(t.getMessage()));
                }
            });
        } catch (JSONRPCError e) {
            return ZeroPublisher.fromItems(new SendStreamingMessageResponse(request.getId(), e));
        } catch (Throwable throwable) {
            return ZeroPublisher.fromItems(new SendStreamingMessageResponse(request.getId(), new InternalError(throwable.getMessage())));
        }
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
        } catch (Throwable t) {
            return new CancelTaskResponse(request.getId(), new InternalError(t.getMessage()));
        }
    }

    public Flow.Publisher<SendStreamingMessageResponse> onResubscribeToTask(TaskResubscriptionRequest request) {
        if (!agentCard.capabilities().streaming()) {
            return ZeroPublisher.fromItems(
                    new SendStreamingMessageResponse(
                            request.getId(),
                            new InvalidRequestError("Streaming is not supported by the agent")));
        }

        try {
            Flow.Publisher<StreamingEventKind> publisher = requestHandler.onResubscribeToTask(request.getParams());
            return convertingProcessor(publisher, streamingEventType -> {
                try {
                    return new SendStreamingMessageResponse(request.getId(), streamingEventType);
                } catch (JSONRPCError error) {
                    return new SendStreamingMessageResponse(request.getId(), error);
                } catch (Throwable t) {
                    return new SendStreamingMessageResponse(request.getId(), new InternalError(t.getMessage()));
                }
            });
        } catch (JSONRPCError e) {
            return ZeroPublisher.fromItems(new SendStreamingMessageResponse(request.getId(), e));
        } catch (Throwable throwable) {
            return ZeroPublisher.fromItems(new SendStreamingMessageResponse(request.getId(), new InternalError(throwable.getMessage())));
        }
    }

    public GetTaskPushNotificationConfigResponse getPushNotification(GetTaskPushNotificationConfigRequest request) {
        try {
            TaskPushNotificationConfig config = requestHandler.onGetTaskPushNotificationConfig(request.getParams());
            return new GetTaskPushNotificationConfigResponse(request.getId(), config);
        } catch (JSONRPCError e) {
            return new GetTaskPushNotificationConfigResponse(request.getId().toString(), e);
        } catch (Throwable t) {
            return new GetTaskPushNotificationConfigResponse(request.getId(), new InternalError(t.getMessage()));
        }
    }

    public SetTaskPushNotificationConfigResponse setPushNotification(SetTaskPushNotificationConfigRequest request) {
        if (!agentCard.capabilities().pushNotifications()) {
            return new SetTaskPushNotificationConfigResponse(request.getId(),
                    new InvalidRequestError("Push notifications are not supported by the agent"));
        }
        try {
            TaskPushNotificationConfig config = requestHandler.onSetTaskPushNotificationConfig(request.getParams());
            return new SetTaskPushNotificationConfigResponse(request.getId().toString(), config);
        } catch (JSONRPCError e) {
            return new SetTaskPushNotificationConfigResponse(request.getId(), e);
        } catch (Throwable t) {
            return new SetTaskPushNotificationConfigResponse(request.getId(), new InternalError(t.getMessage()));
        }
    }

    public GetTaskResponse onGetTask(GetTaskRequest request) {
        try {
            Task task = requestHandler.onGetTask(request.getParams());
            return new GetTaskResponse(request.getId(), task);
        } catch (JSONRPCError e) {
            return new GetTaskResponse(request.getId(), e);
        } catch (Throwable t) {
            return new GetTaskResponse(request.getId(), new InternalError(t.getMessage()));
        }
    }

    public AgentCard getAgentCard() {
        return agentCard;
    }
}
