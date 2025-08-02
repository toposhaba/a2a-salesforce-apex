/**
 * An agent provider
 * Converted from Java io.a2a.spec.AgentProvider
 */
public class A2AAgentProvider implements A2ASerializable {
    public String organization { get; set; }
    public String url { get; set; }
    
    public A2AAgentProvider() {
        // Default constructor
    }
    
    public A2AAgentProvider(String organization, String url) {
        A2AAssert.checkNotNullParam('organization', organization);
        A2AAssert.checkNotNullParam('url', url);
        this.organization = organization;
        this.url = url;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        if (organization != null) result.put('organization', organization);
        if (url != null) result.put('url', url);
        return result;
    }
    
    public static A2AAgentProvider deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AAgentProvider provider = new A2AAgentProvider();
        provider.organization = (String) jsonMap.get('organization');
        provider.url = (String) jsonMap.get('url');
        return provider;
    }
}