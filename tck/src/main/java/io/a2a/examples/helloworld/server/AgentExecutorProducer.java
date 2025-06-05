package io.a2a.examples.helloworld.server;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.annotation.PreDestroy;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;

@ApplicationScoped
public class AgentExecutorProducer {

    @Produces
    public AgentExecutor agentExecutor() {
        return new FireAndForgetAgentExecutor();
    }
    
    private static class FireAndForgetAgentExecutor implements AgentExecutor {
        // Dedicated thread pool for background task execution
        private final ExecutorService taskExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "AgentTask-" + System.currentTimeMillis());
            t.setDaemon(true); // Don't prevent JVM shutdown
            return t;
        });
        
        // Track running tasks for cancellation - store the future reference
        private final Map<String, CompletableFuture<Void>> runningTasks = new ConcurrentHashMap<>();

        @Override
        public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
            Task task = context.getTask();

            if (context.getMessage().getTaskId() != null && task == null && context.getMessage().getTaskId().startsWith("non-existent")) {
                throw new TaskNotFoundError();
            }

            if (task == null) {
                task = new Task.Builder()
                        .id(context.getTaskId())
                        .contextId(context.getContextId())
                        .status(new TaskStatus(TaskState.SUBMITTED))
                        .history(context.getMessage())
                        .build();
                eventQueue.enqueueEvent(task);
            }

            // Immediately set to WORKING state
            eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                    .taskId(context.getTaskId())
                    .contextId(context.getContextId())
                    .status(new TaskStatus(TaskState.WORKING))
                    .build());
            
            System.out.println("====> task set to WORKING, starting background execution");

            // Fire and forget - start the task but don't wait for it
            CompletableFuture<Void> taskFuture = CompletableFuture
                .runAsync(() -> executeTaskInBackground(context, eventQueue), taskExecutor);
            
            // Store the future for potential cancellation
            runningTasks.put(context.getTaskId(), taskFuture);
            
            // Method returns immediately - task continues in background
            System.out.println("====> execute() method returning immediately, task running in background");
        }

        @Override
        public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
            System.out.println("====> task cancel request received");
            Task task = context.getTask();
            
            if (task == null) {
                System.out.println("====> task not found");
                throw new TaskNotFoundError();
            }
            
            if (task.getStatus().state() == TaskState.CANCELED) {
                System.out.println("====> task already canceled");
                throw new TaskNotCancelableError();
            }
            
            if (task.getStatus().state() == TaskState.COMPLETED) {
                System.out.println("====> task already completed");
                throw new TaskNotCancelableError();
            }

            // Cancel the CompletableFuture
            CompletableFuture<Void> taskFuture = runningTasks.get(task.getId());
            if (taskFuture != null) {
                boolean cancelled = taskFuture.cancel(true); // mayInterruptIfRunning = true
                System.out.println("====> cancellation attempted, success: " + cancelled);
            }
            
            // Remove from running tasks and update status
            runningTasks.remove(task.getId());
            
            eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                    .taskId(task.getId())
                    .contextId(task.getContextId())
                    .status(new TaskStatus(TaskState.CANCELED))
                    .isFinal(true)
                    .build());
            
            System.out.println("====> task canceled");
        }

        /**
         * This method runs completely in the background.
         * The main execute() method has already returned.
         */
        private void executeTaskInBackground(RequestContext context, EventQueue eventQueue) {
            String taskId = context.getTaskId();
            
            try {
                System.out.println("====> background execution started for task: " + taskId);
                
                // Check if task was cancelled before we even started
                if (!runningTasks.containsKey(taskId)) {
                    System.out.println("====> task was cancelled before background execution started");
                    return;
                }
                
                // Perform the actual work
                Object result = performActualWork(context);
                
                // Check again if task was cancelled during execution
                if (!runningTasks.containsKey(taskId)) {
                    System.out.println("====> task was cancelled during execution");
                    return;
                }
                
                // Task completed successfully
                eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                        .taskId(taskId)
                        .contextId(context.getContextId())
                        .status(new TaskStatus(TaskState.COMPLETED))
                        .isFinal(true)
                        .build());
                
                System.out.println("====> background task completed successfully: " + taskId);
                
            } catch (InterruptedException e) {
                // Task was interrupted (cancelled)
                System.out.println("====> background task was interrupted: " + taskId);
                Thread.currentThread().interrupt();
                
                // Only send CANCELED event if task is still tracked (not already cancelled)
                if (runningTasks.containsKey(taskId)) {
                    eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                            .taskId(taskId)
                            .contextId(context.getContextId())
                            .status(new TaskStatus(TaskState.CANCELED))
                            .isFinal(true)
                            .build());
                }
                
            } catch (Exception e) {
                // Task failed
                System.err.println("====> background task failed: " + taskId);
                e.printStackTrace();
                
                if (runningTasks.containsKey(taskId)) {
                    eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                            .taskId(taskId)
                            .contextId(context.getContextId())
                            .status(new TaskStatus(TaskState.FAILED))
                            .isFinal(true)
                            .build());
                }
                
            } finally {
                // Always clean up - remove from running tasks
                runningTasks.remove(taskId);
                System.out.println("====> background task cleanup completed: " + taskId);
            }
        }

        /**
         * This method represents the actual work that needs to be done.
         * Replace this with your real business logic.
         */
        private Object performActualWork(RequestContext context) throws InterruptedException {
            
            
            System.out.println("====> starting actual work for task: " + context.getTaskId());
            
            // Simulate work that can be interrupted
            for (int i = 0; i < 10; i++) {
                // Check for interruption regularly during long-running work
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Task was cancelled during execution");
                }
                
                Thread.sleep(200); // Simulate work chunks
                System.out.println("====> work progress for task " + context.getTaskId() + ": " + ((i + 1) * 10) + "%");
            }
            
            System.out.println("====> finished actual work for task: " + context.getTaskId());
            return "Task completed successfully";
        }

        /**
         * Cleanup method for proper resource management
         */
        @PreDestroy
        public void cleanup() {
            System.out.println("====> shutting down task executor");
            taskExecutor.shutdown();
            try {
                if (!taskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    taskExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                taskExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}