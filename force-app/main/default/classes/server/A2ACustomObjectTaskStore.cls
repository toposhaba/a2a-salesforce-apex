/**
 * Task store implementation using A2A_Task__c custom object for persistence.
 * This provides durable storage for A2A tasks in Salesforce.
 */
public class A2ACustomObjectTaskStore implements A2ATaskStore {
    
    /**
     * Save a task
     */
    public A2ATaskManager.A2ATask save(A2ATaskManager.A2ATask task) {
        if (task == null || String.isBlank(task.id)) {
            throw new A2AException('Task or task ID cannot be null');
        }
        
        A2A_Task__c taskRecord = convertToCustomObject(task);
        
        // Check if task exists
        List<A2A_Task__c> existingTasks = [
            SELECT Id FROM A2A_Task__c 
            WHERE Task_ID__c = :task.id 
            LIMIT 1
        ];
        
        if (!existingTasks.isEmpty()) {
            taskRecord.Id = existingTasks[0].Id;
            update taskRecord;
        } else {
            insert taskRecord;
        }
        
        // Publish platform event for real-time updates
        try {
            A2APlatformEventPublisher.publishTaskEvent(
                task.id, 
                'task_update',
                new Map<String, Object>{
                    'taskId' => task.id,
                    'status' => task.status.name(),
                    'timestamp' => DateTime.now().format()
                }
            );
        } catch (Exception e) {
            // Log but don't fail the save operation
            System.debug(LoggingLevel.WARN, 'Failed to publish platform event: ' + e.getMessage());
        }
        
        return task;
    }
    
    /**
     * Get a task by ID
     */
    public A2ATaskManager.A2ATask get(String taskId) {
        if (String.isBlank(taskId)) {
            return null;
        }
        
        List<A2A_Task__c> tasks = [
            SELECT Id, Task_ID__c, Context_ID__c, Status__c, 
                   Artifacts__c, Message_History__c, Metadata__c,
                   CreatedDate, LastModifiedDate
            FROM A2A_Task__c 
            WHERE Task_ID__c = :taskId 
            LIMIT 1
        ];
        
        if (tasks.isEmpty()) {
            return null;
        }
        
        return convertFromCustomObject(tasks[0]);
    }
    
    /**
     * Delete a task by ID
     */
    public Boolean remove(String taskId) {
        if (String.isBlank(taskId)) {
            return false;
        }
        
        List<A2A_Task__c> tasks = [
            SELECT Id FROM A2A_Task__c 
            WHERE Task_ID__c = :taskId 
            LIMIT 1
        ];
        
        if (tasks.isEmpty()) {
            return false;
        }
        
        delete tasks;
        return true;
    }
    
    /**
     * Check if a task exists
     */
    public Boolean exists(String taskId) {
        if (String.isBlank(taskId)) {
            return false;
        }
        
        Integer count = [
            SELECT COUNT() FROM A2A_Task__c 
            WHERE Task_ID__c = :taskId 
            LIMIT 1
        ];
        
        return count > 0;
    }
    
    /**
     * Get all tasks
     */
    public List<A2ATaskManager.A2ATask> getAll() {
        List<A2A_Task__c> taskRecords = [
            SELECT Id, Task_ID__c, Context_ID__c, Status__c, 
                   Artifacts__c, Message_History__c, Metadata__c,
                   CreatedDate, LastModifiedDate
            FROM A2A_Task__c 
            ORDER BY CreatedDate DESC
            LIMIT 1000
        ];
        
        List<A2ATaskManager.A2ATask> tasks = new List<A2ATaskManager.A2ATask>();
        for (A2A_Task__c record : taskRecords) {
            tasks.add(convertFromCustomObject(record));
        }
        
        return tasks;
    }
    
    /**
     * Get tasks by status
     */
    public List<A2ATaskManager.A2ATask> getByStatus(A2ATaskManager.A2ATaskStatus status) {
        String statusValue = status.name();
        
        List<A2A_Task__c> taskRecords = [
            SELECT Id, Task_ID__c, Context_ID__c, Status__c, 
                   Artifacts__c, Message_History__c, Metadata__c,
                   CreatedDate, LastModifiedDate
            FROM A2A_Task__c 
            WHERE Status__c = :statusValue
            ORDER BY CreatedDate DESC
            LIMIT 1000
        ];
        
        List<A2ATaskManager.A2ATask> tasks = new List<A2ATaskManager.A2ATask>();
        for (A2A_Task__c record : taskRecords) {
            tasks.add(convertFromCustomObject(record));
        }
        
        return tasks;
    }
    
