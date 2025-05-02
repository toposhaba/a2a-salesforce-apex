package io.a2a.spec;

import java.util.Map;

import io.a2a.util.Assert;

/**
 * Task query parameters.
 */
public record TaskQueryParams(String id, Map<String, Object> metadata, int historyLength) {

    public TaskQueryParams {
        Assert.checkNotNullParam("id", id);
    }
}
