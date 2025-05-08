package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * Represents a push notification.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PushNotificationConfig(String url, String token, AuthenticationInfo authentication) {

    public PushNotificationConfig {
        Assert.checkNotNullParam("url", url);
    }
}
