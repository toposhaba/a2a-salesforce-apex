package io.a2a.server.requesthandlers;

import java.util.concurrent.Flow;

import io.a2a.server.tasks.TaskStore;
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
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskResubscriptionRequest;

public class DefaultRequestHandler implements RequestHandler {

    private final TaskStore taskStore;

    public DefaultRequestHandler(TaskStore taskStore) {
        this.taskStore = taskStore;
    }

    @Override
    public GetTaskResponse onGetTask(GetTaskRequest request) {
        TaskQueryParams params = request.getParams();

        Task task = taskStore.get(params.id());
        if (task == null) {
            return new GetTaskResponse(params.id(), new TaskNotFoundError());
        }
        return null;
    }

    @Override
    public CancelTaskResponse onCancelTask(GetTaskRequest request) {
        return null;
    }

    @Override
    public SendMessageResponse onMessageSend(SendMessageRequest request) {
        return null;
    }

    @Override
    public SendStreamingMessageResponse onMessageSendStream(SendStreamingMessageRequest request) {
        return null;
    }

    @Override
    public SetTaskPushNotificationResponse onSetTaskPushNotificationConfig(SetTaskPushNotificationRequest request) {
        return null;
    }

    @Override
    public GetTaskPushNotificationResponse onGetTaskPushNotificationConfig(GetTaskPushNotificationRequest request) {
        return null;
    }

    @Override
    public Flow.Publisher<SendStreamingMessageResponse> onResubscribeToTask(TaskResubscriptionRequest request) {
        return null;
    }

    private void sendToAgent() {

    }
}
