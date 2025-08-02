/**
 * Test class for A2ATaskManager
 */
@isTest
private class A2ATaskManagerTest {
    
    @isTest
    static void testTaskCreationAndRetrieval() {
        // Create task store
        A2ATaskStore taskStore = new A2AInMemoryTaskStore();
        
        // Create initial message
        A2AMessage message = new A2AMessage();
        message.id = 'msg_test';
        message.role = 'user';
        message.parts = new List<A2APart>{ new A2ATextPart('Test message') };
        
        // Create task manager
        A2ATaskManager taskManager = new A2ATaskManager('task_123', 'ctx_456', taskStore, message);
        
        // Test getters
        System.assertEquals('task_123', taskManager.getTaskId());
        System.assertEquals('ctx_456', taskManager.getContextId());
        
        // Get task (should be null initially)
        A2ATaskManager.A2ATask task = taskManager.getTask();
        System.assertEquals(null, task);
        
        // Create and save task
        A2ATaskManager.A2ATask newTask = new A2ATaskManager.A2ATask();
        newTask.id = 'task_123';
        newTask.contextId = 'ctx_456';
        newTask.status = A2ATaskManager.A2ATaskStatus.SUBMITTED;
        newTask.createdAt = DateTime.now();
        newTask.updatedAt = DateTime.now();
        
        A2ATaskManager.A2ATask savedTask = taskManager.saveTaskEvent(newTask);
        System.assertNotEquals(null, savedTask);
        System.assertEquals('task_123', savedTask.id);
        
        // Retrieve task
        A2ATaskManager.A2ATask retrievedTask = taskManager.getTask();
        System.assertNotEquals(null, retrievedTask);
        System.assertEquals('task_123', retrievedTask.id);
        System.assertEquals(A2ATaskManager.A2ATaskStatus.SUBMITTED, retrievedTask.status);
    }
    
    @isTest
    static void testTaskStatusUpdate() {
        A2ATaskStore taskStore = new A2AInMemoryTaskStore();
        A2ATaskManager taskManager = new A2ATaskManager('task_456', 'ctx_789', taskStore, null);
        
        // Create status update event
        A2ATaskManager.A2ATaskStatusUpdateEvent statusUpdate = new A2ATaskManager.A2ATaskStatusUpdateEvent();
        statusUpdate.taskId = 'task_456';
        statusUpdate.contextId = 'ctx_789';
        statusUpdate.status = A2ATaskManager.A2ATaskStatus.RUNNING;
        
        // Save status update
        A2ATaskManager.A2ATask updatedTask = taskManager.saveTaskStatusUpdateEvent(statusUpdate);
        System.assertNotEquals(null, updatedTask);
        System.assertEquals(A2ATaskManager.A2ATaskStatus.RUNNING, updatedTask.status);
        
        // Update to completed
        statusUpdate.status = A2ATaskManager.A2ATaskStatus.COMPLETED;
        updatedTask = taskManager.saveTaskStatusUpdateEvent(statusUpdate);
        System.assertEquals(A2ATaskManager.A2ATaskStatus.COMPLETED, updatedTask.status);
    }
    
