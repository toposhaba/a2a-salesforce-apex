/**
 * An agent extension
 * Converted from Java io.a2a.spec.AgentExtension
 */
public class A2AAgentExtension implements A2ASerializable {
    public String description { get; set; }
    public Map<String, Object> params { get; set; }
    public Boolean required { get; set; }
    public String uri { get; set; }
    
    public A2AAgentExtension() {
        // Default constructor
    }
    
    public A2AAgentExtension(String description, Map<String, Object> params, Boolean required, String uri) {
        A2AAssert.checkNotNullParam('uri', uri);
        this.description = description;
        this.params = params;
        this.required = required;
        this.uri = uri;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        if (description != null) result.put('description', description);
        if (params != null) result.put('params', params);
        if (required != null) result.put('required', required);
        if (uri != null) result.put('uri', uri);
        return result;
    }
    
    public static A2AAgentExtension deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AAgentExtension extension = new A2AAgentExtension();
        extension.description = (String) jsonMap.get('description');
        extension.params = (Map<String, Object>) jsonMap.get('params');
        extension.required = (Boolean) jsonMap.get('required');
        extension.uri = (String) jsonMap.get('uri');
        return extension;
    }
    
    // Builder pattern implementation
    public class Builder {
        private String description;
        private Map<String, Object> params;
        private Boolean required;
        private String uri;
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }
        
        public Builder required(Boolean required) {
            this.required = required;
            return this;
        }
        
        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }
        
        public A2AAgentExtension build() {
            return new A2AAgentExtension(description, params, required, uri);
        }
    }
}