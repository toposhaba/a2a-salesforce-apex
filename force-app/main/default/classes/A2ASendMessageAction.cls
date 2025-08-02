/**
 * Invocable action to send messages to A2A agents from Flow
 */
public class A2ASendMessageAction {
    
    @InvocableMethod(label='Send A2A Message' description='Send a message to an A2A agent and receive a task ID')
    public static List<SendMessageResult> sendMessage(List<SendMessageRequest> requests) {
        List<SendMessageResult> results = new List<SendMessageResult>();
        
        for (SendMessageRequest request : requests) {
            SendMessageResult result = new SendMessageResult();
            
            try {
                // Create client
                A2AClient client = new A2AClient(request.agentUrl);
                
                // Build message
                A2AMessage.Builder messageBuilder = new A2AMessage.Builder()
                    .withRole(request.role != null ? request.role : 'user');
                
                if (String.isNotBlank(request.messageText)) {
                    messageBuilder.withTextPart(request.messageText);
                }
                
                if (request.dataJson != null) {
                    Map<String, Object> data = (Map<String, Object>) JSON.deserializeUntyped(request.dataJson);
                    messageBuilder.withDataPart(data);
                }
                
                A2AMessage message = messageBuilder.build();
                
                // Send message
                A2AClient.A2AMessageSendParams params = new A2AClient.A2AMessageSendParams(message);
                A2AClient.A2ASendMessageResponse response = client.sendMessage(params);
                
                if (response.error != null) {
                    result.success = false;
                    result.errorMessage = response.error.message;
                } else if (response.result != null) {
                    result.success = true;
                    Map<String, Object> resultData = (Map<String, Object>) response.result.data;
                    result.taskId = (String) resultData.get('id');
                    result.taskStatus = (String) resultData.get('status');
                }
                
            } catch (Exception e) {
                result.success = false;
                result.errorMessage = e.getMessage();
            }
            
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Input class for Send Message action
     */
    public class SendMessageRequest {
        @InvocableVariable(label='Agent URL' description='URL of the A2A agent' required=true)
        public String agentUrl;
        
        @InvocableVariable(label='Message Text' description='Text content of the message')
        public String messageText;
        
        @InvocableVariable(label='Data JSON' description='JSON data to include in the message')
        public String dataJson;
        
        @InvocableVariable(label='Role' description='Message role (user/assistant/system)')
        public String role;
    }
    
    /**
     * Output class for Send Message action
     */
    public class SendMessageResult {
        @InvocableVariable(label='Success' description='Whether the operation succeeded')
        public Boolean success;
        
        @InvocableVariable(label='Task ID' description='ID of the created task')
        public String taskId;
        
        @InvocableVariable(label='Task Status' description='Initial status of the task')
        public String taskStatus;
        
        @InvocableVariable(label='Error Message' description='Error message if operation failed')
        public String errorMessage;
    }
}