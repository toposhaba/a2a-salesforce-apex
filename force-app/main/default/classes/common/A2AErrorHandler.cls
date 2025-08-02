/**
 * Comprehensive error handling framework for A2A operations
 * Provides consistent error handling, categorization, and recovery strategies
 */
public class A2AErrorHandler {
    
    /**
     * Error categories
     */
    public enum ErrorCategory {
        NETWORK_ERROR,          // Network connectivity issues
        AUTHENTICATION_ERROR,   // Auth failures
        VALIDATION_ERROR,       // Input validation errors
        PROTOCOL_ERROR,         // A2A protocol violations
        AGENT_ERROR,           // Agent-side errors
        SYSTEM_ERROR,          // Salesforce system errors
        CONFIGURATION_ERROR,   // Configuration issues
        LIMIT_ERROR,          // Governor limit errors
        UNKNOWN_ERROR         // Uncategorized errors
    }
    
    /**
     * Error severity levels
     */
    public enum ErrorSeverity {
        LOW,      // Can be ignored
        MEDIUM,   // Should be logged
        HIGH,     // Requires attention
        CRITICAL  // Requires immediate action
    }
    
    /**
     * Recovery strategies
     */
    public enum RecoveryStrategy {
        NONE,               // No recovery possible
        RETRY,              // Retry the operation
        RETRY_WITH_BACKOFF, // Retry with exponential backoff
        FALLBACK,           // Use fallback mechanism
        MANUAL_INTERVENTION // Requires manual fix
    }
    
    /**
     * A2A Error structure
     */
    public class A2AError {
        public String errorCode { get; set; }
        public String message { get; set; }
        public ErrorCategory category { get; set; }
        public ErrorSeverity severity { get; set; }
        public RecoveryStrategy recoveryStrategy { get; set; }
        public Map<String, Object> context { get; set; }
        public Exception originalException { get; set; }
        public DateTime occurredAt { get; set; }
        public String stackTrace { get; set; }
        
        public A2AError() {
            this.occurredAt = DateTime.now();
            this.context = new Map<String, Object>();
        }
    }
    
    /**
     * Handle exception and return structured error
     */
    public static A2AError handleException(Exception e, Map<String, Object> context) {
        A2AError error = new A2AError();
        error.originalException = e;
        error.message = e.getMessage();
        error.stackTrace = e.getStackTraceString();
        error.context = context != null ? context : new Map<String, Object>();
        
        // Categorize the error
        categorizeError(error, e);
        
        // Log the error
        logError(error);
        
        return error;
    }
    
    /**
     * Handle HTTP response errors
     */
    public static A2AError handleHttpError(Integer statusCode, String statusMessage, String body, String endpoint) {
        A2AError error = new A2AError();
        error.errorCode = 'HTTP_' + statusCode;
        error.message = statusMessage;
        error.context.put('statusCode', statusCode);
        error.context.put('endpoint', endpoint);
        error.context.put('responseBody', body);
        
        // Categorize based on status code
        if (statusCode >= 500) {
            error.category = ErrorCategory.AGENT_ERROR;
            error.severity = ErrorSeverity.HIGH;
            error.recoveryStrategy = RecoveryStrategy.RETRY_WITH_BACKOFF;
        } else if (statusCode == 401 || statusCode == 403) {
            error.category = ErrorCategory.AUTHENTICATION_ERROR;
            error.severity = ErrorSeverity.CRITICAL;
            error.recoveryStrategy = RecoveryStrategy.MANUAL_INTERVENTION;
        } else if (statusCode == 429) {
            error.category = ErrorCategory.LIMIT_ERROR;
            error.severity = ErrorSeverity.MEDIUM;
            error.recoveryStrategy = RecoveryStrategy.RETRY_WITH_BACKOFF;
        } else if (statusCode >= 400) {
            error.category = ErrorCategory.VALIDATION_ERROR;
            error.severity = ErrorSeverity.MEDIUM;
            error.recoveryStrategy = RecoveryStrategy.NONE;
        } else if (statusCode == 0) {
            error.category = ErrorCategory.NETWORK_ERROR;
            error.severity = ErrorSeverity.HIGH;
            error.recoveryStrategy = RecoveryStrategy.RETRY;
        }
        
        logError(error);
        return error;
    }
    
