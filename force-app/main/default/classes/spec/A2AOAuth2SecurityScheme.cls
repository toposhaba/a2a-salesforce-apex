/**
 * Represents an OAuth2 security scheme.
 * Converted from Java: io.a2a.spec.OAuth2SecurityScheme
 */
public class A2AOAuth2SecurityScheme extends A2ASecurityScheme {
    public static final String OAUTH2 = 'oauth2';
    
    public A2AOAuthFlows flows { get; set; }
    
    /**
     * Constructor with flows and description
     */
    public A2AOAuth2SecurityScheme(A2AOAuthFlows flows, String description) {
        A2AAssert.checkNotNullParam('flows', flows);
        
        this.flows = flows;
        this.description = description;
        this.type = OAUTH2;
    }
    
    /**
     * Constructor with flows, description and type
     */
    public A2AOAuth2SecurityScheme(A2AOAuthFlows flows, String description, String type) {
        A2AAssert.checkNotNullParam('flows', flows);
        
        if (!OAUTH2.equals(type)) {
            throw new A2AException('Invalid type for OAuth2SecurityScheme: ' + type);
        }
        
        this.flows = flows;
        this.description = description;
        this.type = type;
    }
    
    /**
     * Get the scheme type
     */
    public override String getSchemeType() {
        return 'OAuth2';
    }
    
    /**
     * Serialize to Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        if (flows != null) {
            result.put('flows', flows.serialize());
        }
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AOAuth2SecurityScheme deserialize(Map<String, Object> jsonMap) {
        String description = (String) jsonMap.get('description');
        String type = (String) jsonMap.get('type');
        
        Map<String, Object> flowsMap = (Map<String, Object>) jsonMap.get('flows');
        A2AOAuthFlows flows = flowsMap != null ? A2AOAuthFlows.deserialize(flowsMap) : null;
        
        return new A2AOAuth2SecurityScheme(flows, description, type != null ? type : OAUTH2);
    }
    
    /**
     * Builder class for fluent API
     */
    public class Builder {
        private A2AOAuthFlows flows;
        private String description;
        
        public Builder withFlows(A2AOAuthFlows flows) {
            this.flows = flows;
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public A2AOAuth2SecurityScheme build() {
            return new A2AOAuth2SecurityScheme(flows, description);
        }
    }
}