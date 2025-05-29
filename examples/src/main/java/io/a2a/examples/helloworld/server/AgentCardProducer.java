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
        return new AgentCard.Builder()
                .name("MyAgent")
                .description("My agent card")
                .url("http://localhost:9999")
                .version("1.0")
                .documentationUrl("http://example.com/docs")
                .capabilities(new AgentCapabilities(true, true, true))
                .authentication(new AgentAuthentication(new ArrayList<>(), null))
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .skills(Collections.singletonList(new AgentSkill.Builder()
                                .id("skill-123")
                                .name("Greeter")
                                .description("Greets the user")
                                .tags(Collections.singletonList("greeting"))
                                .build()))
                .build();
    }
}

