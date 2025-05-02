package io.a2a.spec;

import io.a2a.util.Assert;

/**
 * Task push notification configuration.
 */
public record TaskPushNotificationConfig(String id, PushNotificationConfig pushNotificationConfig) {

    public TaskPushNotificationConfig {
        Assert.checkNotNullParam("id", id);
        Assert.checkNotNullParam("pushNotificationConfig", pushNotificationConfig);
    }
}