    /**
     * Handle JSON-RPC errors
     */
    public static A2AError handleJsonRpcError(Integer code, String message, Object data) {
        A2AError error = new A2AError();
        error.errorCode = 'JSONRPC_' + code;
        error.message = message;
        error.context.put('jsonrpcCode', code);
        error.context.put('jsonrpcData', data);
        
        // Categorize based on JSON-RPC error code
        if (code >= -32099 && code <= -32000) {
            // Server errors
            error.category = ErrorCategory.AGENT_ERROR;
            error.severity = ErrorSeverity.HIGH;
            error.recoveryStrategy = RecoveryStrategy.RETRY;
        } else if (code == -32700) {
            // Parse error
            error.category = ErrorCategory.PROTOCOL_ERROR;
            error.severity = ErrorSeverity.HIGH;
            error.recoveryStrategy = RecoveryStrategy.NONE;
        } else if (code == -32600 || code == -32601 || code == -32602) {
            // Invalid request/method/params
            error.category = ErrorCategory.VALIDATION_ERROR;
            error.severity = ErrorSeverity.MEDIUM;
            error.recoveryStrategy = RecoveryStrategy.NONE;
        } else {
            error.category = ErrorCategory.UNKNOWN_ERROR;
            error.severity = ErrorSeverity.MEDIUM;
            error.recoveryStrategy = RecoveryStrategy.NONE;
        }
        
        logError(error);
        return error;
    }
    
    /**
     * Handle governor limit errors
     */
    public static A2AError handleLimitError(String limitType, Integer used, Integer limit) {
        A2AError error = new A2AError();
        error.errorCode = 'LIMIT_' + limitType.toUpperCase().replace(' ', '_');
        error.message = String.format('Governor limit exceeded: {0} (used {1} of {2})', 
            new List<Object>{ limitType, used, limit });
        error.category = ErrorCategory.LIMIT_ERROR;
        error.severity = ErrorSeverity.CRITICAL;
        error.recoveryStrategy = RecoveryStrategy.NONE;
        error.context.put('limitType', limitType);
        error.context.put('used', used);
        error.context.put('limit', limit);
        
        logError(error);
        return error;
    }
    
    /**
     * Create validation error
     */
    public static A2AError validationError(String field, String message) {
        A2AError error = new A2AError();
        error.errorCode = 'VALIDATION_' + field.toUpperCase();
        error.message = 'Validation error on field ' + field + ': ' + message;
        error.category = ErrorCategory.VALIDATION_ERROR;
        error.severity = ErrorSeverity.LOW;
        error.recoveryStrategy = RecoveryStrategy.NONE;
        error.context.put('field', field);
        error.context.put('validationMessage', message);
        
        logError(error);
        return error;
    }
    
    /**
     * Create configuration error
     */
    public static A2AError configurationError(String component, String message) {
        A2AError error = new A2AError();
        error.errorCode = 'CONFIG_' + component.toUpperCase();
        error.message = 'Configuration error in ' + component + ': ' + message;
        error.category = ErrorCategory.CONFIGURATION_ERROR;
        error.severity = ErrorSeverity.HIGH;
        error.recoveryStrategy = RecoveryStrategy.MANUAL_INTERVENTION;
        error.context.put('component', component);
        
        logError(error);
        return error;
    }
    
