/**
 * Configuration for the OAuth Authorization Code flow.
 * Converted from Java: io.a2a.spec.AuthorizationCodeOAuthFlow
 */
public class A2AAuthorizationCodeOAuthFlow implements A2ASerializable {
    public String authorizationUrl { get; set; }
    public String refreshUrl { get; set; }
    public Map<String, String> scopes { get; set; }
    public String tokenUrl { get; set; }
    
    /**
     * Default constructor
     */
    public A2AAuthorizationCodeOAuthFlow() {}
    
    /**
     * Constructor with required parameters
     */
    public A2AAuthorizationCodeOAuthFlow(String authorizationUrl, Map<String, String> scopes, String tokenUrl) {
        this(authorizationUrl, null, scopes, tokenUrl);
    }
    
    /**
     * Constructor with all parameters
     */
    public A2AAuthorizationCodeOAuthFlow(String authorizationUrl, String refreshUrl, Map<String, String> scopes, String tokenUrl) {
        A2AAssert.checkNotNullParam('authorizationUrl', authorizationUrl);
        A2AAssert.checkNotNullParam('scopes', scopes);
        A2AAssert.checkNotNullParam('tokenUrl', tokenUrl);
        
        this.authorizationUrl = authorizationUrl;
        this.refreshUrl = refreshUrl;
        this.scopes = scopes;
        this.tokenUrl = tokenUrl;
    }
    
    /**
     * Serialize to Map
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        if (authorizationUrl != null) result.put('authorizationUrl', authorizationUrl);
        if (refreshUrl != null) result.put('refreshUrl', refreshUrl);
        if (scopes != null && !scopes.isEmpty()) result.put('scopes', scopes);
        if (tokenUrl != null) result.put('tokenUrl', tokenUrl);
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AAuthorizationCodeOAuthFlow deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AAuthorizationCodeOAuthFlow flow = new A2AAuthorizationCodeOAuthFlow();
        flow.authorizationUrl = (String) jsonMap.get('authorizationUrl');
        flow.refreshUrl = (String) jsonMap.get('refreshUrl');
        flow.tokenUrl = (String) jsonMap.get('tokenUrl');
        
        Map<String, Object> scopesObj = (Map<String, Object>) jsonMap.get('scopes');
        if (scopesObj != null) {
            flow.scopes = new Map<String, String>();
            for (String key : scopesObj.keySet()) {
                flow.scopes.put(key, String.valueOf(scopesObj.get(key)));
            }
        }
        
        return flow;
    }
}