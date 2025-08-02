/**
 * HTTP client wrapper for A2A operations with retry logic and error handling.
 * Converted from Java: io.a2a.http.A2AHttpClient
 */
public class A2AHttpClient {
    private static final Integer DEFAULT_TIMEOUT = 120000; // 120 seconds
    private static final Integer DEFAULT_MAX_RETRIES = 3;
    private static final Integer DEFAULT_RETRY_DELAY = 1000; // 1 second
    
    private Integer timeout;
    private Integer maxRetries;
    private Integer retryDelay;
    
    /**
     * Default constructor
     */
    public A2AHttpClient() {
        this.timeout = DEFAULT_TIMEOUT;
        this.maxRetries = DEFAULT_MAX_RETRIES;
        this.retryDelay = DEFAULT_RETRY_DELAY;
    }
    
    /**
     * Constructor with custom settings
     */
    public A2AHttpClient(Integer timeout, Integer maxRetries, Integer retryDelay) {
        this.timeout = timeout != null ? timeout : DEFAULT_TIMEOUT;
        this.maxRetries = maxRetries != null ? maxRetries : DEFAULT_MAX_RETRIES;
        this.retryDelay = retryDelay != null ? retryDelay : DEFAULT_RETRY_DELAY;
    }
    
    /**
     * Perform a GET request
     */
    public HttpResponse get(String url) {
        return get(url, new Map<String, String>());
    }
    
    /**
     * Perform a GET request with headers
     */
    public HttpResponse get(String url, Map<String, String> headers) {
        HttpRequest request = new HttpRequest();
        request.setEndpoint(url);
        request.setMethod('GET');
        request.setTimeout(this.timeout);
        
        // Set headers
        for (String key : headers.keySet()) {
            request.setHeader(key, headers.get(key));
        }
        
        return executeWithRetry(request);
    }
    
    /**
     * Perform a POST request
     */
    public HttpResponse post(String url, String body, String contentType) {
        return post(url, body, contentType, new Map<String, String>());
    }
    
    /**
     * Perform a POST request with headers
     */
    public HttpResponse post(String url, String body, String contentType, Map<String, String> headers) {
        HttpRequest request = new HttpRequest();
        request.setEndpoint(url);
        request.setMethod('POST');
        request.setTimeout(this.timeout);
        request.setHeader('Content-Type', contentType);
        request.setBody(body);
        
        // Set additional headers
        for (String key : headers.keySet()) {
            request.setHeader(key, headers.get(key));
        }
        
        return executeWithRetry(request);
    }
    
    /**
     * Perform a PUT request
     */
    public HttpResponse put(String url, String body, String contentType) {
        return put(url, body, contentType, new Map<String, String>());
    }
    
    /**
     * Perform a PUT request with headers
     */
    public HttpResponse put(String url, String body, String contentType, Map<String, String> headers) {
        HttpRequest request = new HttpRequest();
        request.setEndpoint(url);
        request.setMethod('PUT');
        request.setTimeout(this.timeout);
        request.setHeader('Content-Type', contentType);
        request.setBody(body);
        
        // Set additional headers
        for (String key : headers.keySet()) {
            request.setHeader(key, headers.get(key));
        }
        
        return executeWithRetry(request);
    }
    
    /**
     * Perform a DELETE request
     */
    public HttpResponse doDelete(String url) {
        return doDelete(url, new Map<String, String>());
    }
    
    /**
     * Perform a DELETE request with headers
     */
    public HttpResponse doDelete(String url, Map<String, String> headers) {
        HttpRequest request = new HttpRequest();
        request.setEndpoint(url);
        request.setMethod('DELETE');
        request.setTimeout(this.timeout);
        
        // Set headers
        for (String key : headers.keySet()) {
            request.setHeader(key, headers.get(key));
        }
        
        return executeWithRetry(request);
    }
    
    /**
     * Execute request with retry logic
     */
    private HttpResponse executeWithRetry(HttpRequest request) {
        Http http = new Http();
        HttpResponse response;
        Integer attempt = 0;
        
        while (attempt < this.maxRetries) {
            try {
                response = http.send(request);
                
                // Check if we should retry based on status code
                if (shouldRetry(response.getStatusCode()) && attempt < this.maxRetries - 1) {
                    attempt++;
                    // Wait before retrying
                    if (this.retryDelay > 0) {
                        Long startTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - startTime < this.retryDelay) {
                            // Busy wait to simulate sleep
                        }
                    }
                    continue;
                }
                
                return response;
                
            } catch (Exception e) {
                attempt++;
                if (attempt >= this.maxRetries) {
                    throw new A2AException('HTTP request failed after ' + this.maxRetries + ' attempts: ' + e.getMessage(), e);
                }
                
                // Wait before retrying
                if (this.retryDelay > 0 && attempt < this.maxRetries) {
                    Long startTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTime < this.retryDelay) {
                        // Busy wait to simulate sleep
                    }
                }
            }
        }
        
        throw new A2AException('HTTP request failed after ' + this.maxRetries + ' attempts');
    }
    
    /**
     * Determine if we should retry based on status code
     */
    private Boolean shouldRetry(Integer statusCode) {
        // Retry on 5xx errors and specific 4xx errors
        return statusCode >= 500 || statusCode == 408 || statusCode == 429;
    }
    
    /**
     * Builder class for fluent API
     */
    public class Builder {
        private Integer timeout;
        private Integer maxRetries;
        private Integer retryDelay;
        
        public Builder withTimeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }
        
        public Builder withMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder withRetryDelay(Integer retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }
        
        public A2AHttpClient build() {
            return new A2AHttpClient(timeout, maxRetries, retryDelay);
        }
    }
    
    /**
     * Create a builder instance
     */
    public static Builder builder() {
        return new A2AHttpClient().new Builder();
    }
}