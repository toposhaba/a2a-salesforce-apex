package io.a2a.server.apps;

import java.util.ArrayList;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class AgentCardProducer {

    @Produces
    public AgentCard agentCard() {
        return new AgentCard(
                "test-card",
                "A test agent card",
                "http://localhost:8081",
                null,
                "1.0",
                "http://example.con/docs",
                new AgentCapabilities(true, true, true),
                null,
                null,
                null,
                new ArrayList<>()
        );
    }
}

