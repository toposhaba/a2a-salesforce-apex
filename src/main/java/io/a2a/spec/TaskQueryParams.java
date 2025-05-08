package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.a2a.util.Assert;

/**
 * Task query parameters.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskQueryParams(String id, Map<String, Object> metadata, Integer historyLength) {

    public TaskQueryParams {
        Assert.checkNotNullParam("id", id);
    }
}
