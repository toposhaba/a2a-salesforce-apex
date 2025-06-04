package io.a2a.examples.helloworld.server;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.spec.Artifact;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Task;
import io.a2a.spec.TaskArtifactUpdateEvent;
import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;
import io.a2a.spec.TextPart;
import io.a2a.spec.UnsupportedOperationError;

@ApplicationScoped
public class AgentExecutorProducer {

    @Produces
    public AgentExecutor agentExecutor() {
        return new AgentExecutor() {
            List<String> runningTasks = new ArrayList<>();
            @Override
            public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                //eventQueue.enqueueEvent(A2A.toAgentMessage("Hello World"));
                Task task = context.getTask();
                if (task == null) {
                    task = new Task.Builder()
                            .id(context.getTaskId())
                            .contextId(context.getContextId())
                            .status(new TaskStatus(TaskState.SUBMITTED))
                            .history(context.getMessage())
                            .build();
                    eventQueue.enqueueEvent(task);
                }
                runningTasks.add(task.getId());
                try {
                    eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                            .taskId(context.getTaskId())
                            .contextId(context.getContextId())
                            .status(new TaskStatus(TaskState.SUBMITTED))
                            .build());

                    Thread.sleep(500);

                    if (! runningTasks.contains(context.getTaskId())) {
                        return; // task was cancelled
                    }

                    eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                            .taskId(context.getTaskId())
                            .contextId(context.getContextId())
                            .status(new TaskStatus(TaskState.WORKING))
                            .build());

                    Thread.sleep(500);

                    if (! runningTasks.contains(context.getTaskId())) {
                        return;
                    }

                    

                    eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                            .taskId(context.getTaskId())
                            .contextId(context.getContextId())
                            .status(new TaskStatus(TaskState.COMPLETED))
                            .isFinal(true)
                            .build());
                } catch (Exception e) {
                    e.printStackTrace();
                    eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                            .taskId(context.getTaskId())
                            .contextId(context.getContextId())
                            .status(new TaskStatus(TaskState.FAILED))
                            .isFinal(true)
                            .build());
                }
            }

            @Override
            public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
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
                runningTasks.remove(task.getId());
                eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                        .taskId(task.getId())
                        .contextId(task.getContextId())
                        .status(new TaskStatus(TaskState.CANCELED))
                        .isFinal(true)
                        .build());
            }
        };
    }
}
