package io.a2a.spec;

import java.util.Map;

import io.a2a.util.Assert;

/**
 * Task id parameters.
 */
public record TaskIdParams(String id, Map<String, Object> metadata) {

    public TaskIdParams {
        Assert.checkNotNullParam("id", id);
    }
}
