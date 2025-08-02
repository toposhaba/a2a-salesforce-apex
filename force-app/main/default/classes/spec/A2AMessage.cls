/**
 * An A2A message
 * Converted from Java io.a2a.spec.Message
 */
public class A2AMessage implements A2ASerializable {
    public String id { get; set; }
    public String role { get; set; }
    public List<A2APart> parts { get; set; }
    public String contextId { get; set; }
    public String taskId { get; set; }
    public List<String> referenceTaskIds { get; set; }
    public Map<String, Object> metadata { get; set; }
    public String kind { get; set; }
    
    // Role enum
    public enum Role {
        USER,
        AGENT
    }
    
    // Constants
    public static final String KIND_MESSAGE = 'message';
    
    public A2AMessage() {
        this.parts = new List<A2APart>();
        this.referenceTaskIds = new List<String>();
        this.metadata = new Map<String, Object>();
        this.kind = KIND_MESSAGE;
    }
    
    public A2AMessage(String role, List<A2APart> parts, String messageId, String contextId, 
                     String taskId, List<String> referenceTaskIds, Map<String, Object> metadata) {
        A2AAssert.checkNotNullParam('parts', parts);
        A2AAssert.checkNotNullParam('role', role);
        
        if (parts.isEmpty()) {
            throw new A2AException('Parts cannot be empty');
        }
        
        this.id = messageId != null ? messageId : generateId();
        this.role = role;
        this.parts = parts;
        this.contextId = contextId;
        this.taskId = taskId;
        this.referenceTaskIds = referenceTaskIds != null ? referenceTaskIds : new List<String>();
        this.metadata = metadata != null ? metadata : new Map<String, Object>();
        this.kind = KIND_MESSAGE;
    }
    
    /**
     * Generate a unique message ID
     */
    private String generateId() {
        // Generate a UUID-like string
        Blob b = Crypto.GenerateAESKey(128);
        String h = EncodingUtil.ConvertToHex(b);
        return h.substring(0,8) + '-' + h.substring(8,12) + '-' + h.substring(12,16) + '-' + h.substring(16,20) + '-' + h.substring(20);
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        result.put('messageId', id);
        result.put('role', role);
        result.put('kind', kind);
        
        // Serialize parts
        List<Map<String, Object>> partsList = new List<Map<String, Object>>();
        for (A2APart part : parts) {
            partsList.add(part.serialize());
        }
        result.put('parts', partsList);
        
        // Optional fields
        if (contextId != null) result.put('contextId', contextId);
        if (taskId != null) result.put('taskId', taskId);
        if (referenceTaskIds != null && !referenceTaskIds.isEmpty()) {
            result.put('referenceTaskIds', referenceTaskIds);
        }
        if (metadata != null && !metadata.isEmpty()) {
            result.put('metadata', metadata);
        }
        
        return result;
    }
    
    public static A2AMessage deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AMessage message = new A2AMessage();
        message.id = (String) jsonMap.get('messageId');
        message.role = (String) jsonMap.get('role');
        message.contextId = (String) jsonMap.get('contextId');
        message.taskId = (String) jsonMap.get('taskId');
        message.kind = (String) jsonMap.get('kind');
        
        // Deserialize parts
        List<Object> partsList = (List<Object>) jsonMap.get('parts');
        if (partsList != null) {
            message.parts = new List<A2APart>();
            for (Object partObj : partsList) {
                message.parts.add(A2APart.deserialize((Map<String, Object>) partObj));
            }
        }
        
        // Reference task IDs
        List<Object> refTaskIds = (List<Object>) jsonMap.get('referenceTaskIds');
        if (refTaskIds != null) {
            message.referenceTaskIds = new List<String>();
            for (Object taskId : refTaskIds) {
                message.referenceTaskIds.add((String) taskId);
            }
        }
        
        // Metadata
        message.metadata = (Map<String, Object>) jsonMap.get('metadata');
        if (message.metadata == null) {
            message.metadata = new Map<String, Object>();
        }
        
        return message;
    }
    
    /**
     * Create a message with text content
     */
    public static A2AMessage createTextMessage(String text, Role role) {
        A2AMessage message = new A2AMessage();
        message.role = role.name();
        message.parts = new List<A2APart>{new A2ATextPart(text)};
        return message;
    }
}