    /**
     * Categorize exception into error categories
     */
    private static void categorizeError(A2AError error, Exception e) {
        String exceptionType = e.getTypeName();
        String message = e.getMessage().toLowerCase();
        
        if (exceptionType.contains('CalloutException')) {
            if (message.contains('unauthorized') || message.contains('authentication')) {
                error.category = ErrorCategory.AUTHENTICATION_ERROR;
                error.severity = ErrorSeverity.CRITICAL;
                error.recoveryStrategy = RecoveryStrategy.MANUAL_INTERVENTION;
            } else if (message.contains('timeout') || message.contains('timed out')) {
                error.category = ErrorCategory.NETWORK_ERROR;
                error.severity = ErrorSeverity.HIGH;
                error.recoveryStrategy = RecoveryStrategy.RETRY;
            } else {
                error.category = ErrorCategory.NETWORK_ERROR;
                error.severity = ErrorSeverity.HIGH;
                error.recoveryStrategy = RecoveryStrategy.RETRY_WITH_BACKOFF;
            }
        } else if (exceptionType.contains('LimitException')) {
            error.category = ErrorCategory.LIMIT_ERROR;
            error.severity = ErrorSeverity.CRITICAL;
            error.recoveryStrategy = RecoveryStrategy.NONE;
        } else if (exceptionType.contains('DmlException')) {
            error.category = ErrorCategory.SYSTEM_ERROR;
            error.severity = ErrorSeverity.HIGH;
            error.recoveryStrategy = RecoveryStrategy.RETRY;
        } else if (exceptionType.contains('JSONException')) {
            error.category = ErrorCategory.PROTOCOL_ERROR;
            error.severity = ErrorSeverity.MEDIUM;
            error.recoveryStrategy = RecoveryStrategy.NONE;
        } else if (exceptionType.contains('A2AException')) {
            // Custom A2A exceptions
            if (message.contains('protocol')) {
                error.category = ErrorCategory.PROTOCOL_ERROR;
            } else if (message.contains('agent')) {
                error.category = ErrorCategory.AGENT_ERROR;
            } else {
                error.category = ErrorCategory.UNKNOWN_ERROR;
            }
            error.severity = ErrorSeverity.MEDIUM;
            error.recoveryStrategy = RecoveryStrategy.NONE;
        } else {
            error.category = ErrorCategory.UNKNOWN_ERROR;
            error.severity = ErrorSeverity.MEDIUM;
            error.recoveryStrategy = RecoveryStrategy.NONE;
        }
        
        error.errorCode = exceptionType.replace('Exception', '').toUpperCase();
    }
    
    /**
     * Log error using A2ALogger
     */
    private static void logError(A2AError error) {
        Map<String, Object> logData = new Map<String, Object>{
            'errorCode' => error.errorCode,
            'category' => error.category.name(),
            'severity' => error.severity.name(),
            'recoveryStrategy' => error.recoveryStrategy.name(),
            'context' => error.context
        };
        
        if (error.severity == ErrorSeverity.CRITICAL) {
            A2ALogger.fatal('ERROR_HANDLER', error.message, error.originalException);
        } else if (error.severity == ErrorSeverity.HIGH) {
            A2ALogger.error('ERROR_HANDLER', error.message, error.originalException);
        } else if (error.severity == ErrorSeverity.MEDIUM) {
            A2ALogger.warn('ERROR_HANDLER', error.message, logData);
        } else {
            A2ALogger.info('ERROR_HANDLER', error.message, logData);
        }
    }
    
    /**
     * Check if error is retryable
     */
    public static Boolean isRetryable(A2AError error) {
        return error.recoveryStrategy == RecoveryStrategy.RETRY || 
               error.recoveryStrategy == RecoveryStrategy.RETRY_WITH_BACKOFF;
    }
    
    /**
     * Calculate retry delay for exponential backoff
     */
    public static Integer calculateRetryDelay(Integer attemptNumber) {
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s, 32s (max)
        Integer delay = (Integer) Math.pow(2, Math.min(attemptNumber - 1, 5));
        return delay * 1000; // Convert to milliseconds
    }
    
    /**
     * Convert error to user-friendly message
     */
    public static String getUserMessage(A2AError error) {
        switch on error.category {
            when NETWORK_ERROR {
                return 'Network connection error. Please check your internet connection and try again.';
            }
            when AUTHENTICATION_ERROR {
                return 'Authentication failed. Please check your credentials and permissions.';
            }
            when VALIDATION_ERROR {
                return 'Invalid input: ' + error.message;
            }
            when PROTOCOL_ERROR {
                return 'Communication protocol error. Please contact support.';
            }
            when AGENT_ERROR {
                return 'The agent service is temporarily unavailable. Please try again later.';
            }
            when SYSTEM_ERROR {
                return 'System error occurred. Please try again or contact support.';
            }
            when CONFIGURATION_ERROR {
                return 'Configuration error. Please contact your administrator.';
            }
            when LIMIT_ERROR {
                return 'System limits exceeded. Please try again with smaller data set.';
            }
            when else {
                return 'An unexpected error occurred. Please try again or contact support.';
            }
        }
    }
}