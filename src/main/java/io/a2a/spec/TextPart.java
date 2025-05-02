package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * A fundamental text unit of an Artifact or Message.
 */
public class TextPart implements Part<String> {
    private String part;
    private Map<String, Object> metadata;

    @JsonCreator
    public TextPart(@JsonProperty("part") String part) {
        this(part, null);
    }

    @JsonCreator
    public TextPart(@JsonProperty("part") String part, @JsonProperty("metadata") Map<String, Object> metadata) {
        Assert.checkNotNullParam("part", part);
        this.part = part;
        this.metadata = metadata;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    @Override
    public String getPart() {
        return part;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

}