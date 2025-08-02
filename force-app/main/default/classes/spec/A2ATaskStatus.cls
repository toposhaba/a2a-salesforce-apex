/**
 * Represents the status of an A2A task
 * Converted from Java: io.a2a.spec.TaskStatus
 */
public class A2ATaskStatus implements A2ASerializable {
    
    public A2ATaskState state { get; set; }
    public String message { get; set; }
    public Map<String, Object> metadata { get; set; }
    
    /**
     * Default constructor
     */
    public A2ATaskStatus() {
        this.metadata = new Map<String, Object>();
    }
    
    /**
     * Constructor with state
     */
    public A2ATaskStatus(A2ATaskState state) {
        this();
        this.state = state;
    }
    
    /**
     * Constructor with all fields
     */
    public A2ATaskStatus(A2ATaskState state, String message, Map<String, Object> metadata) {
        this.state = state;
        this.message = message;
        this.metadata = metadata != null ? metadata : new Map<String, Object>();
    }
    
    /**
     * Serialize to Map
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        if (state != null) {
            result.put('state', state.name());
        }
        
        if (message != null) {
            result.put('message', message);
        }
        
        if (metadata != null && !metadata.isEmpty()) {
            result.put('metadata', metadata);
        }
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2ATaskStatus deserialize(Map<String, Object> jsonMap) {
        A2ATaskStatus status = new A2ATaskStatus();
        
        String stateStr = (String) jsonMap.get('state');
        if (stateStr != null) {
            status.state = A2ATaskState.valueOf(stateStr);
        }
        
        status.message = (String) jsonMap.get('message');
        status.metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        return status;
    }
    
    /**
     * Create a submitted status
     */
    public static A2ATaskStatus submitted() {
        return new A2ATaskStatus(A2ATaskState.SUBMITTED);
    }
    
    /**
     * Create a running status
     */
    public static A2ATaskStatus running() {
        return new A2ATaskStatus(A2ATaskState.RUNNING);
    }
    
    /**
     * Create a completed status
     */
    public static A2ATaskStatus completed() {
        return new A2ATaskStatus(A2ATaskState.COMPLETED);
    }
    
    /**
     * Create a failed status
     */
    public static A2ATaskStatus failed(String message) {
        return new A2ATaskStatus(A2ATaskState.FAILED, message, null);
    }
    
    /**
     * Create a cancelled status
     */
    public static A2ATaskStatus cancelled() {
        return new A2ATaskStatus(A2ATaskState.CANCELLED);
    }
}