package io.a2a.client;

import static io.a2a.spec.A2A.CANCEL_TASK_REQUEST;
import static io.a2a.spec.A2A.GET_TASK_PUSH_NOTIFICATION_REQUEST;
import static io.a2a.spec.A2A.GET_TASK_REQUEST;
import static io.a2a.spec.A2A.JSONRPC_VERSION;
import static io.a2a.spec.A2A.SEND_MESSAGE_REQUEST;
import static io.a2a.spec.A2A.SET_TASK_PUSH_NOTIFICATION_REQUEST;
import static io.a2a.spec.A2A.getRequestEndpoint;
import static io.a2a.util.Utils.OBJECT_MAPPER;
import static io.a2a.util.Utils.unmarshalFrom;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.core.type.TypeReference;

import io.a2a.spec.A2A;
import io.a2a.spec.A2AServerException;
import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskPushNotificationRequest;
import io.a2a.spec.GetTaskPushNotificationResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.PushNotificationConfig;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.SetTaskPushNotificationRequest;
import io.a2a.spec.SetTaskPushNotificationResponse;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;
import io.a2a.util.Assert;

/**
 * An A2A client.
 */
public class A2AClient {

    private static TypeReference<SendMessageResponse> SEND_MESSAGE_RESPONSE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<GetTaskResponse> GET_TASK_RESPONSE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<CancelTaskResponse> CANCEL_TASK_RESPONSE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<GetTaskPushNotificationResponse> GET_TASK_PUSH_NOTIFICATION_RESPONSE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<SetTaskPushNotificationResponse> SET_TASK_PUSH_NOTIFICATION_RESPONSE_REFERENCE = new TypeReference<>() {};
    private final HttpClient httpClient;
    private final String agentUrl;
    private AgentCard agentCard;


