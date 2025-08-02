/**
 * In-memory implementation of A2ATaskStore for testing and development.
 * Note: This is not suitable for production as data is lost on restart.
 */
public class A2AInMemoryTaskStore implements A2ATaskStore {
    private Map<String, A2ATaskManager.A2ATask> tasks;
    
    /**
     * Constructor
     */
    public A2AInMemoryTaskStore() {
        this.tasks = new Map<String, A2ATaskManager.A2ATask>();
    }
    
    /**
     * Save a task
     */
    public A2ATaskManager.A2ATask save(A2ATaskManager.A2ATask task) {
        if (task == null || String.isBlank(task.id)) {
            throw new A2AException('Task or task ID cannot be null');
        }
        
        this.tasks.put(task.id, task);
        return task;
    }
    
    /**
     * Get a task by ID
     */
    public A2ATaskManager.A2ATask get(String taskId) {
        if (String.isBlank(taskId)) {
            return null;
        }
        
        return this.tasks.get(taskId);
    }
    
    /**
     * Delete a task by ID
     */
    public Boolean remove(String taskId) {
        if (String.isBlank(taskId)) {
            return false;
        }
        
        return this.tasks.remove(taskId) != null;
    }
    
    /**
     * Check if a task exists
     */
    public Boolean exists(String taskId) {
        if (String.isBlank(taskId)) {
            return false;
        }
        
        return this.tasks.containsKey(taskId);
    }
    
    /**
     * Get all tasks
     */
    public List<A2ATaskManager.A2ATask> getAll() {
        return this.tasks.values();
    }
    
    /**
     * Get tasks by status
     */
    public List<A2ATaskManager.A2ATask> getByStatus(A2ATaskManager.A2ATaskStatus status) {
        List<A2ATaskManager.A2ATask> result = new List<A2ATaskManager.A2ATask>();
        
        for (A2ATaskManager.A2ATask task : this.tasks.values()) {
            if (task.status == status) {
                result.add(task);
            }
        }
        
        return result;
    }
    
    /**
     * Clear all tasks (for testing)
     */
    public void clear() {
        this.tasks.clear();
    }
    
    /**
     * Get the number of tasks
     */
    public Integer size() {
        return this.tasks.size();
    }
}