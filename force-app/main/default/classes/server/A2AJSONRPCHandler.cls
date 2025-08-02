/**
 * Handles JSON-RPC requests for the A2A server.
 * Converted from Java: io.a2a.server.requesthandlers.JSONRPCHandler
 */
public class A2AJSONRPCHandler {
    private A2AAgentCard agentCard;
    private A2ARequestHandler requestHandler;
    
    /**
     * Constructor
     */
    public A2AJSONRPCHandler(A2AAgentCard agentCard, A2ARequestHandler requestHandler) {
        this.agentCard = agentCard;
        this.requestHandler = requestHandler;
    }
    
    /**
     * Handle message send request
     */
    public A2ASendMessageResponse onMessageSend(A2ASendMessageRequest request, A2AServerCallContext context) {
        try {
            A2ARequestHandler.A2AEventKind taskOrMessage = requestHandler.onMessageSend(request.params, context);
            return new A2ASendMessageResponse(request.id, taskOrMessage, null);
        } catch (A2AJSONRPCError e) {
            return new A2ASendMessageResponse(request.id, null, e);
        } catch (Exception e) {
            return new A2ASendMessageResponse(request.id, null, new A2AInternalError(e.getMessage()));
        }
    }
    
    /**
     * Handle get task request
     */
    public A2AGetTaskResponse onGetTask(A2AGetTaskRequest request, A2AServerCallContext context) {
        try {
            A2ARequestHandler.A2ATask task = requestHandler.onGetTask(request.params, context);
            if (task != null) {
                return new A2AGetTaskResponse(request.id, task, null);
            }
            return new A2AGetTaskResponse(request.id, null, new A2ATaskNotFoundError());
        } catch (A2AJSONRPCError e) {
            return new A2AGetTaskResponse(request.id, null, e);
        } catch (Exception e) {
            return new A2AGetTaskResponse(request.id, null, new A2AInternalError(e.getMessage()));
        }
    }
    
    /**
     * Handle cancel task request
     */
    public A2ACancelTaskResponse onCancelTask(A2ACancelTaskRequest request, A2AServerCallContext context) {
        try {
            A2ARequestHandler.A2ATask task = requestHandler.onCancelTask(request.params, context);
            if (task != null) {
                return new A2ACancelTaskResponse(request.id, task, null);
            }
            return new A2ACancelTaskResponse(request.id, null, new A2ATaskNotFoundError());
        } catch (A2AJSONRPCError e) {
            return new A2ACancelTaskResponse(request.id, null, e);
        } catch (Exception e) {
            return new A2ACancelTaskResponse(request.id, null, new A2AInternalError(e.getMessage()));
        }
    }
    
    /**
     * Handle set task push notification config request
     */
    public A2ASetTaskPushNotificationConfigResponse onSetTaskPushNotificationConfig(
        A2ASetTaskPushNotificationConfigRequest request, 
        A2AServerCallContext context
    ) {
        try {
            if (!agentCard.capabilities.pushNotifications) {
                return new A2ASetTaskPushNotificationConfigResponse(
                    request.id, 
                    null, 
                    new A2APushNotificationNotSupportedError()
                );
            }
            
            A2ARequestHandler.A2ATaskPushNotificationConfig config = 
                requestHandler.onSetTaskPushNotificationConfig(request.params, context);
            return new A2ASetTaskPushNotificationConfigResponse(request.id, config, null);
        } catch (A2AJSONRPCError e) {
            return new A2ASetTaskPushNotificationConfigResponse(request.id, null, e);
        } catch (Exception e) {
            return new A2ASetTaskPushNotificationConfigResponse(request.id, null, new A2AInternalError(e.getMessage()));
        }
    }
    
