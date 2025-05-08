package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * A fundamental data unit within a Message or Artifact.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataPart extends Part<Map<String, Object>> {

    private Map<String, Object> data;
    private Map<String, Object> metadata;
    private Type type;

    public DataPart(Map<String, Object> data) {
        this(data, null);
    }

    @JsonCreator
    public DataPart(@JsonProperty("data") Map<String, Object> data,
                    @JsonProperty("metadata") Map<String, Object> metadata) {
        Assert.checkNotNullParam("data", data);
        this.data = data;
        this.metadata = metadata;
        this.type = Type.DATA;
    }

    @Override
    public Type getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

}
