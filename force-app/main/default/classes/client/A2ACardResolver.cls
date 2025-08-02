/**
 * Resolves A2A agent cards from remote servers.
 * Converted from Java: io.a2a.client.A2ACardResolver
 */
public class A2ACardResolver {
    private A2AHttpClient httpClient;
    private String url;
    private Map<String, String> authHeaders;
    
    public static final String DEFAULT_AGENT_CARD_PATH = '/.well-known/agent.json';
    
    /**
     * Constructor with HTTP client and base URL
     */
    public A2ACardResolver(A2AHttpClient httpClient, String baseUrl) {
        this(httpClient, baseUrl, null, null);
    }
    
    /**
     * Constructor with HTTP client, base URL and agent card path
     */
    public A2ACardResolver(A2AHttpClient httpClient, String baseUrl, String agentCardPath) {
        this(httpClient, baseUrl, agentCardPath, null);
    }
    
    /**
     * Constructor with all parameters
     */
    public A2ACardResolver(A2AHttpClient httpClient, String baseUrl, String agentCardPath, Map<String, String> authHeaders) {
        A2AAssert.checkNotNullParam('httpClient', httpClient);
        A2AAssert.checkNotNullParam('baseUrl', baseUrl);
        
        this.httpClient = httpClient;
        this.authHeaders = authHeaders;
        
        // Build the full URL
        String cardPath = String.isBlank(agentCardPath) ? DEFAULT_AGENT_CARD_PATH : agentCardPath;
        this.url = buildUrl(baseUrl, cardPath);
    }
    
    /**
     * Get the agent card for the configured A2A agent
     */
    public A2AAgentCard getAgentCard() {
        HttpRequest request = new HttpRequest();
        request.setEndpoint(this.url);
        request.setMethod('GET');
        request.setHeader('Content-Type', 'application/json');
        request.setHeader('Accept', 'application/json');
        
        // Add authentication headers if provided
        if (this.authHeaders != null) {
            for (String key : this.authHeaders.keySet()) {
                request.setHeader(key, this.authHeaders.get(key));
            }
        }
        
        Http http = new Http();
        HttpResponse response = http.send(request);
        
        if (response.getStatusCode() != 200) {
            throw new A2AException('Failed to obtain agent card: HTTP ' + response.getStatusCode() + ' - ' + response.getStatus());
        }
        
        String body = response.getBody();
        if (String.isBlank(body)) {
            throw new A2AException('Empty response body when fetching agent card');
        }
        
        try {
            return A2AAgentCard.deserialize(body);
        } catch (Exception e) {
            throw new A2AException('Could not unmarshal agent card response: ' + e.getMessage(), e);
        }
    }
    
    /**
     * Resolve an agent card from a URL
     */
    public static A2AAgentCard resolveFromUrl(String baseUrl) {
        return resolveFromUrl(baseUrl, null);
    }
    
    /**
     * Resolve an agent card from a URL with custom path
     */
    public static A2AAgentCard resolveFromUrl(String baseUrl, String agentCardPath) {
        A2ACardResolver resolver = new A2ACardResolver(new A2AHttpClient(), baseUrl, agentCardPath);
        return resolver.getAgentCard();
    }
    
    /**
     * Resolve an agent card from a file (not directly supported in Apex)
     */
    public static A2AAgentCard resolveFromFile(String filePath) {
        // File-based resolution would need to use Static Resources or Documents
        // For now, throw an exception indicating this is not supported
        throw new A2AException('File-based agent card resolution is not directly supported in Apex. Use Static Resources instead.');
    }
    
    /**
     * Load agent card from Static Resource
     */
    public static A2AAgentCard resolveFromStaticResource(String resourceName) {
        try {
            // Get the static resource
            StaticResource sr = [SELECT Body FROM StaticResource WHERE Name = :resourceName LIMIT 1];
            String jsonContent = sr.Body.toString();
            
            return A2AAgentCard.deserialize(jsonContent);
        } catch (QueryException e) {
            throw new A2AException('Static resource not found: ' + resourceName);
        } catch (Exception e) {
            throw new A2AException('Failed to load agent card from static resource: ' + e.getMessage(), e);
        }
    }
    
    /**
     * Build full URL from base and path
     */
    private String buildUrl(String baseUrl, String path) {
        // Remove trailing slash from base URL
        if (baseUrl.endsWith('/')) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        // Ensure path starts with slash
        if (!path.startsWith('/')) {
            path = '/' + path;
        }
        
        return baseUrl + path;
    }
    
    /**
     * Create a default card resolver
     */
    public static A2ACardResolver createDefault(String baseUrl) {
        return new A2ACardResolver(new A2AHttpClient(), baseUrl);
    }
}