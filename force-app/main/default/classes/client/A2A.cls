/**
 * Constants and utility methods related to the A2A protocol
 * Converted from Java io.a2a.A2A
 */
public class A2A {
    
    /**
     * Create an A2A client for the given agent URL
     * @param agentUrl the agent's base URL
     * @return the A2A client
     */
    public static A2AClient client(String agentUrl) {
        return new A2AClient(agentUrl);
    }
    
    /**
     * Create an A2A client with custom HTTP client
     * @param agentUrl the agent's base URL
     * @param httpClient the HTTP client to use
     * @return the A2A client
     */
    public static A2AClient client(String agentUrl, A2AHttpClient httpClient) {
        return new A2AClient(agentUrl, httpClient);
    }
    
    /**
     * Convert the given text to a user message
     * @param text the message text
     * @return the user message
     */
    public static A2AMessage toUserMessage(String text) {
        return toMessage(text, A2AMessage.Role.USER, null);
    }
    
    /**
     * Convert the given text to a user message with ID
     * @param text the message text
     * @param messageId the message ID to use
     * @return the user message
     */
    public static A2AMessage toUserMessage(String text, String messageId) {
        return toMessage(text, A2AMessage.Role.USER, messageId);
    }
    
    /**
     * Convert the given text to an agent message
     * @param text the message text
     * @return the agent message
     */
    public static A2AMessage toAgentMessage(String text) {
        return toMessage(text, A2AMessage.Role.AGENT, null);
    }
    
    /**
     * Convert the given text to an agent message with ID
     * @param text the message text
     * @param messageId the message ID to use
     * @return the agent message
     */
    public static A2AMessage toAgentMessage(String text, String messageId) {
        return toMessage(text, A2AMessage.Role.AGENT, messageId);
    }
    
    /**
     * Create a message from text, role, and optional ID
     */
    private static A2AMessage toMessage(String text, A2AMessage.Role role, String messageId) {
        A2AMessage message = new A2AMessage();
        message.id = messageId;
        message.role = role.name();
        message.parts = new List<A2APart>{new A2ATextPart(text)};
        message.metadata = new Map<String, Object>();
        return message;
    }
    
    /**
     * Resolve an agent card from an identifier (URL or filename)
     * @param identifier the agent identifier
     * @return the agent card
     */
    public static A2AAgentCard resolveAgentCard(String identifier) {
        return A2ACardResolver.resolveCard(identifier);
    }
}