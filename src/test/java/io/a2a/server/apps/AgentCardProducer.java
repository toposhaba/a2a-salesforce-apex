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
        return new AgentCard.Builder()
                .name("test-card")
                .description("A test agent card")
                .url("http://localhost:8081")
                .version("1.0")
                .documentationUrl("http://example.com/docs")
                .capabilities(new AgentCapabilities(true, true, true))
                .defaultInputModes(new ArrayList<>())
                .defaultOutputModes(new ArrayList<>())
                .skills(new ArrayList<>())
                .build();
    }
}

