/**
 * Queueable class for processing A2A messages asynchronously
 * Supports chaining for processing multiple messages and retry logic
 */
public class A2AMessageQueueable implements Queueable, Database.AllowsCallouts {
    
    private List<MessageRequest> messageRequests;
    private Integer currentIndex = 0;
    private A2AMessageQueueableConfig config;
    private List<MessageResult> results;
    
    /**
     * Configuration for queueable processing
     */
    public class A2AMessageQueueableConfig {
        public Integer maxRetries { get; set; }
        public Integer delayBetweenMessages { get; set; }
        public Boolean chainNextMessage { get; set; }
        public String callbackApexClass { get; set; }
        public String callbackMethodName { get; set; }
        
        public A2AMessageQueueableConfig() {
            this.maxRetries = 3;
            this.delayBetweenMessages = 0;
            this.chainNextMessage = true;
        }
    }
    
    /**
     * Message request structure
     */
    public class MessageRequest {
        public String agentUrl { get; set; }
        public A2AMessage message { get; set; }
        public String requestId { get; set; }
        public Integer retryCount { get; set; }
        public Map<String, Object> metadata { get; set; }
        
        public MessageRequest(String agentUrl, A2AMessage message) {
            this.agentUrl = agentUrl;
            this.message = message;
            this.requestId = A2AUtils.generateUniqueId('req');
            this.retryCount = 0;
            this.metadata = new Map<String, Object>();
        }
    }
    
    /**
     * Message result structure
     */
    public class MessageResult {
        public String requestId { get; set; }
        public Boolean success { get; set; }
        public String taskId { get; set; }
        public String errorMessage { get; set; }
        public DateTime processedAt { get; set; }
        
        public MessageResult(String requestId) {
            this.requestId = requestId;
            this.processedAt = DateTime.now();
        }
    }
    
    /**
     * Constructor for single message
     */
    public A2AMessageQueueable(String agentUrl, A2AMessage message) {
        this.messageRequests = new List<MessageRequest>{ 
            new MessageRequest(agentUrl, message) 
        };
        this.config = new A2AMessageQueueableConfig();
        this.results = new List<MessageResult>();
    }
    
    /**
     * Constructor for multiple messages
     */
    public A2AMessageQueueable(List<MessageRequest> requests, A2AMessageQueueableConfig config) {
        this.messageRequests = requests;
        this.config = config != null ? config : new A2AMessageQueueableConfig();
        this.results = new List<MessageResult>();
    }
    
    /**
     * Constructor for continuing chain
     */
    private A2AMessageQueueable(List<MessageRequest> requests, Integer startIndex, 
                               A2AMessageQueueableConfig config, List<MessageResult> previousResults) {
        this.messageRequests = requests;
        this.currentIndex = startIndex;
        this.config = config;
        this.results = previousResults;
    }
    
    /**
     * Execute queueable
     */
    public void execute(QueueableContext context) {
        if (currentIndex >= messageRequests.size()) {
            // All messages processed, invoke callback if configured
            invokeCallback();
            return;
        }
        
        MessageRequest request = messageRequests[currentIndex];
        MessageResult result = new MessageResult(request.requestId);
        
        try {
            A2ALogger.info('QUEUEABLE', 'Processing message', new Map<String, Object>{
                'requestId' => request.requestId,
                'agentUrl' => request.agentUrl,
                'retryCount' => request.retryCount
            });
            
            // Create client and send message
            A2AClient client = new A2AClient(request.agentUrl);
            A2AClient.A2AMessageSendParams params = new A2AClient.A2AMessageSendParams(request.message);
            A2AClient.A2ASendMessageResponse response = client.sendMessage(request.requestId, params);
            
            if (response.error != null) {
                handleError(request, result, response.error.message);
            } else if (response.result != null) {
                // Success
                result.success = true;
                Map<String, Object> resultData = (Map<String, Object>) response.result.data;
                result.taskId = (String) resultData.get('id');
                
                A2ALogger.info('QUEUEABLE', 'Message sent successfully', new Map<String, Object>{
                    'requestId' => request.requestId,
                    'taskId' => result.taskId
                });
                
                // Store task in database
                storeTask(request, result.taskId);
            }
            
        } catch (Exception e) {
            handleError(request, result, e.getMessage());
        }
        
        results.add(result);
        
        // Chain next message if configured
        if (config.chainNextMessage && currentIndex + 1 < messageRequests.size()) {
            chainNext();
        } else if (currentIndex + 1 >= messageRequests.size()) {
            // All done, invoke callback
            invokeCallback();
        }
        
        A2ALogger.flush();
    }
    
