/**
 * Provides a declaration of the target url and the supported transport to interact with the agent
 * Converted from Java io.a2a.spec.AgentInterface
 */
public class A2AAgentInterface implements A2ASerializable {
    public String transport { get; set; }
    public String url { get; set; }
    
    public A2AAgentInterface() {
        // Default constructor
    }
    
    public A2AAgentInterface(String transport, String url) {
        A2AAssert.checkNotNullParam('transport', transport);
        A2AAssert.checkNotNullParam('url', url);
        this.transport = transport;
        this.url = url;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        if (transport != null) result.put('transport', transport);
        if (url != null) result.put('url', url);
        return result;
    }
    
    public static A2AAgentInterface deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AAgentInterface agentInterface = new A2AAgentInterface();
        agentInterface.transport = (String) jsonMap.get('transport');
        agentInterface.url = (String) jsonMap.get('url');
        return agentInterface;
    }
}