/**
 * Task status update event
 * Converted from Java: io.a2a.spec.TaskStatusUpdateEvent
 */
public class A2ATaskStatusUpdateEvent extends A2ATaskEvent {
    
    public String taskId { get; set; }
    public A2ATaskStatus status { get; set; }
    public A2ATaskStatus previousStatus { get; set; }
    
    /**
     * Default constructor
     */
    public A2ATaskStatusUpdateEvent() {
        super('statusUpdate');
    }
    
    /**
     * Constructor with required fields
     */
    public A2ATaskStatusUpdateEvent(String taskId, A2ATaskStatus status) {
        super('statusUpdate');
        this.taskId = taskId;
        this.status = status;
    }
    
    /**
     * Constructor with all fields
     */
    public A2ATaskStatusUpdateEvent(String taskId, A2ATaskStatus status, A2ATaskStatus previousStatus) {
        super('statusUpdate');
        this.taskId = taskId;
        this.status = status;
        this.previousStatus = previousStatus;
    }
    
    /**
     * Serialize to Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        if (taskId != null) result.put('taskId', taskId);
        if (status != null) result.put('status', status.serialize());
        if (previousStatus != null) result.put('previousStatus', previousStatus.serialize());
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2ATaskStatusUpdateEvent deserializeEvent(Map<String, Object> jsonMap) {
        A2ATaskStatusUpdateEvent event = new A2ATaskStatusUpdateEvent();
        
        event.taskId = (String) jsonMap.get('taskId');
        
        Map<String, Object> statusMap = (Map<String, Object>) jsonMap.get('status');
        if (statusMap != null) {
            event.status = A2ATaskStatus.deserialize(statusMap);
        }
        
        Map<String, Object> prevStatusMap = (Map<String, Object>) jsonMap.get('previousStatus');
        if (prevStatusMap != null) {
            event.previousStatus = A2ATaskStatus.deserialize(prevStatusMap);
        }
        
        return event;
    }
    
    /**
     * Check if this is a state transition
     */
    public Boolean isStateTransition() {
        if (status == null || previousStatus == null) {
            return false;
        }
        return status.state != previousStatus.state;
    }
    
    /**
     * Get state transition description
     */
    public String getTransitionDescription() {
        if (!isStateTransition()) {
            return null;
        }
        return previousStatus.state.name() + ' -> ' + status.state.name();
    }
}