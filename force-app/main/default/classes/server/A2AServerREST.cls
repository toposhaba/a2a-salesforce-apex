/**
 * REST API endpoint for A2A server operations.
 * Exposes A2A functionality via Salesforce REST API.
 */
@RestResource(urlMapping='/a2a/v1/*')
global class A2AServerREST {
    
    /**
     * Handle POST requests for JSON-RPC
     */
    @HttpPost
    global static void handlePost() {
        RestRequest req = RestContext.request;
        RestResponse res = RestContext.response;
        
        try {
            // Get request body
            String requestBody = req.requestBody.toString();
            
            // Create server context
            A2AServerCallContext context = A2AServerCallContext.fromRestContext();
            
            // Get or create agent card
            A2AAgentCard agentCard = getAgentCard();
            
            // Get request handler (this should be injected/configured)
            A2ARequestHandler requestHandler = getRequestHandler();
            
            // Create JSON-RPC handler
            A2AJSONRPCHandler rpcHandler = new A2AJSONRPCHandler(agentCard, requestHandler);
            
            // Process the request
            String responseBody = rpcHandler.processRequest(requestBody, context);
            
            // Set response
            res.statusCode = 200;
            res.responseBody = Blob.valueOf(responseBody);
            res.addHeader('Content-Type', 'application/json');
            
        } catch (Exception e) {
            handleError(res, e);
        }
    }
    
    /**
     * Handle GET requests for agent card
     */
    @HttpGet
    global static void handleGet() {
        RestRequest req = RestContext.request;
        RestResponse res = RestContext.response;
        
        try {
            String path = req.requestURI.substringAfter('/a2a/v1');
            
            if (path == '/.well-known/agent.json' || path == '/agent/card') {
                // Return agent card
                A2AAgentCard agentCard = getAgentCard();
                res.statusCode = 200;
                res.responseBody = Blob.valueOf(JSON.serialize(agentCard.serialize()));
                res.addHeader('Content-Type', 'application/json');
            } else {
                // Not found
                res.statusCode = 404;
                Map<String, Object> error = new Map<String, Object>{
                    'error' => 'Not Found',
                    'message' => 'The requested resource was not found'
                };
                res.responseBody = Blob.valueOf(JSON.serialize(error));
                res.addHeader('Content-Type', 'application/json');
            }
            
        } catch (Exception e) {
            handleError(res, e);
        }
    }
    
    /**
     * Get the agent card (should be configured/customized)
     */
    private static A2AAgentCard getAgentCard() {
        // This should be loaded from configuration or custom metadata
        A2AAgentCard card = new A2AAgentCard();
        card.name = 'Salesforce A2A Agent';
        card.description = 'A2A Agent running on Salesforce Platform';
        card.url = URL.getOrgDomainUrl().toExternalForm() + '/services/apexrest/a2a/v1';
        
        // Set capabilities
        A2AAgentCapabilities capabilities = new A2AAgentCapabilities();
        capabilities.streaming = false; // Apex doesn't support SSE natively
        capabilities.pushNotifications = true; // Can use Platform Events
        card.capabilities = capabilities;
        
        return card;
    }
    
    /**
     * Get the request handler (should be injected/configured)
     */
    private static A2ARequestHandler getRequestHandler() {
        // This should use dependency injection or configuration
        // For now, return a simple implementation
        return new A2ASimpleRequestHandler();
    }
    
    /**
     * Handle errors
     */
    private static void handleError(RestResponse res, Exception e) {
        res.statusCode = 500;
        Map<String, Object> error = new Map<String, Object>{
            'error' => 'Internal Server Error',
            'message' => e.getMessage(),
            'type' => e.getTypeName()
        };
        res.responseBody = Blob.valueOf(JSON.serialize(error));
        res.addHeader('Content-Type', 'application/json');
        
        // Log the error
        System.debug(LoggingLevel.ERROR, 'A2A Server Error: ' + e.getMessage());
        System.debug(LoggingLevel.ERROR, e.getStackTraceString());
    }
}