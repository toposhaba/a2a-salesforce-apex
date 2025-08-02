/**
 * Represents an OpenID Connect security scheme.
 * Converted from Java: io.a2a.spec.OpenIdConnectSecurityScheme
 */
public class A2AOpenIdConnectSecurityScheme extends A2ASecurityScheme {
    public static final String OPENID_CONNECT = 'openIdConnect';
    
    public String openIdConnectUrl { get; set; }
    
    /**
     * Constructor with URL and description
     */
    public A2AOpenIdConnectSecurityScheme(String openIdConnectUrl, String description) {
        this(openIdConnectUrl, description, OPENID_CONNECT);
    }
    
    /**
     * Constructor with all parameters
     */
    public A2AOpenIdConnectSecurityScheme(String openIdConnectUrl, String description, String type) {
        if (!OPENID_CONNECT.equals(type)) {
            throw new A2AException('Invalid type for OpenIdConnectSecurityScheme: ' + type);
        }
        
        this.openIdConnectUrl = openIdConnectUrl;
        this.description = description;
        this.type = type;
    }
    
    /**
     * Get the scheme type
     */
    public override String getSchemeType() {
        return 'OpenIdConnect';
    }
    
    /**
     * Serialize to Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        if (openIdConnectUrl != null) result.put('openIdConnectUrl', openIdConnectUrl);
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AOpenIdConnectSecurityScheme deserialize(Map<String, Object> jsonMap) {
        String openIdConnectUrl = (String) jsonMap.get('openIdConnectUrl');
        String description = (String) jsonMap.get('description');
        String type = (String) jsonMap.get('type');
        
        return new A2AOpenIdConnectSecurityScheme(openIdConnectUrl, description, type != null ? type : OPENID_CONNECT);
    }
    
    /**
     * Builder class for fluent API
     */
    public class Builder {
        private String openIdConnectUrl;
        private String description;
        
        public Builder withOpenIdConnectUrl(String openIdConnectUrl) {
            this.openIdConnectUrl = openIdConnectUrl;
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public A2AOpenIdConnectSecurityScheme build() {
            return new A2AOpenIdConnectSecurityScheme(openIdConnectUrl, description);
        }
    }
}