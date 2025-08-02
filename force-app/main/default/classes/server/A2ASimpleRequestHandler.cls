/**
 * Simple implementation of A2ARequestHandler for demonstration purposes.
 * This should be extended/customized for actual agent logic.
 */
public class A2ASimpleRequestHandler implements A2ARequestHandler {
    private A2ATaskStore taskStore;
    
    /**
     * Constructor
     */
    public A2ASimpleRequestHandler() {
        // Use in-memory task store for simplicity
        this.taskStore = new A2AInMemoryTaskStore();
    }
    
    /**
     * Constructor with custom task store
     */
    public A2ASimpleRequestHandler(A2ATaskStore taskStore) {
        this.taskStore = taskStore;
    }
    
    /**
     * Handle get task request
     */
    public A2ATask onGetTask(A2ATaskQueryParams params, A2AServerCallContext context) {
        if (params == null || String.isBlank(params.id)) {
            throw new A2AJSONRPCHandler.A2AJSONRPCError(-32602, 'Invalid params: task ID is required');
        }
        
        A2ATaskManager.A2ATask internalTask = taskStore.get(params.id);
        if (internalTask == null) {
            return null;
        }
        
        return convertToExternalTask(internalTask);
    }
    
    /**
     * Handle cancel task request
     */
    public A2ATask onCancelTask(A2ATaskIdParams params, A2AServerCallContext context) {
        if (params == null || String.isBlank(params.id)) {
            throw new A2AJSONRPCHandler.A2AJSONRPCError(-32602, 'Invalid params: task ID is required');
        }
        
        A2ATaskManager taskManager = new A2ATaskManager(params.id, null, taskStore, null);
        Boolean cancelled = taskManager.cancelTask();
        
        if (!cancelled) {
            return null;
        }
        
        A2ATaskManager.A2ATask internalTask = taskManager.getTask();
        return convertToExternalTask(internalTask);
    }
    
    /**
     * Handle message send request
     */
    public A2AEventKind onMessageSend(A2AMessageSendParams params, A2AServerCallContext context) {
        if (params == null || params.message == null) {
            throw new A2AJSONRPCHandler.A2AJSONRPCError(-32602, 'Invalid params: message is required');
        }
        
        // Generate task ID
        String taskId = generateTaskId();
        String contextId = params.message.contextId != null ? params.message.contextId : generateContextId();
        
        // Create task manager
        A2ATaskManager taskManager = new A2ATaskManager(taskId, contextId, taskStore, params.message);
        
        // Create initial task
        A2ATaskManager.A2ATask task = new A2ATaskManager.A2ATask();
        task.id = taskId;
        task.contextId = contextId;
        task.status = A2ATaskManager.A2ATaskStatus.SUBMITTED;
        task.createdAt = DateTime.now();
        task.updatedAt = DateTime.now();
        
        // Save task
        taskManager.saveTaskEvent(task);
        
        // Process message asynchronously (in real implementation)
        // For now, just mark as running
        A2ATaskManager.A2ATaskStatusUpdateEvent statusUpdate = new A2ATaskManager.A2ATaskStatusUpdateEvent();
        statusUpdate.taskId = taskId;
        statusUpdate.contextId = contextId;
        statusUpdate.status = A2ATaskManager.A2ATaskStatus.RUNNING;
        taskManager.saveTaskStatusUpdateEvent(statusUpdate);
        
        // Return task event
        A2AEventKind eventKind = new A2AEventKind();
        eventKind.type = 'task';
        eventKind.data = new Map<String, Object>{
            'id' => taskId,
            'status' => 'RUNNING'
        };
        
        return eventKind;
    }
    
    /**
     * Handle set task push notification config
     */
    public A2ATaskPushNotificationConfig onSetTaskPushNotificationConfig(
        A2ATaskPushNotificationConfig params, 
        A2AServerCallContext context
    ) {
        // Store notification config (would be persisted in real implementation)
        return params;
    }
    
    /**
     * Handle get task push notification config
     */
    public A2ATaskPushNotificationConfig onGetTaskPushNotificationConfig(
        A2AGetTaskPushNotificationConfigParams params,
        A2AServerCallContext context
    ) {
        // Return empty config for now
        return null;
    }
    
    /**
     * Handle list task push notification configs
     */
    public List<A2ATaskPushNotificationConfig> onListTaskPushNotificationConfig(
        A2AListTaskPushNotificationConfigParams params,
        A2AServerCallContext context
    ) {
        // Return empty list for now
        return new List<A2ATaskPushNotificationConfig>();
    }
    
    /**
     * Handle delete task push notification config
     */
    public void onDeleteTaskPushNotificationConfig(
        A2ADeleteTaskPushNotificationConfigParams params,
        A2AServerCallContext context
    ) {
        // No-op for now
    }
    
    /**
     * Helper methods
     */
    private String generateTaskId() {
        return 'task_' + String.valueOf(DateTime.now().getTime()) + '_' + String.valueOf(Math.random()).substring(2, 8);
    }
    
    private String generateContextId() {
        return 'ctx_' + String.valueOf(DateTime.now().getTime()) + '_' + String.valueOf(Math.random()).substring(2, 8);
    }
    
    private A2ATask convertToExternalTask(A2ATaskManager.A2ATask internalTask) {
        A2ATask task = new A2ATask();
        task.id = internalTask.id;
        task.status = internalTask.status.name();
        task.createdAt = internalTask.createdAt;
        task.updatedAt = internalTask.updatedAt;
        task.metadata = internalTask.metadata;
        
        // Convert artifacts
        if (internalTask.artifacts != null) {
            task.artifacts = new Map<String, Object>();
            for (A2ATaskManager.A2AArtifact artifact : internalTask.artifacts) {
                task.artifacts.put(artifact.artifactId, artifact);
            }
        }
        
        return task;
    }
}