    @isTest
    static void testTaskArtifactUpdate() {
        A2ATaskStore taskStore = new A2AInMemoryTaskStore();
        A2ATaskManager taskManager = new A2ATaskManager('task_789', 'ctx_012', taskStore, null);
        
        // Create artifact
        A2ATaskManager.A2AArtifact artifact = new A2ATaskManager.A2AArtifact();
        artifact.artifactId = 'artifact_1';
        artifact.mimeType = 'text/plain';
        artifact.parts = new List<A2APart>{ new A2ATextPart('Result data') };
        
        // Create artifact update event
        A2ATaskManager.A2ATaskArtifactUpdateEvent artifactUpdate = new A2ATaskManager.A2ATaskArtifactUpdateEvent();
        artifactUpdate.taskId = 'task_789';
        artifactUpdate.contextId = 'ctx_012';
        artifactUpdate.artifact = artifact;
        artifactUpdate.isAppend = false;
        
        // Save artifact update
        A2ATaskManager.A2ATask updatedTask = taskManager.saveTaskArtifactUpdateEvent(artifactUpdate);
        System.assertNotEquals(null, updatedTask);
        System.assertNotEquals(null, updatedTask.artifacts);
        System.assertEquals(1, updatedTask.artifacts.size());
        System.assertEquals('artifact_1', updatedTask.artifacts[0].artifactId);
        
        // Append to artifact
        A2ATaskManager.A2AArtifact appendArtifact = new A2ATaskManager.A2AArtifact();
        appendArtifact.artifactId = 'artifact_1';
        appendArtifact.parts = new List<A2APart>{ new A2ATextPart('Additional data') };
        
        artifactUpdate.artifact = appendArtifact;
        artifactUpdate.isAppend = true;
        
        updatedTask = taskManager.saveTaskArtifactUpdateEvent(artifactUpdate);
        System.assertEquals(1, updatedTask.artifacts.size());
        System.assertEquals(2, updatedTask.artifacts[0].parts.size());
    }
    
    @isTest
    static void testTaskCancellation() {
        A2ATaskStore taskStore = new A2AInMemoryTaskStore();
        A2ATaskManager taskManager = new A2ATaskManager('task_cancel', 'ctx_cancel', taskStore, null);
        
        // Create task
        A2ATaskManager.A2ATask task = new A2ATaskManager.A2ATask();
        task.id = 'task_cancel';
        task.contextId = 'ctx_cancel';
        task.status = A2ATaskManager.A2ATaskStatus.RUNNING;
        task.createdAt = DateTime.now();
        task.updatedAt = DateTime.now();
        
        taskManager.saveTaskEvent(task);
        
        // Cancel task
        Boolean cancelled = taskManager.cancelTask();
        System.assertEquals(true, cancelled);
        
        // Verify task is cancelled
        A2ATaskManager.A2ATask cancelledTask = taskManager.getTask();
        System.assertEquals(A2ATaskManager.A2ATaskStatus.CANCELLED, cancelledTask.status);
        
        // Try to cancel already cancelled task
        cancelled = taskManager.cancelTask();
        System.assertEquals(false, cancelled);
    }
    
    @isTest
    static void testInMemoryTaskStore() {
        A2AInMemoryTaskStore store = new A2AInMemoryTaskStore();
        
        // Test save and retrieve
        A2ATaskManager.A2ATask task = new A2ATaskManager.A2ATask();
        task.id = 'test_task';
        task.status = A2ATaskManager.A2ATaskStatus.SUBMITTED;
        
        A2ATaskManager.A2ATask savedTask = store.save(task);
        System.assertEquals('test_task', savedTask.id);
        
        // Test exists
        System.assertEquals(true, store.exists('test_task'));
        System.assertEquals(false, store.exists('non_existent'));
        
        // Test get
        A2ATaskManager.A2ATask retrievedTask = store.get('test_task');
        System.assertNotEquals(null, retrievedTask);
        System.assertEquals('test_task', retrievedTask.id);
        
        // Test getAll
        List<A2ATaskManager.A2ATask> allTasks = store.getAll();
        System.assertEquals(1, allTasks.size());
        
        // Test getByStatus
        List<A2ATaskManager.A2ATask> submittedTasks = store.getByStatus(A2ATaskManager.A2ATaskStatus.SUBMITTED);
        System.assertEquals(1, submittedTasks.size());
        
        List<A2ATaskManager.A2ATask> runningTasks = store.getByStatus(A2ATaskManager.A2ATaskStatus.RUNNING);
        System.assertEquals(0, runningTasks.size());
        
        // Test remove
        Boolean removed = store.remove('test_task');
        System.assertEquals(true, removed);
        System.assertEquals(false, store.exists('test_task'));
        
        // Test size and clear
        System.assertEquals(0, store.size());
        
        store.save(task);
        System.assertEquals(1, store.size());
        
        store.clear();
        System.assertEquals(0, store.size());
    }
}