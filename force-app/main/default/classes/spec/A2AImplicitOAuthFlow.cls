/**
 * Configuration for the OAuth Implicit flow.
 * Converted from Java: io.a2a.spec.ImplicitOAuthFlow
 */
public class A2AImplicitOAuthFlow implements A2ASerializable {
    public String authorizationUrl { get; set; }
    public String refreshUrl { get; set; }
    public Map<String, String> scopes { get; set; }
    
    /**
     * Default constructor
     */
    public A2AImplicitOAuthFlow() {}
    
    /**
     * Constructor with all parameters
     */
    public A2AImplicitOAuthFlow(String authorizationUrl, String refreshUrl, Map<String, String> scopes) {
        A2AAssert.checkNotNullParam('authorizationUrl', authorizationUrl);
        A2AAssert.checkNotNullParam('scopes', scopes);
        
        this.authorizationUrl = authorizationUrl;
        this.refreshUrl = refreshUrl;
        this.scopes = scopes;
    }
    
    /**
     * Serialize to Map
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        if (authorizationUrl != null) result.put('authorizationUrl', authorizationUrl);
        if (refreshUrl != null) result.put('refreshUrl', refreshUrl);
        if (scopes != null && !scopes.isEmpty()) result.put('scopes', scopes);
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AImplicitOAuthFlow deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AImplicitOAuthFlow flow = new A2AImplicitOAuthFlow();
        flow.authorizationUrl = (String) jsonMap.get('authorizationUrl');
        flow.refreshUrl = (String) jsonMap.get('refreshUrl');
        
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