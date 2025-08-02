/**
 * Base class for JSON-RPC errors
 * Converted from Java: io.a2a.spec.JSONRPCError
 */
public virtual class A2AJSONRPCError extends Exception implements A2ASerializable {
    
    public Integer code { get; set; }
    public String message { get; set; }
    public Object data { get; set; }
    
    /**
     * Constructor with code and message
     */
    public A2AJSONRPCError(Integer code, String message) {
        this(code, message, null);
    }
    
    /**
     * Constructor with all fields
     */
    public A2AJSONRPCError(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
        setMessage(message);
    }
    
    /**
     * Serialize to Map
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        result.put('code', code);
        result.put('message', message);
        
        if (data != null) {
            result.put('data', data);
        }
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AJSONRPCError deserialize(Map<String, Object> jsonMap) {
        Integer code = (Integer) jsonMap.get('code');
        String message = (String) jsonMap.get('message');
        Object data = jsonMap.get('data');
        
        // Create specific error based on code
        return createError(code, message, data);
    }
    
    /**
     * Create specific error instance based on code
     */
    private static A2AJSONRPCError createError(Integer code, String message, Object data) {
        switch on code {
            when -32700 {
                return new A2AParseError(message);
            }
            when -32600 {
                return new A2AInvalidRequestError(message);
            }
            when -32601 {
                return new A2AMethodNotFoundError(message);
            }
            when -32602 {
                return new A2AInvalidParamsError(message, data);
            }
            when -32603 {
                return new A2AInternalError(message, data);
            }
            when else {
                if (code >= -32099 && code <= -32000) {
                    // Server error
                    return new A2AServerError(code, message, data);
                } else {
                    // Application error
                    return new A2AJSONRPCError(code, message, data);
                }
            }
        }
    }
}

/**
 * Parse error - Invalid JSON was received by the server
 */
public class A2AParseError extends A2AJSONRPCError {
    public A2AParseError(String message) {
        super(-32700, message != null ? message : 'Parse error');
    }
}

/**
 * Invalid Request - The JSON sent is not a valid Request object
 */
public class A2AInvalidRequestError extends A2AJSONRPCError {
    public A2AInvalidRequestError(String message) {
        super(-32600, message != null ? message : 'Invalid Request');
    }
}

/**
 * Method not found - The method does not exist / is not available
 */
public class A2AMethodNotFoundError extends A2AJSONRPCError {
    public A2AMethodNotFoundError(String method) {
        super(-32601, 'Method not found: ' + method);
        this.data = new Map<String, Object>{ 'method' => method };
    }
}

/**
 * Invalid params - Invalid method parameter(s)
 */
public class A2AInvalidParamsError extends A2AJSONRPCError {
    public A2AInvalidParamsError(String message, Object data) {
        super(-32602, message != null ? message : 'Invalid params', data);
    }
}

/**
 * Internal error - Internal JSON-RPC error
 */
public class A2AInternalError extends A2AJSONRPCError {
    public A2AInternalError(String message, Object data) {
        super(-32603, message != null ? message : 'Internal error', data);
    }
}

/**
 * Server error - Reserved for implementation-defined server-errors
 */
public class A2AServerError extends A2AJSONRPCError {
    public A2AServerError(Integer code, String message, Object data) {
        super(code, message, data);
    }
}

/**
 * Task not found error
 */
public class A2ATaskNotFoundError extends A2AJSONRPCError {
    public A2ATaskNotFoundError(String taskId) {
        super(-32001, 'Task not found: ' + taskId);
        this.data = new Map<String, Object>{ 'taskId' => taskId };
    }
}

/**
 * Task not cancelable error
 */
public class A2ATaskNotCancelableError extends A2AJSONRPCError {
    public A2ATaskNotCancelableError(String taskId, String reason) {
        super(-32002, 'Task not cancelable: ' + taskId);
        this.data = new Map<String, Object>{ 
            'taskId' => taskId,
            'reason' => reason 
        };
    }
}

/**
 * Content type not supported error
 */
public class A2AContentTypeNotSupportedError extends A2AJSONRPCError {
    public A2AContentTypeNotSupportedError(String contentType) {
        super(-32003, 'Content type not supported: ' + contentType);
        this.data = new Map<String, Object>{ 'contentType' => contentType };
    }
}

/**
 * Push notification not supported error
 */
public class A2APushNotificationNotSupportedError extends A2AJSONRPCError {
    public A2APushNotificationNotSupportedError() {
        super(-32004, 'Push notifications not supported');
    }
}