    /**
     * Handle error with retry logic
     */
    private void handleError(MessageRequest request, MessageResult result, String errorMessage) {
        result.success = false;
        result.errorMessage = errorMessage;
        
        A2ALogger.warn('QUEUEABLE', 'Message processing failed', new Map<String, Object>{
            'requestId' => request.requestId,
            'error' => errorMessage,
            'retryCount' => request.retryCount
        });
        
        // Check if we should retry
        if (request.retryCount < config.maxRetries) {
            request.retryCount++;
            // Re-add to queue for retry
            messageRequests.add(request);
            A2ALogger.info('QUEUEABLE', 'Message queued for retry', new Map<String, Object>{
                'requestId' => request.requestId,
                'retryCount' => request.retryCount
            });
        }
    }
    
    /**
     * Store task in database
     */
    private void storeTask(MessageRequest request, String taskId) {
        try {
            A2ATaskStore taskStore = new A2ACustomObjectTaskStore();
            A2ATaskManager.A2ATask task = new A2ATaskManager.A2ATask();
            task.id = taskId;
            task.contextId = request.message.contextId;
            task.status = A2ATaskManager.A2ATaskStatus.SUBMITTED;
            task.createdAt = DateTime.now();
            task.updatedAt = DateTime.now();
            task.metadata = request.metadata;
            
            taskStore.save(task);
            
        } catch (Exception e) {
            A2ALogger.error('QUEUEABLE', 'Failed to store task', e);
        }
    }
    
    /**
     * Chain next message
     */
    private void chainNext() {
        if (config.delayBetweenMessages > 0) {
            // Schedule with delay
            DateTime scheduledTime = DateTime.now().addSeconds(config.delayBetweenMessages);
            String cronExp = scheduledTime.format('s m H d M ? yyyy');
            String jobName = 'A2AMessageQueue_' + A2AUtils.generateUniqueId('job');
            
            System.schedule(jobName, cronExp, new A2AMessageSchedulable(
                messageRequests, currentIndex + 1, config, results
            ));
        } else {
            // Chain immediately
            System.enqueueJob(new A2AMessageQueueable(
                messageRequests, currentIndex + 1, config, results
            ));
        }
    }
    
    /**
     * Invoke callback method
     */
    private void invokeCallback() {
        if (String.isNotBlank(config.callbackApexClass) && String.isNotBlank(config.callbackMethodName)) {
            try {
                Type callbackType = Type.forName(config.callbackApexClass);
                if (callbackType != null) {
                    Object callbackInstance = callbackType.newInstance();
                    
                    // Use reflection to invoke callback
                    Map<String, Object> params = new Map<String, Object>{
                        'results' => results,
                        'config' => config
                    };
                    
                    A2ALogger.info('QUEUEABLE', 'Invoking callback', new Map<String, Object>{
                        'class' => config.callbackApexClass,
                        'method' => config.callbackMethodName,
                        'resultCount' => results.size()
                    });
                    
                    // Note: In real implementation, you'd need proper reflection
                    // This is simplified for the example
                }
            } catch (Exception e) {
                A2ALogger.error('QUEUEABLE', 'Failed to invoke callback', e);
            }
        }
    }
    
    /**
     * Schedulable wrapper for delayed execution
     */
    public class A2AMessageSchedulable implements Schedulable {
        private List<MessageRequest> requests;
        private Integer startIndex;
        private A2AMessageQueueableConfig config;
        private List<MessageResult> results;
        
        public A2AMessageSchedulable(List<MessageRequest> requests, Integer startIndex,
                                    A2AMessageQueueableConfig config, List<MessageResult> results) {
            this.requests = requests;
            this.startIndex = startIndex;
            this.config = config;
            this.results = results;
        }
        
        public void execute(SchedulableContext sc) {
            System.enqueueJob(new A2AMessageQueueable(requests, startIndex, config, results));
        }
    }
    
    /**
     * Static helper to enqueue messages
     */
    public static Id enqueueMessages(List<MessageRequest> requests) {
        return enqueueMessages(requests, null);
    }
    
    public static Id enqueueMessages(List<MessageRequest> requests, A2AMessageQueueableConfig config) {
        return System.enqueueJob(new A2AMessageQueueable(requests, config));
    }
}