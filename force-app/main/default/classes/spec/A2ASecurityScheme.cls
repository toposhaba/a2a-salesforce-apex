/**
 * Base class for security schemes
 * Converted from Java io.a2a.spec.SecurityScheme
 */
public abstract class A2ASecurityScheme implements A2ASerializable {
    public String type { get; set; }
    public String description { get; set; }
    
    // Security scheme type constants
    public static final String TYPE_API_KEY = 'apiKey';
    public static final String TYPE_HTTP = 'http';
    public static final String TYPE_OAUTH2 = 'oauth2';
    public static final String TYPE_OPENID_CONNECT = 'openIdConnect';
    
    public A2ASecurityScheme(String type) {
        this.type = type;
    }
    
    public abstract Map<String, Object> serialize();
    
    /**
     * Factory method to deserialize SecurityScheme based on type
     */
    public static A2ASecurityScheme deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        String schemeType = (String) jsonMap.get('type');
        if (schemeType == null) {
            throw new A2AException('Security scheme type is required');
        }
        
        switch on schemeType {
            when 'apiKey' {
                return A2AAPIKeySecurityScheme.deserialize(jsonMap);
            }
            when 'http' {
                return A2AHTTPAuthSecurityScheme.deserialize(jsonMap);
            }
            when 'oauth2' {
                return A2AOAuth2SecurityScheme.deserialize(jsonMap);
            }
            when 'openIdConnect' {
                return A2AOpenIdConnectSecurityScheme.deserialize(jsonMap);
            }
            when else {
                throw new A2AException('Unknown security scheme type: ' + schemeType);
            }
        }
    }
}