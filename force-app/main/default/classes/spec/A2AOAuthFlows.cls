/**
 * Allows configuration of the supported OAuth Flows.
 * Converted from Java: io.a2a.spec.OAuthFlows
 */
public class A2AOAuthFlows implements A2ASerializable {
    public A2AAuthorizationCodeOAuthFlow authorizationCode { get; set; }
    public A2AClientCredentialsOAuthFlow clientCredentials { get; set; }
    public A2AImplicitOAuthFlow implicit { get; set; }
    public A2APasswordOAuthFlow password { get; set; }
    
    /**
     * Default constructor
     */
    public A2AOAuthFlows() {}
    
    /**
     * Constructor with all flows
     */
    public A2AOAuthFlows(
        A2AAuthorizationCodeOAuthFlow authorizationCode,
        A2AClientCredentialsOAuthFlow clientCredentials,
        A2AImplicitOAuthFlow implicit,
        A2APasswordOAuthFlow password
    ) {
        this.authorizationCode = authorizationCode;
        this.clientCredentials = clientCredentials;
        this.implicit = implicit;
        this.password = password;
    }
    
    /**
     * Serialize to Map
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        if (authorizationCode != null) {
            result.put('authorizationCode', authorizationCode.serialize());
        }
        if (clientCredentials != null) {
            result.put('clientCredentials', clientCredentials.serialize());
        }
        if (implicit != null) {
            result.put('implicit', implicit.serialize());
        }
        if (password != null) {
            result.put('password', password.serialize());
        }
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AOAuthFlows deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AOAuthFlows flows = new A2AOAuthFlows();
        
        Map<String, Object> authCodeMap = (Map<String, Object>) jsonMap.get('authorizationCode');
        if (authCodeMap != null) {
            flows.authorizationCode = A2AAuthorizationCodeOAuthFlow.deserialize(authCodeMap);
        }
        
        Map<String, Object> clientCredsMap = (Map<String, Object>) jsonMap.get('clientCredentials');
        if (clientCredsMap != null) {
            flows.clientCredentials = A2AClientCredentialsOAuthFlow.deserialize(clientCredsMap);
        }
        
        Map<String, Object> implicitMap = (Map<String, Object>) jsonMap.get('implicit');
        if (implicitMap != null) {
            flows.implicit = A2AImplicitOAuthFlow.deserialize(implicitMap);
        }
        
        Map<String, Object> passwordMap = (Map<String, Object>) jsonMap.get('password');
        if (passwordMap != null) {
            flows.password = A2APasswordOAuthFlow.deserialize(passwordMap);
        }
        
        return flows;
    }
    
    /**
     * Builder class for fluent API
     */
    public class Builder {
        private A2AAuthorizationCodeOAuthFlow authorizationCode;
        private A2AClientCredentialsOAuthFlow clientCredentials;
        private A2AImplicitOAuthFlow implicit;
        private A2APasswordOAuthFlow password;
        
        public Builder withAuthorizationCode(A2AAuthorizationCodeOAuthFlow authorizationCode) {
            this.authorizationCode = authorizationCode;
            return this;
        }
        
        public Builder withClientCredentials(A2AClientCredentialsOAuthFlow clientCredentials) {
            this.clientCredentials = clientCredentials;
            return this;
        }
        
        public Builder withImplicit(A2AImplicitOAuthFlow implicit) {
            this.implicit = implicit;
            return this;
        }
        
        public Builder withPassword(A2APasswordOAuthFlow password) {
            this.password = password;
            return this;
        }
        
        public A2AOAuthFlows build() {
            return new A2AOAuthFlows(authorizationCode, clientCredentials, implicit, password);
        }
    }
}