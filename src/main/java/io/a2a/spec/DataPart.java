package io.a2a.spec;

import java.util.Map;

import io.a2a.util.Assert;

/**
 * A fundamental data unit within a Message or Artifact.
 */
public class DataPart implements Part<Map<String, Object>> {

    private Map<String, Object> part;
    private Map<String, Object> metadata;

    public DataPart(Map<String, Object> part) {
        this(part, null);
    }

    public DataPart(Map<String, Object> part, Map<String, Object> metadata) {
        Assert.checkNotNullParam("part", part);
        this.part = part;
        this.metadata = metadata;
    }

    @Override
    public Type getType() {
        return Type.DATA;
    }

    @Override
    public Map<String, Object> getPart() {
        return part;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

}
