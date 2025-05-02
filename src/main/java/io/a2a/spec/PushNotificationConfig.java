package io.a2a.spec;

import io.a2a.util.Assert;

/**
 * Represents a push notification.
 */
public record PushNotificationConfig(String url, String token, AuthenticationInfo authentication) {

    public PushNotificationConfig {
        Assert.checkNotNullParam("url", url);
    }
}
