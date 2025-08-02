/**
 * Logging framework for A2A operations
 * Provides structured logging with different levels and persistence options
 */
public class A2ALogger {
    
    // Log levels
    public enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }
    
    // Log entry structure
    public class LogEntry {
        public DateTime timestamp { get; set; }
        public LogLevel level { get; set; }
        public String category { get; set; }
        public String message { get; set; }
        public String className { get; set; }
        public String methodName { get; set; }
        public String taskId { get; set; }
        public String contextId { get; set; }
        public Map<String, Object> additionalData { get; set; }
        public String stackTrace { get; set; }
        
        public LogEntry() {
            this.timestamp = DateTime.now();
            this.additionalData = new Map<String, Object>();
        }
    }
    
    // Static collection of log entries for the current transaction
    private static List<LogEntry> logEntries = new List<LogEntry>();
    private static LogLevel minimumLevel = LogLevel.INFO;
    private static Boolean consoleLoggingEnabled = true;
    private static Boolean persistenceEnabled = true;
    
    /**
     * Configure logging settings
     */
    public static void configure(LogLevel minLevel, Boolean enableConsole, Boolean enablePersistence) {
        minimumLevel = minLevel;
        consoleLoggingEnabled = enableConsole;
        persistenceEnabled = enablePersistence;
    }
    
    /**
     * Debug level logging
     */
    public static void debug(String message) {
        log(LogLevel.DEBUG, null, message, null);
    }
    
    public static void debug(String category, String message) {
        log(LogLevel.DEBUG, category, message, null);
    }
    
    public static void debug(String category, String message, Map<String, Object> data) {
        log(LogLevel.DEBUG, category, message, data);
    }
    
    /**
     * Info level logging
     */
    public static void info(String message) {
        log(LogLevel.INFO, null, message, null);
    }
    
    public static void info(String category, String message) {
        log(LogLevel.INFO, category, message, null);
    }
    
    public static void info(String category, String message, Map<String, Object> data) {
        log(LogLevel.INFO, category, message, data);
    }
    
    /**
     * Warning level logging
     */
    public static void warn(String message) {
        log(LogLevel.WARN, null, message, null);
    }
    
    public static void warn(String category, String message) {
        log(LogLevel.WARN, category, message, null);
    }
    
    public static void warn(String category, String message, Map<String, Object> data) {
        log(LogLevel.WARN, category, message, data);
    }
    
    /**
     * Error level logging
     */
    public static void error(String message) {
        log(LogLevel.ERROR, null, message, null);
    }
    
    public static void error(String category, String message) {
        log(LogLevel.ERROR, category, message, null);
    }
    
    public static void error(String category, String message, Exception e) {
        Map<String, Object> data = new Map<String, Object>{
            'exceptionType' => e.getTypeName(),
            'exceptionMessage' => e.getMessage(),
            'lineNumber' => e.getLineNumber()
        };
        LogEntry entry = createLogEntry(LogLevel.ERROR, category, message, data);
        entry.stackTrace = e.getStackTraceString();
        addLogEntry(entry);
    }
    
    /**
     * Fatal level logging
     */
    public static void fatal(String message) {
        log(LogLevel.FATAL, null, message, null);
    }
    
    public static void fatal(String category, String message) {
        log(LogLevel.FATAL, category, message, null);
    }
    
    public static void fatal(String category, String message, Exception e) {
        Map<String, Object> data = new Map<String, Object>{
            'exceptionType' => e.getTypeName(),
            'exceptionMessage' => e.getMessage(),
            'lineNumber' => e.getLineNumber()
        };
        LogEntry entry = createLogEntry(LogLevel.FATAL, category, message, data);
        entry.stackTrace = e.getStackTraceString();
        addLogEntry(entry);
    }
    
    /**
     * Log A2A operation
     */
    public static void logA2AOperation(String operation, String taskId, Map<String, Object> details) {
        LogEntry entry = new LogEntry();
        entry.level = LogLevel.INFO;
        entry.category = 'A2A_OPERATION';
        entry.message = operation;
        entry.taskId = taskId;
        entry.additionalData = details != null ? details : new Map<String, Object>();
        
        addLogEntry(entry);
    }
    
    /**
     * Log A2A task event
     */
    public static void logTaskEvent(String taskId, String event, A2ATaskManager.A2ATaskStatus status) {
        Map<String, Object> data = new Map<String, Object>{
            'event' => event,
            'status' => status.name(),
            'timestamp' => DateTime.now()
        };
        
        LogEntry entry = new LogEntry();
        entry.level = LogLevel.INFO;
        entry.category = 'TASK_EVENT';
        entry.message = 'Task ' + event + ': ' + status.name();
        entry.taskId = taskId;
        entry.additionalData = data;
        
        addLogEntry(entry);
    }
    
    /**
     * Log API call
     */
    public static void logApiCall(String endpoint, String method, Integer statusCode, Long duration) {
        Map<String, Object> data = new Map<String, Object>{
            'endpoint' => endpoint,
            'method' => method,
            'statusCode' => statusCode,
            'duration' => duration
        };
        
        LogEntry entry = new LogEntry();
        entry.level = statusCode >= 400 ? LogLevel.ERROR : LogLevel.INFO;
        entry.category = 'API_CALL';
        entry.message = method + ' ' + endpoint + ' - ' + statusCode;
        entry.additionalData = data;
        
        addLogEntry(entry);
    }
    
    /**
     * Flush logs (persist to database)
     */
    public static void flush() {
        if (!persistenceEnabled || logEntries.isEmpty()) {
            return;
        }
        
        List<A2A_Log__c> logsToInsert = new List<A2A_Log__c>();
        
        for (LogEntry entry : logEntries) {
            A2A_Log__c log = new A2A_Log__c();
            log.Level__c = entry.level.name();
            log.Category__c = entry.category;
            log.Message__c = A2AUtils.truncate(entry.message, 255);
            log.Full_Message__c = entry.message;
            log.Class_Name__c = entry.className;
            log.Method_Name__c = entry.methodName;
            log.Task_ID__c = entry.taskId;
            log.Context_ID__c = entry.contextId;
            log.Stack_Trace__c = entry.stackTrace;
            log.Additional_Data__c = entry.additionalData != null ? 
                A2AUtils.toJson(entry.additionalData) : null;
            log.Timestamp__c = entry.timestamp;
            
            logsToInsert.add(log);
        }
        
        if (!logsToInsert.isEmpty()) {
            try {
                insert logsToInsert;
                logEntries.clear();
            } catch (Exception e) {
                // Fallback to debug log if persistence fails
                System.debug(LoggingLevel.ERROR, 'Failed to persist logs: ' + e.getMessage());
                for (LogEntry entry : logEntries) {
                    System.debug(formatLogEntry(entry));
                }
            }
        }
    }
    
    /**
     * Private helper methods
     */
    private static void log(LogLevel level, String category, String message, Map<String, Object> data) {
        LogEntry entry = createLogEntry(level, category, message, data);
        addLogEntry(entry);
    }
    
    private static LogEntry createLogEntry(LogLevel level, String category, String message, Map<String, Object> data) {
        LogEntry entry = new LogEntry();
        entry.level = level;
        entry.category = category;
        entry.message = message;
        entry.additionalData = data != null ? data : new Map<String, Object>();
        
        // Try to get class and method name from stack trace
        try {
            String stackTrace = new DmlException().getStackTraceString();
            List<String> lines = stackTrace.split('\n');
            if (lines.size() > 3) {
                String callerLine = lines[3];
                if (callerLine.contains('Class.')) {
                    Integer classStart = callerLine.indexOf('Class.') + 6;
                    Integer methodStart = callerLine.indexOf('.', classStart) + 1;
                    Integer methodEnd = callerLine.indexOf(':', methodStart);
                    
                    if (methodEnd > methodStart) {
                        entry.className = callerLine.substring(classStart, methodStart - 1);
                        entry.methodName = callerLine.substring(methodStart, methodEnd);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors in getting caller info
        }
        
        return entry;
    }
    
    private static void addLogEntry(LogEntry entry) {
        if (entry.level.ordinal() < minimumLevel.ordinal()) {
            return;
        }
        
        logEntries.add(entry);
        
        if (consoleLoggingEnabled) {
            System.debug(getDebugLevel(entry.level), formatLogEntry(entry));
        }
    }
    
    private static LoggingLevel getDebugLevel(LogLevel level) {
        switch on level {
            when DEBUG { return LoggingLevel.DEBUG; }
            when INFO { return LoggingLevel.INFO; }
            when WARN { return LoggingLevel.WARN; }
            when ERROR { return LoggingLevel.ERROR; }
            when FATAL { return LoggingLevel.ERROR; }
            when else { return LoggingLevel.DEBUG; }
        }
    }
    
    private static String formatLogEntry(LogEntry entry) {
        String formatted = String.format(
            '[{0}] [{1}] {2}',
            new List<Object>{
                entry.level.name(),
                entry.category != null ? entry.category : 'GENERAL',
                entry.message
            }
        );
        
        if (entry.className != null) {
            formatted += ' [' + entry.className + '.' + entry.methodName + ']';
        }
        
        if (entry.taskId != null) {
            formatted += ' [Task: ' + entry.taskId + ']';
        }
        
        if (!entry.additionalData.isEmpty()) {
            formatted += ' ' + A2AUtils.toJson(entry.additionalData);
        }
        
        if (entry.stackTrace != null) {
            formatted += '\n' + entry.stackTrace;
        }
        
        return formatted;
    }
}