package io.a2a.server.apps;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.UnsupportedOperationError;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class AgentExecutorProducer {

    @Produces
    public AgentExecutor agentExecutor() {
        return new AgentExecutor() {
            @Override
            public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                if (context.getTaskId().equals("task-not-supported-123")) {
                    eventQueue.enqueueEvent(new UnsupportedOperationError());
                }
                eventQueue.enqueueEvent(context.getMessage() != null ? context.getMessage() : context.getTask());
            }

            @Override
            public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
                if (context.getTask().getId().equals("cancel-task-123")) {
                    Task task = context.getTask();
                    Task updated = new Task.Builder(task)
                            .status(new TaskStatus(TaskState.CANCELED))
                            .build();

                    eventQueue.enqueueEvent(updated);
                } else if (context.getTask().getId().equals("cancel-task-not-supported-123")) {
                    throw new UnsupportedOperationError();
                }
            }
        };
    }
}