    /**
     * Process a generic JSON-RPC request
     */
    public String processRequest(String jsonRequest, A2AServerCallContext context) {
        try {
            Map<String, Object> request = (Map<String, Object>) JSON.deserializeUntyped(jsonRequest);
            String method = (String) request.get('method');
            String requestId = (String) request.get('id');
            
            Object response;
            
            switch on method {
                when 'a2a.sendMessage' {
                    A2ASendMessageRequest req = (A2ASendMessageRequest) JSON.deserialize(jsonRequest, A2ASendMessageRequest.class);
                    response = onMessageSend(req, context);
                }
                when 'a2a.getTask' {
                    A2AGetTaskRequest req = (A2AGetTaskRequest) JSON.deserialize(jsonRequest, A2AGetTaskRequest.class);
                    response = onGetTask(req, context);
                }
                when 'a2a.cancelTask' {
                    A2ACancelTaskRequest req = (A2ACancelTaskRequest) JSON.deserialize(jsonRequest, A2ACancelTaskRequest.class);
                    response = onCancelTask(req, context);
                }
                when 'a2a.setTaskPushNotificationConfig' {
                    A2ASetTaskPushNotificationConfigRequest req = (A2ASetTaskPushNotificationConfigRequest) JSON.deserialize(jsonRequest, A2ASetTaskPushNotificationConfigRequest.class);
                    response = onSetTaskPushNotificationConfig(req, context);
                }
                when else {
                    response = new A2AGenericResponse(requestId, null, new A2AMethodNotFoundError(method));
                }
            }
            
            return JSON.serialize(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new Map<String, Object>{
                'jsonrpc' => '2.0',
                'id' => null,
                'error' => new Map<String, Object>{
                    'code' => -32700,
                    'message' => 'Parse error',
                    'data' => e.getMessage()
                }
            };
            return JSON.serialize(errorResponse);
        }
    }
    
    /**
     * Request/Response classes
     */
    public class A2ASendMessageRequest {
        public String jsonrpc;
        public String method;
        public String id;
        public A2ARequestHandler.A2AMessageSendParams params;
    }
    
    public class A2ASendMessageResponse {
        public String jsonrpc = '2.0';
        public String id;
        public A2ARequestHandler.A2AEventKind result;
        public A2AJSONRPCError error;
        
        public A2ASendMessageResponse(String id, A2ARequestHandler.A2AEventKind result, A2AJSONRPCError error) {
            this.id = id;
            this.result = result;
            this.error = error;
        }
    }
    
    public class A2AGetTaskRequest {
        public String jsonrpc;
        public String method;
        public String id;
        public A2ARequestHandler.A2ATaskQueryParams params;
    }
    
    public class A2AGetTaskResponse {
        public String jsonrpc = '2.0';
        public String id;
        public A2ARequestHandler.A2ATask result;
        public A2AJSONRPCError error;
        
        public A2AGetTaskResponse(String id, A2ARequestHandler.A2ATask result, A2AJSONRPCError error) {
            this.id = id;
            this.result = result;
            this.error = error;
        }
    }
    
    public class A2ACancelTaskRequest {
        public String jsonrpc;
        public String method;
        public String id;
        public A2ARequestHandler.A2ATaskIdParams params;
    }
    
    public class A2ACancelTaskResponse {
        public String jsonrpc = '2.0';
        public String id;
        public A2ARequestHandler.A2ATask result;
        public A2AJSONRPCError error;
        
        public A2ACancelTaskResponse(String id, A2ARequestHandler.A2ATask result, A2AJSONRPCError error) {
            this.id = id;
            this.result = result;
            this.error = error;
        }
    }
    
    public class A2ASetTaskPushNotificationConfigRequest {
        public String jsonrpc;
        public String method;
        public String id;
        public A2ARequestHandler.A2ATaskPushNotificationConfig params;
    }
    
    public class A2ASetTaskPushNotificationConfigResponse {
        public String jsonrpc = '2.0';
        public String id;
        public A2ARequestHandler.A2ATaskPushNotificationConfig result;
        public A2AJSONRPCError error;
        
        public A2ASetTaskPushNotificationConfigResponse(String id, A2ARequestHandler.A2ATaskPushNotificationConfig result, A2AJSONRPCError error) {
            this.id = id;
            this.result = result;
            this.error = error;
        }
    }
    
    public class A2AGenericResponse {
        public String jsonrpc = '2.0';
        public String id;
        public Object result;
        public A2AJSONRPCError error;
        
        public A2AGenericResponse(String id, Object result, A2AJSONRPCError error) {
            this.id = id;
            this.result = result;
            this.error = error;
        }
    }
    
    /**
     * JSON-RPC Error classes
     */
    public virtual class A2AJSONRPCError {
        public Integer code;
        public String message;
        public Object data;
        
        public A2AJSONRPCError(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public A2AJSONRPCError(Integer code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }
    }
    
    public class A2AInternalError extends A2AJSONRPCError {
        public A2AInternalError(String message) {
            super(-32603, 'Internal error: ' + message);
        }
    }
    
    public class A2AMethodNotFoundError extends A2AJSONRPCError {
        public A2AMethodNotFoundError(String method) {
            super(-32601, 'Method not found: ' + method);
        }
    }
    
    public class A2ATaskNotFoundError extends A2AJSONRPCError {
        public A2ATaskNotFoundError() {
            super(-32001, 'Task not found');
        }
    }
    
    public class A2APushNotificationNotSupportedError extends A2AJSONRPCError {
        public A2APushNotificationNotSupportedError() {
            super(-32002, 'Push notifications not supported');
        }
    }
}