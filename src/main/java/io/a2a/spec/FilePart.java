package io.a2a.spec;

import java.util.Map;

import io.a2a.util.Assert;

/**
 * A fundamental file unit within a Message or Artifact.
 */
public class FilePart implements Part<FileContent> {

    private FileContent part;
    private Map<String, Object> metadata;

    public FilePart(FileContent part) {
        this(part, null);
    }

    public FilePart(FileContent part, Map<String, Object> metadata) {
        Assert.checkNotNullParam("part", part);
        this.part = part;
        this.metadata = metadata;
    }

    @Override
    public Type getType() {
        return Type.FILE;
    }

    @Override
    public FileContent getPart() {
        return part;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

}