    /**
     * Create a new A2AClient.
     *
     * @param agentCard the agent card for the A2A server this client will be communicating with
     */
    public A2AClient(AgentCard agentCard) {
        Assert.checkNotNullParam("agentCard", agentCard);
        this.agentCard = agentCard;
        this.agentUrl = agentCard.url();
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Create a new A2AClient.
     *
     * @param agentUrl the URL for the A2A server this client will be communicating with
     */
    public A2AClient(String agentUrl) {
        Assert.checkNotNullParam("agentUrl", agentUrl);
        this.agentUrl = agentUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Get the agent card for the A2A server this client will be communicating with.
     *
     * @return the agent card for the A2A server
     * @throws {@code A2AServerException} if the agent card for the A2A server cannot be obtained
     */
    public AgentCard getAgentCard() throws A2AServerException {
        if (this.agentCard == null) {
            this.agentCard = A2A.getAgentCard(this.httpClient, this.agentUrl);
        }
        return this.agentCard;
    }

    /**
     * Send a message to the remote agent.
     *
     * @param messageSendParams the parameters for the message to be sent
     * @return the response, may contain a message or a task
     * @throws A2AServerException if sending the message fails for any reason
     */
    public SendMessageResponse sendMessage(MessageSendParams messageSendParams) throws A2AServerException {
        return sendMessage(null, messageSendParams);
    }

    /**
     * Send a message to the remote agent.
     *
     * @param requestId the request ID to use
     * @param messageSendParams the parameters for the message to be sent
     * @return the response, may contain a message or a task
     * @throws A2AServerException if sending the message fails for any reason
     */
    public SendMessageResponse sendMessage(String requestId, MessageSendParams messageSendParams) throws A2AServerException {
        SendMessageRequest.Builder sendMessageRequestBuilder = new SendMessageRequest.Builder()
                .jsonrpc(JSONRPC_VERSION)
                .method(SEND_MESSAGE_REQUEST)
                .params(messageSendParams);

        if (requestId != null) {
            sendMessageRequestBuilder.id(requestId);
        }

        SendMessageRequest sendMessageRequest = sendMessageRequestBuilder.build();

        try {
            HttpResponse<String> httpResponse = sendPostRequest(SEND_MESSAGE_REQUEST, sendMessageRequest);
            if (httpResponse.statusCode() != 200) {
                throw new A2AServerException("Failed to send message: " + httpResponse.statusCode());
            }
            return unmarshalFrom(httpResponse.body(), SEND_MESSAGE_RESPONSE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to send message: " + e);
        }
    }

    /**
     * Retrieve a task from the A2A server. This method can be used to retrieve the generated
     * artifacts for a task.
     *
     * @param id the task ID
     * @return the response containing the task
     * @throws A2AServerException if retrieving the task fails for any reason
     */
    public GetTaskResponse getTask(String id) throws A2AServerException {
        return getTask(null, new TaskQueryParams(id));
    }

    /**
     * Retrieve a task from the A2A server. This method can be used to retrieve the generated
     * artifacts for a task.
     *
     * @param taskQueryParams the params for the task to be queried
     * @return the response containing the task
     * @throws A2AServerException if retrieving the task fails for any reason
     */
    public GetTaskResponse getTask(TaskQueryParams taskQueryParams) throws A2AServerException {
        return getTask(null, taskQueryParams);
    }

    /**
     * Retrieve the generated artifacts for a task.
     *
     * @param requestId the request ID to use
     * @param taskQueryParams the params for the task to be queried
     * @return the response containing the task
     * @throws A2AServerException if retrieving the task fails for any reason
     */
    public GetTaskResponse getTask(String requestId, TaskQueryParams taskQueryParams) throws A2AServerException {
        GetTaskRequest.Builder getTaskRequestBuilder = new GetTaskRequest.Builder()
                .jsonrpc(JSONRPC_VERSION)
                .method(GET_TASK_REQUEST)
                .params(taskQueryParams);

        if (requestId != null) {
            getTaskRequestBuilder.id(requestId);
        }

        GetTaskRequest getTaskRequest = getTaskRequestBuilder.build();

        try {
            HttpResponse<String> httpResponse = sendPostRequest(GET_TASK_REQUEST, getTaskRequest);
            if (httpResponse.statusCode() != 200) {
                throw new A2AServerException("Failed to get task: " + httpResponse.statusCode());
            }
            return unmarshalFrom(httpResponse.body(), GET_TASK_RESPONSE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to get task: " + e);
        }
    }

    /**
     * Cancel a task that was previously submitted to the A2A server.
     *
     * @param id the task ID
     * @return the response indicating if the task was cancelled
     * @throws A2AServerException if cancelling the task fails for any reason
     */
    public CancelTaskResponse cancelTask(String id) throws A2AServerException {
        return cancelTask(null, new TaskIdParams(id));
    }

    /**
     * Cancel a task that was previously submitted to the A2A server.
     *
     * @param taskIdParams the params for the task to be cancelled
     * @return the response indicating if the task was cancelled
     * @throws A2AServerException if cancelling the task fails for any reason
     */
    public CancelTaskResponse cancelTask(TaskIdParams taskIdParams) throws A2AServerException {
        return cancelTask(null, taskIdParams);
    }

    /**
     * Cancel a task that was previously submitted to the A2A server.
     *
     * @param requestId the request ID to use
     * @param taskIdParams the params for the task to be cancelled
     * @return the response indicating if the task was cancelled
     * @throws A2AServerException if retrieving the task fails for any reason
     */
    public CancelTaskResponse cancelTask(String requestId, TaskIdParams taskIdParams) throws A2AServerException {
        CancelTaskRequest.Builder cancelTaskRequestBuilder = new CancelTaskRequest.Builder()
                .jsonrpc(JSONRPC_VERSION)
                .method(CANCEL_TASK_REQUEST)
                .params(taskIdParams);

        if (requestId != null) {
            cancelTaskRequestBuilder.id(requestId);
        }

        CancelTaskRequest cancelTaskRequest = cancelTaskRequestBuilder.build();

        try {
            HttpResponse<String> httpResponse = sendPostRequest(CANCEL_TASK_REQUEST, cancelTaskRequest);
            if (httpResponse.statusCode() != 200) {
                throw new A2AServerException("Failed to cancel task: " + httpResponse.statusCode());
            }
            return unmarshalFrom(httpResponse.body(), CANCEL_TASK_RESPONSE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to cancel task: " + e);
        }
    }

    /**
     * Get the push notification configuration for a task.
     *
     * @param id the task ID
     * @return the response containing the push notification configuration
     * @throws A2AServerException if getting the push notification configuration fails for any reason
     */
    public GetTaskPushNotificationResponse getTaskPushNotificationConfig(String id) throws A2AServerException {
        return getTaskPushNotificationConfig(null, new TaskIdParams(id));
    }

    /**
     * Get the push notification configuration for a task.
     *
     * @param taskIdParams the params for the task
     * @return the response containing the push notification configuration
     * @throws A2AServerException if getting the push notification configuration fails for any reason
     */
    public GetTaskPushNotificationResponse getTaskPushNotificationConfig(TaskIdParams taskIdParams) throws A2AServerException {
        return getTaskPushNotificationConfig(null, taskIdParams);
    }

    /**
     * Get the push notification configuration for a task.
     *
     * @param requestId the request ID to use
     * @param taskIdParams the params for the task
     * @return the response containing the push notification configuration
     * @throws A2AServerException if getting the push notification configuration fails for any reason
     */
    public GetTaskPushNotificationResponse getTaskPushNotificationConfig(String requestId, TaskIdParams taskIdParams) throws A2AServerException {
        GetTaskPushNotificationRequest.Builder getTaskPushNotificationRequestBuilder = new GetTaskPushNotificationRequest.Builder()
                .jsonrpc(JSONRPC_VERSION)
                .method(GET_TASK_PUSH_NOTIFICATION_REQUEST)
                .params(taskIdParams);

        if (requestId != null) {
            getTaskPushNotificationRequestBuilder.id(requestId);
        }

        GetTaskPushNotificationRequest getTaskPushNotificationRequest = getTaskPushNotificationRequestBuilder.build();

        try {
            HttpResponse<String> httpResponse = sendPostRequest(GET_TASK_PUSH_NOTIFICATION_REQUEST, getTaskPushNotificationRequest);
            if (httpResponse.statusCode() != 200) {
                throw new A2AServerException("Failed to get task push notification config: " + httpResponse.statusCode());
            }
            return unmarshalFrom(httpResponse.body(), GET_TASK_PUSH_NOTIFICATION_RESPONSE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to get task push notification config: " + e);
        }
    }

    /**
     * Set push notification configuration for a task.
     *
     * @param taskId the task ID
     * @param pushNotificationConfig the push notification configuration
     * @return the response indicating whether setting the task push notification configuration succeeded
     * @throws A2AServerException if setting the push notification configuration fails for any reason
     */
    public SetTaskPushNotificationResponse setTaskPushNotificationConfig(String taskId,
                                                                         PushNotificationConfig pushNotificationConfig) throws A2AServerException {
        return setTaskPushNotificationConfig(null, taskId, pushNotificationConfig);
    }

    /**
     * Set push notification configuration for a task.
     *
     * @param requestId the request ID to use
     * @param taskId the task ID
     * @param pushNotificationConfig the push notification configuration
     * @return the response indicating whether setting the task push notification configuration succeeded
     * @throws A2AServerException if setting the push notification configuration fails for any reason
     */
    public SetTaskPushNotificationResponse setTaskPushNotificationConfig(String requestId, String taskId,
                                                                         PushNotificationConfig pushNotificationConfig) throws A2AServerException {
        SetTaskPushNotificationRequest.Builder setTaskPushNotificationRequestBuilder = new SetTaskPushNotificationRequest.Builder()
                .jsonrpc(JSONRPC_VERSION)
                .method(SET_TASK_PUSH_NOTIFICATION_REQUEST)
                .params(new TaskPushNotificationConfig(taskId, pushNotificationConfig));

        if (requestId != null) {
            setTaskPushNotificationRequestBuilder.id(requestId);
        }

        SetTaskPushNotificationRequest setTaskPushNotificationRequest = setTaskPushNotificationRequestBuilder.build();

        try {
            HttpResponse<String> httpResponse = sendPostRequest(SET_TASK_PUSH_NOTIFICATION_REQUEST, setTaskPushNotificationRequest);
            if (httpResponse.statusCode() != 200) {
                throw new A2AServerException("Failed to set task push notification config: " + httpResponse.statusCode());
            }
            return unmarshalFrom(httpResponse.body(), SET_TASK_PUSH_NOTIFICATION_RESPONSE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to set task push notification config: " + e);
        }
    }

    private HttpResponse<String> sendPostRequest(String request, Object value) throws IOException, InterruptedException{
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(getRequestEndpoint(agentUrl, request)))
                .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(value)))
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}
