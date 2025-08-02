/**
 * Context for server-side A2A calls.
 * Converted from Java: io.a2a.server.ServerCallContext
 */
public class A2AServerCallContext {
    public String requestId { get; set; }
    public Map<String, String> headers { get; set; }
    public String remoteAddress { get; set; }
    public String authToken { get; set; }
    public Map<String, Object> metadata { get; set; }
    
    /**
     * Default constructor
     */
    public A2AServerCallContext() {
        this.headers = new Map<String, String>();
        this.metadata = new Map<String, Object>();
    }
    
    /**
     * Constructor with request ID
     */
    public A2AServerCallContext(String requestId) {
        this();
        this.requestId = requestId;
    }
    
    /**
     * Get a header value
     */
    public String getHeader(String name) {
        return this.headers != null ? this.headers.get(name) : null;
    }
    
    /**
     * Add a header
     */
    public void addHeader(String name, String value) {
        if (this.headers == null) {
            this.headers = new Map<String, String>();
        }
        this.headers.put(name, value);
    }
    
    /**
     * Get metadata value
     */
    public Object getMetadata(String key) {
        return this.metadata != null ? this.metadata.get(key) : null;
    }
    
    /**
     * Set metadata value
     */
    public void setMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new Map<String, Object>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Check if user is authenticated
     */
    public Boolean isAuthenticated() {
        return String.isNotBlank(this.authToken);
    }
    
    /**
     * Create from REST context
     */
    public static A2AServerCallContext fromRestContext() {
        A2AServerCallContext context = new A2AServerCallContext();
        
        // Get headers from REST context
        RestRequest req = RestContext.request;
        if (req != null) {
            context.requestId = req.headers.get('X-Request-ID');
            context.remoteAddress = req.remoteAddress;
            context.headers = req.headers;
            context.authToken = req.headers.get('Authorization');
        }
        
        return context;
    }
}