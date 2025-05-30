package io.a2a.server.tasks;

import java.io.StringWriter;
import java.net.http.HttpClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.a2a.http.A2AHttpClient;
import io.a2a.spec.PushNotificationConfig;
import io.a2a.spec.Task;
import io.a2a.util.Utils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InMemoryPushNotifier implements PushNotifier {
    private final A2AHttpClient httpClient;
    private final Map<String, PushNotificationConfig> pushNotificationInfos = Collections.synchronizedMap(new HashMap<>());

    public InMemoryPushNotifier(A2AHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void setInfo(String taskId, PushNotificationConfig notificationConfig) {
        pushNotificationInfos.put(taskId, notificationConfig);
    }

    @Override
    public PushNotificationConfig getInfo(String taskId) {
        return pushNotificationInfos.get(taskId);
    }

    @Override
    public void deleteInfo(String taskId) {
        pushNotificationInfos.remove(taskId);
    }

    @Override
    public void sendNotification(Task task) {
        PushNotificationConfig pushInfo = pushNotificationInfos.get(task.getId());
        if (pushInfo == null) {
            return;
        }
        String url = pushInfo.url();
        // TODO https://github.com/fjuma/a2a-java-sdk/issues/59 will have the real client
        httpClient.post(url, task);

    }
}
