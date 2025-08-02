/**
 * HTTP Client wrapper for A2A SDK
 * Provides abstraction over Salesforce HTTP callouts
 */
public class A2AHttpClient {
    private Integer timeout;
    private Map<String, String> defaultHeaders;
    
    public A2AHttpClient() {
        this.timeout = 120000; // 2 minutes default
        this.defaultHeaders = new Map<String, String>{
            'Accept' => 'application/json',
            'Content-Type' => 'application/json'
        };
    }
    
    /**
     * Set timeout in milliseconds
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    
    /**
     * Add a default header
     */
    public void addDefaultHeader(String key, String value) {
        this.defaultHeaders.put(key, value);
    }
    
    /**
     * Execute HTTP request
     * TODO: Add retry logic, error handling, etc.
     */
    public HttpResponse send(HttpRequest request) {
        // Apply default headers
        for (String key : defaultHeaders.keySet()) {
            if (request.getHeader(key) == null) {
                request.setHeader(key, defaultHeaders.get(key));
            }
        }
        
        request.setTimeout(timeout);
        
        Http http = new Http();
        return http.send(request);
    }
}