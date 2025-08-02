/**
 * Configuration for the OAuth Client Credentials flow.
 * Converted from Java: io.a2a.spec.ClientCredentialsOAuthFlow
 */
public class A2AClientCredentialsOAuthFlow implements A2ASerializable {
    public String refreshUrl { get; set; }
    public Map<String, String> scopes { get; set; }
    public String tokenUrl { get; set; }
    
    /**
     * Default constructor
     */
    public A2AClientCredentialsOAuthFlow() {}
    
    /**
     * Constructor with all parameters
     */
    public A2AClientCredentialsOAuthFlow(String refreshUrl, Map<String, String> scopes, String tokenUrl) {
        A2AAssert.checkNotNullParam('scopes', scopes);
        A2AAssert.checkNotNullParam('tokenUrl', tokenUrl);
        
        this.refreshUrl = refreshUrl;
        this.scopes = scopes;
        this.tokenUrl = tokenUrl;
    }
    
    /**
     * Serialize to Map
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        if (refreshUrl != null) result.put('refreshUrl', refreshUrl);
        if (scopes != null && !scopes.isEmpty()) result.put('scopes', scopes);
        if (tokenUrl != null) result.put('tokenUrl', tokenUrl);
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AClientCredentialsOAuthFlow deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AClientCredentialsOAuthFlow flow = new A2AClientCredentialsOAuthFlow();
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