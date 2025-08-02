/**
 * Bearer token authentication provider for A2A
 * Supports static tokens and token refresh
 */
public class A2ABearerTokenAuth implements A2AAuthProvider {
    
    private String token;
    private DateTime tokenExpiry;
    private String refreshToken;
    private String tokenEndpoint;
    private Map<String, String> refreshParams;
    
    /**
     * Constructor for static bearer token
     */
    public A2ABearerTokenAuth(String token) {
        this.token = token;
    }
    
    /**
     * Constructor for bearer token with expiry
     */
    public A2ABearerTokenAuth(String token, DateTime expiry) {
        this.token = token;
        this.tokenExpiry = expiry;
    }
    
    /**
     * Constructor for refreshable bearer token
     */
    public A2ABearerTokenAuth(String token, String refreshToken, String tokenEndpoint) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.tokenEndpoint = tokenEndpoint;
        this.refreshParams = new Map<String, String>();
    }
    
    /**
     * Get authentication headers
     */
    public Map<String, String> getAuthHeaders() {
        if (!isValid()) {
            refresh();
        }
        
        return new Map<String, String>{
            'Authorization' => 'Bearer ' + token
        };
    }
    
    /**
     * Check if token is valid
     */
    public Boolean isValid() {
        if (String.isBlank(token)) {
            return false;
        }
        
        if (tokenExpiry != null && tokenExpiry <= DateTime.now()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Refresh token if supported
     */
    public void refresh() {
        if (String.isBlank(refreshToken) || String.isBlank(tokenEndpoint)) {
            throw new A2AException('Token refresh not supported for this authentication');
        }
        
        try {
            A2ALogger.info('AUTH', 'Refreshing bearer token', new Map<String, Object>{
                'endpoint' => tokenEndpoint
            });
            
            // Prepare refresh request
            HttpRequest req = new HttpRequest();
            req.setEndpoint(tokenEndpoint);
            req.setMethod('POST');
            req.setHeader('Content-Type', 'application/x-www-form-urlencoded');
            
            // Build request body
            String body = 'grant_type=refresh_token&refresh_token=' + EncodingUtil.urlEncode(refreshToken, 'UTF-8');
            for (String key : refreshParams.keySet()) {
                body += '&' + key + '=' + EncodingUtil.urlEncode(refreshParams.get(key), 'UTF-8');
            }
            req.setBody(body);
            
            // Send request
            Http http = new Http();
            HttpResponse res = http.send(req);
            
            if (res.getStatusCode() == 200) {
                // Parse response
                Map<String, Object> responseData = (Map<String, Object>) JSON.deserializeUntyped(res.getBody());
                
                // Update token
                this.token = (String) responseData.get('access_token');
                
                // Update refresh token if provided
                if (responseData.containsKey('refresh_token')) {
                    this.refreshToken = (String) responseData.get('refresh_token');
                }
                
                // Update expiry
                if (responseData.containsKey('expires_in')) {
                    Integer expiresIn = (Integer) responseData.get('expires_in');
                    this.tokenExpiry = DateTime.now().addSeconds(expiresIn - 60); // Subtract 60s for buffer
                }
                
                A2ALogger.info('AUTH', 'Token refreshed successfully');
                
            } else {
                throw new A2AException('Failed to refresh token: ' + res.getStatus());
            }
            
        } catch (Exception e) {
            A2ALogger.error('AUTH', 'Token refresh failed', e);
            throw new A2AException('Token refresh failed: ' + e.getMessage(), e);
        }
    }
    
    /**
     * Get authentication type
     */
    public String getAuthType() {
        return 'Bearer';
    }
    
    /**
     * Set refresh parameters
     */
    public void setRefreshParams(Map<String, String> params) {
        this.refreshParams = params != null ? params : new Map<String, String>();
    }
    
    /**
     * Get current token (for debugging)
     */
    public String getToken() {
        return token;
    }
    
    /**
     * Get token expiry
     */
    public DateTime getTokenExpiry() {
        return tokenExpiry;
    }
}