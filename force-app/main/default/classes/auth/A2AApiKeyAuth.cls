/**
 * API Key authentication provider for A2A
 * Supports header-based and query parameter-based API keys
 */
public class A2AApiKeyAuth implements A2AAuthProvider {
    
    public enum ApiKeyLocation {
        HEADER,
        QUERY_PARAM
    }
    
    private String apiKey;
    private String keyName;
    private ApiKeyLocation location;
    
    /**
     * Constructor for header-based API key
     */
    public A2AApiKeyAuth(String apiKey) {
        this(apiKey, 'X-API-Key', ApiKeyLocation.HEADER);
    }
    
    /**
     * Constructor with custom key name
     */
    public A2AApiKeyAuth(String apiKey, String keyName) {
        this(apiKey, keyName, ApiKeyLocation.HEADER);
    }
    
    /**
     * Constructor with full configuration
     */
    public A2AApiKeyAuth(String apiKey, String keyName, ApiKeyLocation location) {
        if (String.isBlank(apiKey)) {
            throw new A2AException('API key cannot be blank');
        }
        if (String.isBlank(keyName)) {
            throw new A2AException('Key name cannot be blank');
        }
        
        this.apiKey = apiKey;
        this.keyName = keyName;
        this.location = location;
    }
    
    /**
     * Get authentication headers
     */
    public Map<String, String> getAuthHeaders() {
        if (location == ApiKeyLocation.HEADER) {
            return new Map<String, String>{
                keyName => apiKey
            };
        } else {
            // For query params, return empty headers
            // The API key will need to be added to the URL
            return new Map<String, String>();
        }
    }
    
    /**
     * Check if API key is valid
     */
    public Boolean isValid() {
        return String.isNotBlank(apiKey);
    }
    
    /**
     * Refresh not supported for API keys
     */
    public void refresh() {
        // API keys don't support refresh
        A2ALogger.debug('AUTH', 'API key refresh called but not supported');
    }
    
    /**
     * Get authentication type
     */
    public String getAuthType() {
        return 'API Key';
    }
    
    /**
     * Get the API key location
     */
    public ApiKeyLocation getLocation() {
        return location;
    }
    
    /**
     * Get the key name
     */
    public String getKeyName() {
        return keyName;
    }
    
    /**
     * Build URL with API key if using query parameter
     */
    public String buildUrl(String baseUrl) {
        if (location == ApiKeyLocation.QUERY_PARAM) {
            String separator = baseUrl.contains('?') ? '&' : '?';
            return baseUrl + separator + keyName + '=' + EncodingUtil.urlEncode(apiKey, 'UTF-8');
        }
        return baseUrl;
    }
}