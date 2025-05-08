package io.a2a.client;

import static io.a2a.spec.A2A.AGENT_CARD_REQUEST;
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
import io.a2a.spec.SendTaskRequest;
import io.a2a.spec.SendTaskResponse;
import io.a2a.spec.TaskSendParams;
import io.a2a.util.Assert;

/**
 * An A2A client.
 */
public class A2AClient {

    private static TypeReference<AgentCard> AGENT_CARD_TYPE_REFERENCE = new TypeReference<>() {};
    private static TypeReference<SendTaskResponse> SEND_TASK_RESPONSE_REFERENCE = new TypeReference<>() {};
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
     * Send a task to the A2A server.
     *
     * @param taskSendParams the parameters for the task to be sent
     * @return the response
     * @throws A2AServerException if sending the task request fails for any reason
     */
    public SendTaskResponse sendTask(TaskSendParams taskSendParams) throws A2AServerException {
        return sendTask(null, taskSendParams);
    }

    /**
     * Send a task to the A2A server.
     *
     * @param requestId the request ID to use
     * @param taskSendParams the parameters for the task to be sent
     * @return the response
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
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .uri(URI.create(getRequestEndpoint(agentUrl, SEND_TASK_REQUEST)))
                    .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(sendTaskRequest)))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new A2AServerException("Failed to send task: " + httpResponse.statusCode());
            }
            return unmarshalFrom(httpResponse.body(), SEND_TASK_RESPONSE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to send task: " + e);
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
}
