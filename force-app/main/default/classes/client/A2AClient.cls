/**
 * An A2A client for communicating with A2A servers.
 * Converted from Java: io.a2a.client.A2AClient
 */
public class A2AClient {
    private A2AHttpClient httpClient;
    private String agentUrl;
    private A2AAgentCard agentCard;
    
    /**
     * Create a new A2AClient with an agent card
     */
    public A2AClient(A2AAgentCard agentCard) {
        A2AAssert.checkNotNullParam('agentCard', agentCard);
        this.agentCard = agentCard;
        this.agentUrl = agentCard.url;
        this.httpClient = new A2AHttpClient();
    }
    
    /**
     * Create a new A2AClient with an agent URL
     */
    public A2AClient(String agentUrl) {
        A2AAssert.checkNotNullParam('agentUrl', agentUrl);
        this.agentUrl = agentUrl;
        this.httpClient = new A2AHttpClient();
    }
    
    /**
     * Create a new A2AClient with custom HTTP client
     */
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
        if (this.agentCard == null) {
            // Fetch agent card from server
            this.agentCard = fetchAgentCard();
        }
        return this.agentCard;
    }
    
    /**
     * Send a message to the remote agent
     */
    public A2ASendMessageResponse sendMessage(A2AMessageSendParams params) {
        return sendMessage(null, params);
    }
    
    /**
     * Send a message to the remote agent with request ID
     */
    public A2ASendMessageResponse sendMessage(String requestId, A2AMessageSendParams params) {
        A2ASendMessageRequest request = new A2ASendMessageRequest();
        request.jsonrpc = A2AJSONRPCMessage.JSONRPC_VERSION;
        request.method = 'a2a.sendMessage';
        request.params = params;
        if (requestId != null) {
            request.id = requestId;
        }
        
        String jsonRequest = JSON.serialize(request, true);
        HttpResponse response = httpClient.post(agentUrl + '/rpc', jsonRequest, 'application/json');
        
        if (response.getStatusCode() != 200) {
            throw new A2AException('Failed to send message: HTTP ' + response.getStatusCode());
        }
        
        return (A2ASendMessageResponse) JSON.deserialize(response.getBody(), A2ASendMessageResponse.class);
    }
    
    /**
     * Get a task by ID
     */
    public A2AGetTaskResponse getTask(String taskId) {
        return getTask(null, new A2ATaskQueryParams(taskId));
    }
    
    /**
     * Get a task with query parameters
     */
    public A2AGetTaskResponse getTask(A2ATaskQueryParams taskQueryParams) {
        return getTask(null, taskQueryParams);
    }
    
    /**
     * Get a task with request ID and query parameters
     */
    public A2AGetTaskResponse getTask(String requestId, A2ATaskQueryParams taskQueryParams) {
        A2AGetTaskRequest request = new A2AGetTaskRequest();
        request.jsonrpc = A2AJSONRPCMessage.JSONRPC_VERSION;
        request.method = 'a2a.getTask';
        request.params = taskQueryParams;
        if (requestId != null) {
            request.id = requestId;
        }
        
        String jsonRequest = JSON.serialize(request, true);
        HttpResponse response = httpClient.post(agentUrl + '/rpc', jsonRequest, 'application/json');
        
        if (response.getStatusCode() != 200) {
            throw new A2AException('Failed to get task: HTTP ' + response.getStatusCode());
        }
        
        return (A2AGetTaskResponse) JSON.deserialize(response.getBody(), A2AGetTaskResponse.class);
    }
    
    /**
     * Cancel a task by ID
     */
    public A2ACancelTaskResponse cancelTask(String taskId) {
        return cancelTask(null, new A2ATaskIdParams(taskId));
    }
    
    /**
     * Cancel a task with parameters
     */
    public A2ACancelTaskResponse cancelTask(A2ATaskIdParams taskIdParams) {
        return cancelTask(null, taskIdParams);
    }
    
    /**
     * Cancel a task with request ID and parameters
     */
    public A2ACancelTaskResponse cancelTask(String requestId, A2ATaskIdParams taskIdParams) {
        A2ACancelTaskRequest request = new A2ACancelTaskRequest();
        request.jsonrpc = A2AJSONRPCMessage.JSONRPC_VERSION;
        request.method = 'a2a.cancelTask';
        request.params = taskIdParams;
        if (requestId != null) {
            request.id = requestId;
        }
        
        String jsonRequest = JSON.serialize(request, true);
        HttpResponse response = httpClient.post(agentUrl + '/rpc', jsonRequest, 'application/json');
        
        if (response.getStatusCode() != 200) {
            throw new A2AException('Failed to cancel task: HTTP ' + response.getStatusCode());
        }
        
        return (A2ACancelTaskResponse) JSON.deserialize(response.getBody(), A2ACancelTaskResponse.class);
    }
    
    /**
     * Send a streaming message (not fully supported in Apex)
     */
    public void sendStreamingMessage(String requestId, A2AMessageSendParams params) {
        // Note: Server-Sent Events (SSE) are not natively supported in Apex
        // This would require Platform Events or CometD for real-time streaming
        throw new A2AException('Streaming messages are not yet supported in Apex. Use Platform Events instead.');
    }
    
    /**
     * Fetch the agent card from the server
     */
    private A2AAgentCard fetchAgentCard() {
        HttpResponse response = httpClient.get(agentUrl + '/.well-known/a2a-agentcard.json');
        
        if (response.getStatusCode() != 200) {
            throw new A2AException('Failed to fetch agent card: HTTP ' + response.getStatusCode());
        }
        
        return A2AAgentCard.deserialize(response.getBody());
    }
    
    /**
     * Helper class for JSON-RPC requests
     */
    public virtual class A2AJSONRPCRequest {
        public String jsonrpc;
        public String method;
        public String id;
        public Object params;
    }
    
    /**
     * Helper class for JSON-RPC messages
     */
    public class A2AJSONRPCMessage {
        public static final String JSONRPC_VERSION = '2.0';
    }
    
    /**
     * Send message request
     */
    public class A2ASendMessageRequest extends A2AJSONRPCRequest {
        public A2AMessageSendParams params;
    }
    
    /**
     * Send message response
     */
    public class A2ASendMessageResponse {
        public String id;
        public Object result;
        public A2AJSONRPCError error;
    }
    
    /**
     * Get task request
     */
    public class A2AGetTaskRequest extends A2AJSONRPCRequest {
        public A2ATaskQueryParams params;
    }
    
    /**
     * Get task response
     */
    public class A2AGetTaskResponse {
        public String id;
        public A2ATask result;
        public A2AJSONRPCError error;
    }
    
    /**
     * Cancel task request
     */
    public class A2ACancelTaskRequest extends A2AJSONRPCRequest {
        public A2ATaskIdParams params;
    }
    
    /**
     * Cancel task response
     */
    public class A2ACancelTaskResponse {
        public String id;
        public Boolean result;
        public A2AJSONRPCError error;
    }
    
    /**
     * Message send parameters
     */
    public class A2AMessageSendParams {
        public A2AMessage message;
        
        public A2AMessageSendParams() {}
        
        public A2AMessageSendParams(A2AMessage message) {
            this.message = message;
        }
    }
    
    /**
     * Task query parameters
     */
    public class A2ATaskQueryParams {
        public String id;
        
        public A2ATaskQueryParams() {}
        
        public A2ATaskQueryParams(String id) {
            this.id = id;
        }
    }
    
    /**
     * Task ID parameters
     */
    public class A2ATaskIdParams {
        public String id;
        
        public A2ATaskIdParams() {}
        
        public A2ATaskIdParams(String id) {
            this.id = id;
        }
    }
    
    /**
     * JSON-RPC error
     */
    public class A2AJSONRPCError {
        public Integer code;
        public String message;
        public Object data;
    }
    
    /**
     * Task representation
     */
    public class A2ATask {
        public String id;
        public String status;
        public Map<String, Object> artifacts;
        public Map<String, Object> metadata;
    }
}