    /**
     * Convert internal task to custom object
     */
    private A2A_Task__c convertToCustomObject(A2ATaskManager.A2ATask task) {
        A2A_Task__c record = new A2A_Task__c();
        record.Task_ID__c = task.id;
        record.Context_ID__c = task.contextId;
        record.Status__c = task.status.name();
        
        // Serialize artifacts
        if (task.artifacts != null && !task.artifacts.isEmpty()) {
            List<Map<String, Object>> artifactsData = new List<Map<String, Object>>();
            for (A2ATaskManager.A2AArtifact artifact : task.artifacts) {
                artifactsData.add(serializeArtifact(artifact));
            }
            record.Artifacts__c = JSON.serialize(artifactsData);
        }
        
        // Serialize message history
        if (task.history != null && !task.history.isEmpty()) {
            List<Map<String, Object>> historyData = new List<Map<String, Object>>();
            for (A2AMessage message : task.history) {
                historyData.add(message.serialize());
            }
            record.Message_History__c = JSON.serialize(historyData);
        }
        
        // Serialize metadata
        if (task.metadata != null && !task.metadata.isEmpty()) {
            record.Metadata__c = JSON.serialize(task.metadata);
        }
        
        return record;
    }
    
    /**
     * Convert custom object to internal task
     */
    private A2ATaskManager.A2ATask convertFromCustomObject(A2A_Task__c record) {
        A2ATaskManager.A2ATask task = new A2ATaskManager.A2ATask();
        task.id = record.Task_ID__c;
        task.contextId = record.Context_ID__c;
        task.status = A2ATaskManager.A2ATaskStatus.valueOf(record.Status__c);
        task.createdAt = record.CreatedDate;
        task.updatedAt = record.LastModifiedDate;
        
        // Deserialize artifacts
        if (String.isNotBlank(record.Artifacts__c)) {
            task.artifacts = new List<A2ATaskManager.A2AArtifact>();
            List<Object> artifactsData = (List<Object>) JSON.deserializeUntyped(record.Artifacts__c);
            for (Object artifactObj : artifactsData) {
                Map<String, Object> artifactMap = (Map<String, Object>) artifactObj;
                task.artifacts.add(deserializeArtifact(artifactMap));
            }
        }
        
        // Deserialize message history
        if (String.isNotBlank(record.Message_History__c)) {
            task.history = new List<A2AMessage>();
            List<Object> historyData = (List<Object>) JSON.deserializeUntyped(record.Message_History__c);
            for (Object messageObj : historyData) {
                Map<String, Object> messageMap = (Map<String, Object>) messageObj;
                task.history.add(A2AMessage.deserialize(messageMap));
            }
        }
        
        // Deserialize metadata
        if (String.isNotBlank(record.Metadata__c)) {
            task.metadata = (Map<String, Object>) JSON.deserializeUntyped(record.Metadata__c);
        }
        
        return task;
    }
    
    /**
     * Serialize artifact
     */
    private Map<String, Object> serializeArtifact(A2ATaskManager.A2AArtifact artifact) {
        Map<String, Object> result = new Map<String, Object>();
        result.put('artifactId', artifact.artifactId);
        result.put('mimeType', artifact.mimeType);
        
        if (artifact.parts != null) {
            List<Map<String, Object>> partsData = new List<Map<String, Object>>();
            for (A2APart part : artifact.parts) {
                partsData.add(part.serialize());
            }
            result.put('parts', partsData);
        }
        
        if (artifact.metadata != null) {
            result.put('metadata', artifact.metadata);
        }
        
        return result;
    }
    
    /**
     * Deserialize artifact
     */
    private A2ATaskManager.A2AArtifact deserializeArtifact(Map<String, Object> artifactMap) {
        A2ATaskManager.A2AArtifact artifact = new A2ATaskManager.A2AArtifact();
        artifact.artifactId = (String) artifactMap.get('artifactId');
        artifact.mimeType = (String) artifactMap.get('mimeType');
        artifact.metadata = (Map<String, Object>) artifactMap.get('metadata');
        
        // Note: Deserializing parts would require more complex logic
        // based on part types, which is omitted for brevity
        
        return artifact;
    }
}