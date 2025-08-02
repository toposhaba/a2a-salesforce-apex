/**
 * Publishes A2A events to Platform Events for streaming and notifications.
 * This enables real-time updates and push notifications in Salesforce.
 */
public class A2APlatformEventPublisher {
    
    /**
     * Publish a task status update event
     */
    public static void publishTaskStatusUpdate(String taskId, String status) {
        A2A_Task_Event__e event = new A2A_Task_Event__e();
        event.Task_Id__c = taskId;
        event.Event_Type__c = 'status_update';
        event.Status__c = status;
        event.Payload__c = JSON.serialize(new Map<String, Object>{
            'taskId' => taskId,
            'status' => status,
            'timestamp' => DateTime.now().format()
        });
        
        publishEvent(event);
    }
    
    /**
     * Publish a task artifact update event
     */
    public static void publishTaskArtifactUpdate(String taskId, Map<String, Object> artifact) {
        A2A_Task_Event__e event = new A2A_Task_Event__e();
        event.Task_Id__c = taskId;
        event.Event_Type__c = 'artifact_update';
        event.Payload__c = JSON.serialize(new Map<String, Object>{
            'taskId' => taskId,
            'artifact' => artifact,
            'timestamp' => DateTime.now().format()
        });
        
        publishEvent(event);
    }
    
    /**
     * Publish a task message event
     */
    public static void publishTaskMessage(String taskId, Map<String, Object> message) {
        A2A_Task_Event__e event = new A2A_Task_Event__e();
        event.Task_Id__c = taskId;
        event.Event_Type__c = 'message';
        event.Payload__c = JSON.serialize(new Map<String, Object>{
            'taskId' => taskId,
            'message' => message,
            'timestamp' => DateTime.now().format()
        });
        
        publishEvent(event);
    }
    
    /**
     * Publish a generic task event
     */
    public static void publishTaskEvent(String taskId, String eventType, Map<String, Object> data) {
        A2A_Task_Event__e event = new A2A_Task_Event__e();
        event.Task_Id__c = taskId;
        event.Event_Type__c = eventType;
        event.Payload__c = JSON.serialize(data);
        
        publishEvent(event);
    }
    
    /**
     * Publish multiple events
     */
    public static void publishEvents(List<A2A_Task_Event__e> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        List<Database.SaveResult> results = EventBus.publish(events);
        
        // Check for errors
        for (Integer i = 0; i < results.size(); i++) {
            Database.SaveResult result = results[i];
            if (!result.isSuccess()) {
                System.debug(LoggingLevel.ERROR, 
                    'Failed to publish event ' + i + ': ' + result.getErrors()[0].getMessage());
            }
        }
    }
    
    /**
     * Private helper to publish a single event
     */
    private static void publishEvent(A2A_Task_Event__e event) {
        List<Database.SaveResult> results = EventBus.publish(event);
        
        if (!results[0].isSuccess()) {
            System.debug(LoggingLevel.ERROR, 
                'Failed to publish event: ' + results[0].getErrors()[0].getMessage());
            throw new A2AException('Failed to publish platform event: ' + results[0].getErrors()[0].getMessage());
        }
    }
    
    /**
     * Helper method to create event from task update
     */
    public static A2A_Task_Event__e createTaskUpdateEvent(A2ATaskManager.A2ATask task) {
        A2A_Task_Event__e event = new A2A_Task_Event__e();
        event.Task_Id__c = task.id;
        event.Event_Type__c = 'task_update';
        event.Status__c = task.status.name();
        
        Map<String, Object> payload = new Map<String, Object>{
            'taskId' => task.id,
            'contextId' => task.contextId,
            'status' => task.status.name(),
            'createdAt' => task.createdAt,
            'updatedAt' => task.updatedAt
        };
        
        if (task.metadata != null) {
            payload.put('metadata', task.metadata);
        }
        
        event.Payload__c = JSON.serialize(payload);
        
        return event;
    }
}