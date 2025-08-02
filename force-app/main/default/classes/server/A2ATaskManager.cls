/**
 * Manages A2A tasks including creation, updates, and persistence.
 * Converted from Java: io.a2a.server.tasks.TaskManager
 */
public class A2ATaskManager {
    private String taskId;
    private String contextId;
    private A2ATaskStore taskStore;
    private A2AMessage initialMessage;
    private A2ATask currentTask;
    
    /**
     * Constructor
     */
    public A2ATaskManager(String taskId, String contextId, A2ATaskStore taskStore, A2AMessage initialMessage) {
        A2AAssert.checkNotNullParam('taskStore', taskStore);
        this.taskId = taskId;
        this.contextId = contextId;
        this.taskStore = taskStore;
        this.initialMessage = initialMessage;
    }
    
    /**
     * Get task ID
     */
    public String getTaskId() {
        return this.taskId;
    }
    
    /**
     * Get context ID
     */
    public String getContextId() {
        return this.contextId;
    }
    
    /**
     * Get the current task
     */
    public A2ATask getTask() {
        if (this.taskId == null) {
            return null;
        }
        if (this.currentTask != null) {
            return this.currentTask;
        }
        this.currentTask = taskStore.get(this.taskId);
        return this.currentTask;
    }
    
    /**
     * Save task event
     */
    public A2ATask saveTaskEvent(A2ATask task) {
        checkIdsAndUpdateIfNecessary(task.id, task.contextId);
        return saveTask(task);
    }
    
    /**
     * Save task status update event
     */
    public A2ATask saveTaskStatusUpdateEvent(A2ATaskStatusUpdateEvent event) {
        checkIdsAndUpdateIfNecessary(event.taskId, event.contextId);
        A2ATask task = ensureTask(event.taskId, event.contextId);
        
        // Update task status
        task.status = event.status;
        
        // Add message to history if present
        if (task.statusMessage != null) {
            if (task.history == null) {
                task.history = new List<A2AMessage>();
            }
            task.history.add(task.statusMessage);
        }
        
        return saveTask(task);
    }
    
    /**
     * Save task artifact update event
     */
    public A2ATask saveTaskArtifactUpdateEvent(A2ATaskArtifactUpdateEvent event) {
        checkIdsAndUpdateIfNecessary(event.taskId, event.contextId);
        A2ATask task = ensureTask(event.taskId, event.contextId);
        
        // Initialize artifacts list if needed
        if (task.artifacts == null) {
            task.artifacts = new List<A2AArtifact>();
        }
        
        A2AArtifact newArtifact = event.artifact;
        String artifactId = newArtifact.artifactId;
        Boolean appendParts = event.isAppend != null && event.isAppend;
        
        // Find existing artifact
        A2AArtifact existingArtifact = null;
        Integer existingArtifactIndex = -1;
        
        for (Integer i = 0; i < task.artifacts.size(); i++) {
            A2AArtifact curr = task.artifacts[i];
            if (curr.artifactId != null && curr.artifactId.equals(artifactId)) {
                existingArtifact = curr;
                existingArtifactIndex = i;
                break;
            }
        }
        
        if (existingArtifact != null) {
            if (appendParts && existingArtifact.parts != null && newArtifact.parts != null) {
                // Append parts to existing artifact
                existingArtifact.parts.addAll(newArtifact.parts);
                System.debug('Appending parts to artifact at id ' + artifactId + ' for task ' + taskId);
            } else {
                // Replace existing artifact
                task.artifacts[existingArtifactIndex] = newArtifact;
                System.debug('Replacing artifact at id ' + artifactId + ' for task ' + taskId);
            }
        } else {
            // Add new artifact
            task.artifacts.add(newArtifact);
            System.debug('Adding artifact at id ' + artifactId + ' for task ' + taskId);
        }
        
        return saveTask(task);
    }
    
    /**
     * Cancel the task
     */
    public Boolean cancelTask() {
        if (this.taskId == null) {
            return false;
        }
        
        A2ATask task = getTask();
        if (task == null) {
            return false;
        }
        
        // Check if task can be cancelled
        if (task.status == A2ATaskStatus.COMPLETED || task.status == A2ATaskStatus.FAILED) {
            return false;
        }
        
        task.status = A2ATaskStatus.CANCELLED;
        saveTask(task);
        return true;
    }
    
    /**
     * Private helper methods
     */
    private void checkIdsAndUpdateIfNecessary(String newTaskId, String newContextId) {
        if (this.taskId == null && newTaskId != null) {
            this.taskId = newTaskId;
        }
        if (this.contextId == null && newContextId != null) {
            this.contextId = newContextId;
        }
    }
    
    private A2ATask ensureTask(String taskId, String contextId) {
        A2ATask task = getTask();
        if (task == null) {
            // Create new task
            task = new A2ATask();
            task.id = taskId;
            task.contextId = contextId;
            task.status = A2ATaskStatus.SUBMITTED;
            task.createdAt = DateTime.now();
            task.updatedAt = DateTime.now();
            
            if (this.initialMessage != null) {
                task.history = new List<A2AMessage>{ this.initialMessage };
            }
        }
        return task;
    }
    
    private A2ATask saveTask(A2ATask task) {
        task.updatedAt = DateTime.now();
        this.currentTask = task;
        return taskStore.save(task);
    }
    
    /**
     * Helper classes
     */
    public class A2ATask {
        public String id { get; set; }
        public String contextId { get; set; }
        public A2ATaskStatus status { get; set; }
        public A2AMessage statusMessage { get; set; }
        public List<A2AArtifact> artifacts { get; set; }
        public List<A2AMessage> history { get; set; }
        public Map<String, Object> metadata { get; set; }
        public DateTime createdAt { get; set; }
        public DateTime updatedAt { get; set; }
    }
    
    public class A2AArtifact {
        public String artifactId { get; set; }
        public String mimeType { get; set; }
        public List<A2APart> parts { get; set; }
        public Map<String, Object> metadata { get; set; }
    }
    
    public enum A2ATaskStatus {
        SUBMITTED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    public class A2ATaskStatusUpdateEvent {
        public String taskId { get; set; }
        public String contextId { get; set; }
        public A2ATaskStatus status { get; set; }
    }
    
    public class A2ATaskArtifactUpdateEvent {
        public String taskId { get; set; }
        public String contextId { get; set; }
        public A2AArtifact artifact { get; set; }
        public Boolean isAppend { get; set; }
    }
}