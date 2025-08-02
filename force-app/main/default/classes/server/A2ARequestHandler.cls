/**
 * Interface for handling A2A server requests.
 * Converted from Java: io.a2a.server.requesthandlers.RequestHandler
 */
public interface A2ARequestHandler {
    /**
     * Handle get task request
     */
    A2ATask onGetTask(A2ATaskQueryParams params, A2AServerCallContext context);
    
    /**
     * Handle cancel task request
     */
    A2ATask onCancelTask(A2ATaskIdParams params, A2AServerCallContext context);
    
    /**
     * Handle message send request
     */
    A2AEventKind onMessageSend(A2AMessageSendParams params, A2AServerCallContext context);
    
    /**
     * Handle set task push notification config
     */
    A2ATaskPushNotificationConfig onSetTaskPushNotificationConfig(
        A2ATaskPushNotificationConfig params, 
        A2AServerCallContext context
    );
    
    /**
     * Handle get task push notification config
     */
    A2ATaskPushNotificationConfig onGetTaskPushNotificationConfig(
        A2AGetTaskPushNotificationConfigParams params,
        A2AServerCallContext context
    );
    
    /**
     * Handle list task push notification configs
     */
    List<A2ATaskPushNotificationConfig> onListTaskPushNotificationConfig(
        A2AListTaskPushNotificationConfigParams params,
        A2AServerCallContext context
    );
    
    /**
     * Handle delete task push notification config
     */
    void onDeleteTaskPushNotificationConfig(
        A2ADeleteTaskPushNotificationConfigParams params,
        A2AServerCallContext context
    );
    
    /**
     * Helper classes for request parameters
     */
    class A2ATaskQueryParams {
        public String id { get; set; }
        
        public A2ATaskQueryParams() {}
        
        public A2ATaskQueryParams(String id) {
            this.id = id;
        }
    }
    
    class A2ATaskIdParams {
        public String id { get; set; }
        
        public A2ATaskIdParams() {}
        
        public A2ATaskIdParams(String id) {
            this.id = id;
        }
    }
    
    class A2AMessageSendParams {
        public A2AMessage message { get; set; }
        public String pushNotificationUrl { get; set; }
        
        public A2AMessageSendParams() {}
        
        public A2AMessageSendParams(A2AMessage message) {
            this.message = message;
        }
    }
    
    class A2AEventKind {
        public String type { get; set; }
        public Map<String, Object> data { get; set; }
    }
    
    class A2ATask {
        public String id { get; set; }
        public String status { get; set; }
        public Map<String, Object> artifacts { get; set; }
        public Map<String, Object> metadata { get; set; }
        public DateTime createdAt { get; set; }
        public DateTime updatedAt { get; set; }
    }
    
    class A2ATaskPushNotificationConfig {
        public String taskId { get; set; }
        public String url { get; set; }
        public Map<String, String> headers { get; set; }
    }
    
    class A2AGetTaskPushNotificationConfigParams {
        public String taskId { get; set; }
        public String url { get; set; }
    }
    
    class A2AListTaskPushNotificationConfigParams {
        public String taskId { get; set; }
        public Integer limitValue { get; set; }
        public Integer offset { get; set; }
    }
    
    class A2ADeleteTaskPushNotificationConfigParams {
        public String taskId { get; set; }
        public String url { get; set; }
    }
}