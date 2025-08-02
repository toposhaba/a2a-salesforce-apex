/**
 * Interface for storing and retrieving A2A tasks.
 * Converted from Java: io.a2a.server.tasks.TaskStore
 */
public interface A2ATaskStore {
    /**
     * Save a task
     */
    A2ATaskManager.A2ATask save(A2ATaskManager.A2ATask task);
    
    /**
     * Get a task by ID
     */
    A2ATaskManager.A2ATask get(String taskId);
    
    /**
     * Delete a task by ID
     */
    Boolean remove(String taskId);
    
    /**
     * Check if a task exists
     */
    Boolean exists(String taskId);
    
    /**
     * Get all tasks (with optional filtering)
     */
    List<A2ATaskManager.A2ATask> getAll();
    
    /**
     * Get tasks by status
     */
    List<A2ATaskManager.A2ATask> getByStatus(A2ATaskManager.A2ATaskStatus status);
}