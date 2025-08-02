/**
 * An agent's capabilities
 * Converted from Java io.a2a.spec.AgentCapabilities
 */
public class A2AAgentCapabilities implements A2ASerializable {
    public Boolean streaming { get; set; }
    public Boolean pushNotifications { get; set; }
    public Boolean stateTransitionHistory { get; set; }
    public List<A2AAgentExtension> extensions { get; set; }
    
    public A2AAgentCapabilities() {
        // Default constructor
        this.extensions = new List<A2AAgentExtension>();
    }
    
    public A2AAgentCapabilities(Boolean streaming, Boolean pushNotifications, 
                               Boolean stateTransitionHistory, List<A2AAgentExtension> extensions) {
        this.streaming = streaming;
        this.pushNotifications = pushNotifications;
        this.stateTransitionHistory = stateTransitionHistory;
        this.extensions = extensions != null ? extensions : new List<A2AAgentExtension>();
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        if (streaming != null) result.put('streaming', streaming);
        if (pushNotifications != null) result.put('pushNotifications', pushNotifications);
        if (stateTransitionHistory != null) result.put('stateTransitionHistory', stateTransitionHistory);
        
        if (extensions != null && !extensions.isEmpty()) {
            List<Map<String, Object>> extensionsList = new List<Map<String, Object>>();
            for (A2AAgentExtension extension : extensions) {
                extensionsList.add(extension.serialize());
            }
            result.put('extensions', extensionsList);
        }
        
        return result;
    }
    
    public static A2AAgentCapabilities deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AAgentCapabilities capabilities = new A2AAgentCapabilities();
        capabilities.streaming = (Boolean) jsonMap.get('streaming');
        capabilities.pushNotifications = (Boolean) jsonMap.get('pushNotifications');
        capabilities.stateTransitionHistory = (Boolean) jsonMap.get('stateTransitionHistory');
        
        List<Object> extensionsList = (List<Object>) jsonMap.get('extensions');
        if (extensionsList != null) {
            capabilities.extensions = new List<A2AAgentExtension>();
            for (Object obj : extensionsList) {
                capabilities.extensions.add(A2AAgentExtension.deserialize((Map<String, Object>) obj));
            }
        }
        
        return capabilities;
    }
    
    // Builder pattern implementation
    public class Builder {
        private Boolean streaming;
        private Boolean pushNotifications;
        private Boolean stateTransitionHistory;
        private List<A2AAgentExtension> extensions;
        
        public Builder streaming(Boolean streaming) {
            this.streaming = streaming;
            return this;
        }
        
        public Builder pushNotifications(Boolean pushNotifications) {
            this.pushNotifications = pushNotifications;
            return this;
        }
        
        public Builder stateTransitionHistory(Boolean stateTransitionHistory) {
            this.stateTransitionHistory = stateTransitionHistory;
            return this;
        }
        
        public Builder extensions(List<A2AAgentExtension> extensions) {
            this.extensions = extensions;
            return this;
        }
        
        public A2AAgentCapabilities build() {
            return new A2AAgentCapabilities(streaming, pushNotifications, stateTransitionHistory, extensions);
        }
    }
}