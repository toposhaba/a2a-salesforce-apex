package io.a2a.spec;

import static io.a2a.util.Utils.unmarshalFrom;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Constants and utility methods related to the A2A protocol.
 */
public class A2A {

    public static final String CANCEL_TASK_REQUEST = "tasks/cancel";
    public static final String GET_TASK_PUSH_NOTIFICATION_REQUEST = "tasks/pushNotification/get";
    public static final String GET_TASK_REQUEST = "tasks/get";
    public static final String SET_TASK_PUSH_NOTIFICATION_REQUEST = "tasks/pushNotification/set";
    public static final String SEND_TASK_REQUEST = "tasks/send";
    public static final String SEND_TASK_RESUBSCRIPTION_REQUEST = "tasks/resubscribe";
    public static final String SEND_TASK_STREAMING_REQUEST = "tasks/sendSubscribe";

    public static final String JSONRPC_VERSION = "2.0";

    public static final String AGENT_CARD_REQUEST = ".well-known/agent.json";

    private static TypeReference<AgentCard> AGENT_CARD_TYPE_REFERENCE = new TypeReference<>() {};

    public static final String getRequestEndpoint(String agentUrl, String request) {
        return agentUrl.endsWith("/") ? agentUrl + request : agentUrl + "/" + request;
    }

    /**
     * Convert the given text to a user message.
     *
     * @param text the message text
     * @return the user message
     */
    public static Message toUserMessage(String text) {
        return toMessage(text, Message.Role.USER, null);
    }

    /**
     * Convert the given text to a user message.
     *
     * @param text the message text
     * @param messageId the message ID to use
     * @return the user message
     */
    public static Message toUserMessage(String text, String messageId) {
        return toMessage(text, Message.Role.USER, messageId);
    }

    /**
     * Convert the given text to an agent message.
     *
     * @param text the message text
     * @return the agent message
     */
    public static Message toAgentMessage(String text) {
        return toMessage(text, Message.Role.AGENT, null);
    }

    /**
     * Convert the given text to an agent message.
     *
     * @param text the message text
     * @param messageId the message ID to use
     * @return the agent message
     */
    public static Message toAgentMessage(String text, String messageId) {
        return toMessage(text, Message.Role.AGENT, messageId);
    }


    private static Message toMessage(String text, Message.Role role, String messageId) {
        Message.Builder messageBuilder = new Message.Builder()
                .role(role)
                .parts(Collections.singletonList(new TextPart(text)));
        if (messageId != null) {
            messageBuilder.messageId(messageId);
        }
        return messageBuilder.build();
    }

    /**
     * Get the agent card for an A2A agent.
     *
     * @param agentUrl the base URL for the agent whose agent card we want to retrieve
     * @return the agent card
     * @throws A2AServerException if the agent card cannot be retrieved for any reason
     */
    public static AgentCard getAgentCard(String agentUrl) throws A2AServerException {
        return getAgentCard(HttpClient.newHttpClient(), agentUrl);
    }

    /**
     * Get the agent card for an A2A agent.
     *
     * @param httpClient the http client to use
     * @param agentUrl the base URL for the agent whose agent card we want to retrieve
     * @return the agent card
     * @throws A2AServerException if the agent card cannot be retrieved for any reason
     */
    public static AgentCard getAgentCard(HttpClient httpClient, String agentUrl) throws A2AServerException {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(URI.create(getRequestEndpoint(agentUrl, AGENT_CARD_REQUEST)))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new A2AServerException("Failed to obtain agent card: " + response.statusCode());
            }
            return unmarshalFrom(response.body(), AGENT_CARD_TYPE_REFERENCE);
        } catch (IOException | InterruptedException e) {
            throw new A2AServerException("Failed to obtain agent card", e);
        }
    }

}
