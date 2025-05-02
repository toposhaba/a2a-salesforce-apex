package io.a2a.spec;

/**
 * An agent's capabilities.
 */
public record AgentCapabilities(boolean streaming, boolean pushNotifications, boolean stateTransitionHistory) {
}
