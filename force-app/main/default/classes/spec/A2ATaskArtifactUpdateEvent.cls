/**
 * Task artifact update event
 * Converted from Java: io.a2a.spec.TaskArtifactUpdateEvent
 */
public class A2ATaskArtifactUpdateEvent extends A2ATaskEvent {
    
    public String taskId { get; set; }
    public A2AArtifact artifact { get; set; }
    public String action { get; set; } // 'added', 'updated', 'removed'
    
    /**
     * Default constructor
     */
    public A2ATaskArtifactUpdateEvent() {
        super('artifactUpdate');
    }
    
    /**
     * Constructor with required fields
     */
    public A2ATaskArtifactUpdateEvent(String taskId, A2AArtifact artifact, String action) {
        super('artifactUpdate');
        this.taskId = taskId;
        this.artifact = artifact;
        this.action = action;
    }
    
    /**
     * Serialize to Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        if (taskId != null) result.put('taskId', taskId);
        if (artifact != null) result.put('artifact', artifact.serialize());
        if (action != null) result.put('action', action);
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2ATaskArtifactUpdateEvent deserializeEvent(Map<String, Object> jsonMap) {
        A2ATaskArtifactUpdateEvent event = new A2ATaskArtifactUpdateEvent();
        
        event.taskId = (String) jsonMap.get('taskId');
        event.action = (String) jsonMap.get('action');
        
        Map<String, Object> artifactMap = (Map<String, Object>) jsonMap.get('artifact');
        if (artifactMap != null) {
            event.artifact = A2AArtifact.deserialize(artifactMap);
        }
        
        return event;
    }
    
    /**
     * Create an artifact added event
     */
    public static A2ATaskArtifactUpdateEvent artifactAdded(String taskId, A2AArtifact artifact) {
        return new A2ATaskArtifactUpdateEvent(taskId, artifact, 'added');
    }
    
    /**
     * Create an artifact updated event
     */
    public static A2ATaskArtifactUpdateEvent artifactUpdated(String taskId, A2AArtifact artifact) {
        return new A2ATaskArtifactUpdateEvent(taskId, artifact, 'updated');
    }
    
    /**
     * Create an artifact removed event
     */
    public static A2ATaskArtifactUpdateEvent artifactRemoved(String taskId, A2AArtifact artifact) {
        return new A2ATaskArtifactUpdateEvent(taskId, artifact, 'removed');
    }
}