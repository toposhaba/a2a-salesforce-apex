package io.a2a.spec;

import java.util.Collections;
import java.util.List;

import io.a2a.util.Assert;

/**
 * A public metadata file that describes an agent's capabilities, skills, endpoint URL, and
 * authentication requirements. Clients use this for discovery.
 */
public record AgentCard(String name, String description, String url, AgentProvider provider,
                        String version, String documentationUrl, AgentCapabilities capabilities,
                        AgentAuthentication authentication, List<String> defaultInputModes,
                        List<String> defaultOutputModes, List<AgentSkill> skills) {

    private static final String TEXT_MODE = "text";

    public AgentCard {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotNullParam("url", url);
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("capabilities", capabilities);
        Assert.checkNotNullParam("skills", skills);
        defaultInputModes = defaultInputModes == null ? Collections.singletonList(TEXT_MODE) : defaultInputModes;
        defaultOutputModes = defaultOutputModes == null ? Collections.singletonList(TEXT_MODE) : defaultOutputModes;
    }
}
