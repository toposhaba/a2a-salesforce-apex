/**
 * Base class for task events in the history
 * Represents events that occur during task lifecycle
 */
public abstract class A2ATaskEvent implements A2ASerializable {
    
    public String eventType { get; set; }
    public DateTime timestamp { get; set; }
    public Map<String, Object> metadata { get; set; }
    
    /**
     * Constructor
     */
    public A2ATaskEvent(String eventType) {
        this.eventType = eventType;
        this.timestamp = DateTime.now();
        this.metadata = new Map<String, Object>();
    }
    
    /**
     * Serialize to Map
     */
    public virtual Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        result.put('eventType', eventType);
        result.put('timestamp', timestamp.format());
        
        if (metadata != null && !metadata.isEmpty()) {
            result.put('metadata', metadata);
        }
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2ATaskEvent deserialize(Map<String, Object> jsonMap) {
        String eventType = (String) jsonMap.get('eventType');
        
        A2ATaskEvent event;
        
        // Create appropriate event type
        if ('statusUpdate'.equals(eventType)) {
            event = A2ATaskStatusUpdateEvent.deserializeEvent(jsonMap);
        } else if ('artifactUpdate'.equals(eventType)) {
            event = A2ATaskArtifactUpdateEvent.deserializeEvent(jsonMap);
        } else {
            // Generic event
            event = new A2AGenericTaskEvent(eventType);
        }
        
        // Common fields
        String timestampStr = (String) jsonMap.get('timestamp');
        if (timestampStr != null) {
            try {
                event.timestamp = DateTime.parse(timestampStr);
            } catch (Exception e) {
                event.timestamp = DateTime.now();
            }
        }
        
        event.metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        return event;
    }
    
    /**
     * Generic task event implementation
     */
    public class A2AGenericTaskEvent extends A2ATaskEvent {
        public A2AGenericTaskEvent(String eventType) {
            super(eventType);
        }
    }
}