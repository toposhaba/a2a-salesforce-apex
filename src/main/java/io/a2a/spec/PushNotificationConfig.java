package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.a2a.util.Assert;

/**
 * Represents a push notification.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PushNotificationConfig(String url, String token, PushNotificationAuthenticationInfo authentication) {

    public PushNotificationConfig {
        Assert.checkNotNullParam("url", url);
    }

    public static class Builder {
        private String url;
        private String token;
        private PushNotificationAuthenticationInfo authentication;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder authenticationInfo(PushNotificationAuthenticationInfo authenticationInfo) {
            this.authentication = authenticationInfo;
            return this;
        }

        public PushNotificationConfig build() {
            return new PushNotificationConfig(url, token, authentication);
        }
    }
}
