/**
 * Batch processor for handling large volumes of A2A tasks asynchronously.
 * Useful for bulk operations, scheduled processing, and retry mechanisms
 */
public class A2ATaskProcessorBatch implements Database.Batchable<sObject>, Database.AllowsCallouts, Database.Stateful {
    
    private String agentUrl;
    private Integer successCount = 0;
    private Integer errorCount = 0;
    private List<String> errors = new List<String>();
    private A2ATaskProcessorConfig config;
    
    /**
     * Configuration class for batch processing
     */
    public class A2ATaskProcessorConfig {
        public String taskStatus { get; set; }
        public Integer maxRetries { get; set; }
        public Boolean updateTasksAfterProcessing { get; set; }
        public String notificationEmail { get; set; }
        
        public A2ATaskProcessorConfig() {
            this.taskStatus = 'SUBMITTED';
            this.maxRetries = 3;
            this.updateTasksAfterProcessing = true;
        }
    }
    
    /**
     * Constructor
     */
    public A2ATaskProcessorBatch(String agentUrl) {
        this.agentUrl = agentUrl;
        this.config = new A2ATaskProcessorConfig();
    }
    
    public A2ATaskProcessorBatch(String agentUrl, A2ATaskProcessorConfig config) {
        this.agentUrl = agentUrl;
        this.config = config != null ? config : new A2ATaskProcessorConfig();
    }
    
    /**
     * Start method - query tasks to process
     */
    public Database.QueryLocator start(Database.BatchableContext bc) {
        A2ALogger.info('BATCH_PROCESS', 'Starting A2A task batch processing', new Map<String, Object>{
            'agentUrl' => agentUrl,
            'targetStatus' => config.taskStatus
        });
        
        String query = 'SELECT Id, Task_ID__c, Context_ID__c, Status__c, Message_History__c, Metadata__c ' +
                      'FROM A2A_Task__c ' +
                      'WHERE Status__c = :taskStatus ' +
                      'ORDER BY CreatedDate ASC';
        
        return Database.getQueryLocator(query);
    }
    
    /**
     * Execute method - process each batch of tasks
     */
    public void execute(Database.BatchableContext bc, List<A2A_Task__c> scope) {
        A2ALogger.info('BATCH_PROCESS', 'Processing batch', new Map<String, Object>{
            'batchSize' => scope.size()
        });
        
        List<A2A_Task__c> tasksToUpdate = new List<A2A_Task__c>();
        A2AClient client = new A2AClient(agentUrl);
        
        for (A2A_Task__c taskRecord : scope) {
            try {
                // Process the task
                ProcessResult result = processTask(client, taskRecord);
                
                if (result.success) {
                    successCount++;
                    
                    if (config.updateTasksAfterProcessing && result.updatedTask != null) {
                        tasksToUpdate.add(result.updatedTask);
                    }
                } else {
                    errorCount++;
                    errors.add('Task ' + taskRecord.Task_ID__c + ': ' + result.errorMessage);
                    
                    // Update task with error status
                    if (config.updateTasksAfterProcessing) {
                        taskRecord.Status__c = 'FAILED';
                        Map<String, Object> metadata = String.isNotBlank(taskRecord.Metadata__c) ? 
                            (Map<String, Object>) JSON.deserializeUntyped(taskRecord.Metadata__c) : 
                            new Map<String, Object>();
                        metadata.put('lastError', result.errorMessage);
                        metadata.put('lastProcessedDate', DateTime.now().format());
                        taskRecord.Metadata__c = JSON.serialize(metadata);
                        tasksToUpdate.add(taskRecord);
                    }
                }
                
            } catch (Exception e) {
                errorCount++;
                errors.add('Task ' + taskRecord.Task_ID__c + ': ' + e.getMessage());
                A2ALogger.error('BATCH_PROCESS', 'Error processing task', e);
            }
        }
        
        // Update tasks
        if (!tasksToUpdate.isEmpty()) {
            try {
                update tasksToUpdate;
            } catch (Exception e) {
                A2ALogger.error('BATCH_PROCESS', 'Error updating tasks', e);
            }
        }
        
        // Flush logs
        A2ALogger.flush();
    }
    
    /**
     * Finish method - send summary notification
     */
    public void finish(Database.BatchableContext bc) {
        A2ALogger.info('BATCH_PROCESS', 'Batch processing completed', new Map<String, Object>{
            'successCount' => successCount,
            'errorCount' => errorCount,
            'totalProcessed' => successCount + errorCount
        });
        
        // Send notification email if configured
        if (String.isNotBlank(config.notificationEmail)) {
            sendNotification();
        }
        
        A2ALogger.flush();
    }
    
    /**
     * Process individual task
     */
    private ProcessResult processTask(A2AClient client, A2A_Task__c taskRecord) {
        ProcessResult result = new ProcessResult();
        
        try {
            // Get current task status from agent
            A2AClient.A2AGetTaskResponse response = client.getTask(taskRecord.Task_ID__c);
            
            if (response.error != null) {
                result.success = false;
                result.errorMessage = response.error.message;
                return result;
            }
            
            if (response.result != null) {
                // Update task record with latest status
                taskRecord.Status__c = response.result.status;
                
                // Update artifacts if present
                if (response.result.artifacts != null && !response.result.artifacts.isEmpty()) {
                    taskRecord.Artifacts__c = JSON.serialize(response.result.artifacts);
                }
                
                // Update metadata
                Map<String, Object> metadata = String.isNotBlank(taskRecord.Metadata__c) ? 
                    (Map<String, Object>) JSON.deserializeUntyped(taskRecord.Metadata__c) : 
                    new Map<String, Object>();
                metadata.put('lastSyncDate', DateTime.now().format());
                metadata.put('syncedStatus', response.result.status);
                taskRecord.Metadata__c = JSON.serialize(metadata);
                
                result.success = true;
                result.updatedTask = taskRecord;
                
                // Publish platform event for status update
                A2APlatformEventPublisher.publishTaskStatusUpdate(
                    taskRecord.Task_ID__c, 
                    response.result.status
                );
            }
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * Send notification email
     */
    private void sendNotification() {
        try {
            Messaging.SingleEmailMessage mail = new Messaging.SingleEmailMessage();
            mail.setToAddresses(new String[] { config.notificationEmail });
            mail.setSubject('A2A Batch Processing Completed');
            
            String body = 'A2A Task Batch Processing Summary\n\n';
            body += 'Agent URL: ' + agentUrl + '\n';
            body += 'Total Processed: ' + (successCount + errorCount) + '\n';
            body += 'Successful: ' + successCount + '\n';
            body += 'Failed: ' + errorCount + '\n\n';
            
            if (!errors.isEmpty()) {
                body += 'Errors:\n';
                for (Integer i = 0; i < Math.min(errors.size(), 10); i++) {
                    body += '- ' + errors[i] + '\n';
                }
                if (errors.size() > 10) {
                    body += '... and ' + (errors.size() - 10) + ' more errors\n';
                }
            }
            
            mail.setPlainTextBody(body);
            Messaging.sendEmail(new Messaging.SingleEmailMessage[] { mail });
            
        } catch (Exception e) {
            A2ALogger.error('BATCH_PROCESS', 'Failed to send notification email', e);
        }
    }
    
    /**
     * Helper class for process results
     */
    private class ProcessResult {
        public Boolean success { get; set; }
        public String errorMessage { get; set; }
        public A2A_Task__c updatedTask { get; set; }
        
        public ProcessResult() {
            this.success = false;
        }
    }
}