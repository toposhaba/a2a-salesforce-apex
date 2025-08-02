/**
 * Controller for A2A Task Monitor Lightning Web Component
 */
public with sharing class A2ATaskMonitorController {
    
    /**
     * Get all tasks
     */
    @AuraEnabled(cacheable=true)
    public static List<TaskWrapper> getTasks() {
        try {
            List<A2A_Task__c> tasks = [
                SELECT Id, Name, Task_ID__c, Context_ID__c, Status__c, 
                       CreatedDate, LastModifiedDate
                FROM A2A_Task__c
                ORDER BY CreatedDate DESC
                LIMIT 100
            ];
            
            List<TaskWrapper> wrappers = new List<TaskWrapper>();
            for (A2A_Task__c task : tasks) {
                TaskWrapper wrapper = new TaskWrapper();
                wrapper.id = task.Id;
                wrapper.name = task.Name;
                wrapper.taskId = task.Task_ID__c;
                wrapper.contextId = task.Context_ID__c;
                wrapper.status = task.Status__c;
                wrapper.createdDate = task.CreatedDate;
                wrapper.lastModifiedDate = task.LastModifiedDate;
                wrappers.add(wrapper);
            }
            
            return wrappers;
        } catch (Exception e) {
            throw new AuraHandledException('Failed to retrieve tasks: ' + e.getMessage());
        }
    }
    
    /**
     * Get task details
     */
    @AuraEnabled
    public static TaskDetailWrapper getTaskDetails(String taskId) {
        try {
            List<A2A_Task__c> tasks = [
                SELECT Id, Name, Task_ID__c, Context_ID__c, Status__c, 
                       Artifacts__c, Message_History__c, Metadata__c,
                       CreatedDate, LastModifiedDate
                FROM A2A_Task__c
                WHERE Task_ID__c = :taskId
                LIMIT 1
            ];
            
            if (tasks.isEmpty()) {
                throw new AuraHandledException('Task not found');
            }
            
            A2A_Task__c task = tasks[0];
            TaskDetailWrapper wrapper = new TaskDetailWrapper();
            wrapper.id = task.Id;
            wrapper.name = task.Name;
            wrapper.taskId = task.Task_ID__c;
            wrapper.contextId = task.Context_ID__c;
            wrapper.status = task.Status__c;
            wrapper.createdAt = task.CreatedDate;
            wrapper.updatedAt = task.LastModifiedDate;
            
            // Parse JSON fields
            if (String.isNotBlank(task.Artifacts__c)) {
                wrapper.artifacts = (List<Object>) JSON.deserializeUntyped(task.Artifacts__c);
            }
            
            if (String.isNotBlank(task.Message_History__c)) {
                wrapper.messageHistory = (List<Object>) JSON.deserializeUntyped(task.Message_History__c);
            }
            
            if (String.isNotBlank(task.Metadata__c)) {
                wrapper.metadata = (Map<String, Object>) JSON.deserializeUntyped(task.Metadata__c);
            }
            
            return wrapper;
        } catch (Exception e) {
            throw new AuraHandledException('Failed to retrieve task details: ' + e.getMessage());
        }
    }
    
    /**
     * Cancel a task
     */
    @AuraEnabled
    public static Boolean cancelTask(String taskId) {
        try {
            // Use the task store to cancel
            A2ATaskStore taskStore = new A2ACustomObjectTaskStore();
            A2ATaskManager.A2ATask task = taskStore.get(taskId);
            
            if (task == null) {
                throw new AuraHandledException('Task not found');
            }
            
            // Check if task can be cancelled
            if (task.status == A2ATaskManager.A2ATaskStatus.COMPLETED || 
                task.status == A2ATaskManager.A2ATaskStatus.FAILED ||
                task.status == A2ATaskManager.A2ATaskStatus.CANCELLED) {
                throw new AuraHandledException('Cannot cancel task with status: ' + task.status.name());
            }
            
            // Update status
            task.status = A2ATaskManager.A2ATaskStatus.CANCELLED;
            taskStore.save(task);
            
            // Publish platform event
            A2APlatformEventPublisher.publishTaskStatusUpdate(taskId, 'CANCELLED');
            
            return true;
        } catch (Exception e) {
            throw new AuraHandledException('Failed to cancel task: ' + e.getMessage());
        }
    }
    
    /**
     * Wrapper class for task list
     */
    public class TaskWrapper {
        @AuraEnabled public String id { get; set; }
        @AuraEnabled public String name { get; set; }
        @AuraEnabled public String taskId { get; set; }
        @AuraEnabled public String contextId { get; set; }
        @AuraEnabled public String status { get; set; }
        @AuraEnabled public DateTime createdDate { get; set; }
        @AuraEnabled public DateTime lastModifiedDate { get; set; }
    }
    
    /**
     * Wrapper class for task details
     */
    public class TaskDetailWrapper {
        @AuraEnabled public String id { get; set; }
        @AuraEnabled public String name { get; set; }
        @AuraEnabled public String taskId { get; set; }
        @AuraEnabled public String contextId { get; set; }
        @AuraEnabled public String status { get; set; }
        @AuraEnabled public DateTime createdAt { get; set; }
        @AuraEnabled public DateTime updatedAt { get; set; }
        @AuraEnabled public List<Object> artifacts { get; set; }
        @AuraEnabled public List<Object> messageHistory { get; set; }
        @AuraEnabled public Map<String, Object> metadata { get; set; }
    }
}