/**
 * Base exception class for A2A SDK
 * Provides structured error handling with error codes and types
 */
public class A2AException extends Exception {
    private Integer errorCode;
    private String errorType;
    private Map<String, Object> errorData;
    
    public A2AException(String message) {
        this(message, null, null);
    }
    
    public A2AException(String message, Integer code) {
        this(message, code, null);
    }
    
    public A2AException(String message, Integer code, String type) {
        this.setMessage(message);
        this.errorCode = code;
        this.errorType = type;
    }
    
    public A2AException(String message, Integer code, String type, Map<String, Object> data) {
        this(message, code, type);
        this.errorData = data;
    }
    
    public Integer getErrorCode() {
        return errorCode;
    }
    
    public String getErrorType() {
        return errorType;
    }
    
    public Map<String, Object> getErrorData() {
        return errorData;
    }
    
    /**
     * Convert exception to JSON-RPC error format
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> result = new Map<String, Object>{
            'message' => getMessage()
        };
        
        if (errorCode != null) {
            result.put('code', errorCode);
        }
        
        if (errorType != null) {
            result.put('type', errorType);
        }
        
        if (errorData != null && !errorData.isEmpty()) {
            result.put('data', errorData);
        }
        
        return result;
    }
}