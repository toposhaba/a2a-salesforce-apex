/**
 * A2A Client for interacting with A2A agents
 * Placeholder implementation - TODO: Complete based on Java io.a2a.client.A2AClient
 */
public class A2AClient {
    private String agentUrl;
    private A2AHttpClient httpClient;
    
    public A2AClient(String agentUrl) {
        this(agentUrl, new A2AHttpClient());
    }
    
    public A2AClient(String agentUrl, A2AHttpClient httpClient) {
        A2AAssert.checkNotNullParam('agentUrl', agentUrl);
        A2AAssert.checkNotNullParam('httpClient', httpClient);
        
        this.agentUrl = agentUrl;
        this.httpClient = httpClient;
    }
    
    /**
     * Get the agent card
     */
    public A2AAgentCard getAgentCard() {
        Http http = new Http();
        HttpRequest request = new HttpRequest();
        request.setEndpoint(agentUrl + '/agent/card');
        request.setMethod('GET');
        request.setHeader('Accept', 'application/json');
        
        HttpResponse response = http.send(request);
        
        if (response.getStatusCode() != 200) {
            throw new A2AException('Failed to get agent card: ' + response.getStatus());
        }
        
        return A2AAgentCard.deserialize(response.getBody());
    }
    
    /**
     * Send a message to the agent
     * TODO: Implement full message sending functionality
     */
    public Map<String, Object> sendMessage(A2AMessage message) {
        Http http = new Http();
        HttpRequest request = new HttpRequest();
        request.setEndpoint(agentUrl + '/tasks');
        request.setMethod('POST');
        request.setHeader('Content-Type', 'application/json');
        request.setBody(JSON.serialize(message.serialize()));
        
        HttpResponse response = http.send(request);
        
        if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
            throw new A2AException('Failed to send message: ' + response.getStatus());
        }
        
        return (Map<String, Object>) JSON.deserializeUntyped(response.getBody());
    }
}