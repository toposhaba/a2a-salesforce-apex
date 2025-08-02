/**
 * Represents an A2A task with its status, artifacts, and history
 * Converted from Java: io.a2a.spec.Task
 */
public class A2ATask implements A2ASerializable {
    
    public String id { get; set; }
    public String contextId { get; set; }
    public A2ATaskStatus status { get; set; }
    public List<A2AArtifact> artifacts { get; set; }
    public List<A2ATaskEvent> history { get; set; }
    public DateTime createdAt { get; set; }
    public DateTime updatedAt { get; set; }
    public Map<String, Object> metadata { get; set; }
    
    /**
     * Default constructor
     */
    public A2ATask() {
        this.artifacts = new List<A2AArtifact>();
        this.history = new List<A2ATaskEvent>();
        this.metadata = new Map<String, Object>();
    }
    
    /**
     * Constructor with required fields
     */
    public A2ATask(String id, A2ATaskStatus status) {
        this();
        this.id = id;
        this.status = status;
    }
    
    /**
     * Serialize to Map
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        if (id != null) result.put('id', id);
        if (contextId != null) result.put('contextId', contextId);
        if (status != null) result.put('status', status.serialize());
        
        if (artifacts != null && !artifacts.isEmpty()) {
            List<Map<String, Object>> artifactsList = new List<Map<String, Object>>();
            for (A2AArtifact artifact : artifacts) {
                artifactsList.add(artifact.serialize());
            }
            result.put('artifacts', artifactsList);
        }
        
        if (history != null && !history.isEmpty()) {
            List<Map<String, Object>> historyList = new List<Map<String, Object>>();
            for (A2ATaskEvent event : history) {
                historyList.add(event.serialize());
            }
            result.put('history', historyList);
        }
        
        if (createdAt != null) result.put('createdAt', createdAt.format());
        if (updatedAt != null) result.put('updatedAt', updatedAt.format());
        if (metadata != null && !metadata.isEmpty()) result.put('metadata', metadata);
        
        return result;
    }
    
    /**
     * Deserialize from JSON string
     */
    public static A2ATask deserialize(String jsonString) {
        Map<String, Object> jsonMap = (Map<String, Object>) JSON.deserializeUntyped(jsonString);
        return deserialize(jsonMap);
    }
    
    /**
     * Deserialize from Map
     */
    public static A2ATask deserialize(Map<String, Object> jsonMap) {
        A2ATask task = new A2ATask();
        
        task.id = (String) jsonMap.get('id');
        task.contextId = (String) jsonMap.get('contextId');
        
        // Deserialize status
        Map<String, Object> statusMap = (Map<String, Object>) jsonMap.get('status');
        if (statusMap != null) {
            task.status = A2ATaskStatus.deserialize(statusMap);
        }
        
        // Deserialize artifacts
        List<Object> artifactsList = (List<Object>) jsonMap.get('artifacts');
        if (artifactsList != null) {
            task.artifacts = new List<A2AArtifact>();
            for (Object artifactObj : artifactsList) {
                Map<String, Object> artifactMap = (Map<String, Object>) artifactObj;
                task.artifacts.add(A2AArtifact.deserialize(artifactMap));
            }
        }
        
        // Deserialize history
        List<Object> historyList = (List<Object>) jsonMap.get('history');
        if (historyList != null) {
            task.history = new List<A2ATaskEvent>();
            for (Object eventObj : historyList) {
                Map<String, Object> eventMap = (Map<String, Object>) eventObj;
                task.history.add(A2ATaskEvent.deserialize(eventMap));
            }
        }
        
        // Parse dates
        String createdAtStr = (String) jsonMap.get('createdAt');
        if (createdAtStr != null) {
            task.createdAt = DateTime.parse(createdAtStr);
        }
        
        String updatedAtStr = (String) jsonMap.get('updatedAt');
        if (updatedAtStr != null) {
            task.updatedAt = DateTime.parse(updatedAtStr);
        }
        
        task.metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        return task;
    }
    
    /**
     * Check if task is in a terminal state
     */
    public Boolean isTerminal() {
        return status != null && 
               (status.state == A2ATaskState.COMPLETED || 
                status.state == A2ATaskState.FAILED || 
                status.state == A2ATaskState.CANCELLED);
    }
    
    /**
     * Check if task is active
     */
    public Boolean isActive() {
        return status != null && 
               (status.state == A2ATaskState.SUBMITTED || 
                status.state == A2ATaskState.RUNNING);
    }
}