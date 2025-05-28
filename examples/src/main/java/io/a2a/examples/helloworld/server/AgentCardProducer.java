package io.a2a.examples.helloworld.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import io.a2a.spec.AgentAuthentication;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class AgentCardProducer {

    @Produces
    public AgentCard agentCard() {
        return new AgentCard(
                "MyAgent",
                "My agent card",
                "http://localhost:9999",
                null,
                "1.0",
                "http://example.com/docs",
                new AgentCapabilities(true, true, true),
                new AgentAuthentication(new ArrayList<String>(), null),
                null,
                null,
                Collections.singletonList(new AgentSkill.Builder()
                        .id("skill-123")
                        .name("Greeter")
                        .description("Greets the user")
                        .tags(Collections.singletonList("greeting"))
                        .build())
        );
    }
}

