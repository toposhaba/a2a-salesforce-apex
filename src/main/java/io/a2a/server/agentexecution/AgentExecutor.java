package io.a2a.server.agentexecution;

import io.a2a.server.events.EventQueue;

public interface AgentExecutor {
    void execute(RequestContext context, EventQueue eventQueue);

    void cancel(RequestContext context, EventQueue eventQueue);
}
