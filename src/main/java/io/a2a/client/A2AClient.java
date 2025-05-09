package io.a2a.client;

import static io.a2a.spec.A2A.AGENT_CARD_REQUEST;
import static io.a2a.spec.A2A.CANCEL_TASK_REQUEST;
import static io.a2a.spec.A2A.GET_TASK_PUSH_NOTIFICATION_REQUEST;
import static io.a2a.spec.A2A.GET_TASK_REQUEST;
import static io.a2a.spec.A2A.JSONRPC_VERSION;
import static io.a2a.spec.A2A.SEND_TASK_REQUEST;;
import static io.a2a.spec.A2A.getRequestEndpoint;
import static io.a2a.util.Utils.OBJECT_MAPPER;
import static io.a2a.util.Utils.unmarshalFrom;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.core.type.TypeReference;

import io.a2a.spec.A2AServerException;
import io.a2a.spec.AgentCard;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskPushNotificationRequest;
import io.a2a.spec.GetTaskPushNotificationResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.SendTaskRequest;
import io.a2a.spec.SendTaskResponse;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskSendParams;
import io.a2a.util.Assert;

/**
 * An A2A client.
 */
public class A2AClient {

    private static TypeReference<AgentCard> AGENT_CARD_TYPE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<SendTaskResponse> SEND_TASK_RESPONSE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<GetTaskResponse> GET_TASK_RESPONSE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<CancelTaskResponse> CANCEL_TASK_RESPONSE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<GetTaskPushNotificationResponse> GET_TASK_PUSH_NOTIFICATION_RESPONSE_REFERENCE = new TypeReference<>() {};
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
            this.agentCard = getAgentCardFromUrl(this.agentUrl);
        }
        return this.agentCard;
    }

    /**
     * Send a task to the A2A server. This method can be used to start a new task, resume an
     * interrupted task, or re-open a completed task.
     *
     * @param taskSendParams the parameters for the task to be sent
     * @return the response containing the task
     * @throws A2AServerException if sending the task request fails for any reason
     */
    public SendTaskResponse sendTask(TaskSendParams taskSendParams) throws A2AServerException {
        return sendTask(null, taskSendParams);
    }

    /**
     * Send a task to the A2A server. This method can be used to start a new task, resume an
     * interrupted task, or re-open a completed task.
     *
     * @param requestId the request ID to use
     * @param taskSendParams the parameters for the task to be sent
     * @return the response containing the task
     * @throws A2AServerException if sending the task request fails for any reason
     */
    public SendTaskResponse sendTask(String requestId, TaskSendParams taskSendParams) throws A2AServerException {
        SendTaskRequest.Builder sendTaskRequestBuilder = new SendTaskRequest.Builder()
                .jsonrpc(JSONRPC_VERSION)
                .method(SEND_TASK_REQUEST)
                .params(taskSendParams);

        if (requestId != null) {
            sendTaskRequestBuilder.id(requestId);
        }

        SendTaskRequest sendTaskRequest = sendTaskRequestBuilder.build();

        try {
            HttpResponse<String> httpResponse = sendPostRequest(SEND_TASK_REQUEST, sendTaskRequest);
            if (httpResponse.statusCode() != 200) {
                throw new A2AServerException("Failed to send task: " + httpResponse.statusCode());
            }
            return unmarshalFrom(httpResponse.body(), SEND_TASK_RESPONSE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to send task: " + e);
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

    private AgentCard getAgentCardFromUrl(String url) throws A2AServerException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(getRequestEndpoint(agentUrl, AGENT_CARD_REQUEST)))
                .GET()
                .build();

        try {
            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new A2AServerException("Failed to obtain agent card: " + response.statusCode());
            }
            return unmarshalFrom(response.body(), AGENT_CARD_TYPE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to obtain agent card", e);
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
