package io.a2a.spec;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.a2a.util.Assert;

/**
 * A fundamental file unit within a Message or Artifact.
 */
public class FilePart extends Part<FileContent> {

    private FileContent file;
    private Map<String, Object> metadata;
    private Type type;

    public FilePart(FileContent file) {
        this(file, null);
    }

    @JsonCreator
    public FilePart(@JsonProperty("file") FileContent file, @JsonProperty("metadata") Map<String, Object> metadata) {
        Assert.checkNotNullParam("file", file);
        this.file = file;
        this.metadata = metadata;
        this.type = Type.FILE;
    }

    @Override
    public Type getType() {
        return type;
    }

    public FileContent getFile() {
        return file;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

}