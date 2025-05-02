package io.a2a.spec;

/**
 * Represents content within a FilePart.
 */
public record FileContent(String name, String mimeType, String bytes, String uri) {

    public FileContent {
        if (bytes == null && uri == null) {
            throw new IllegalArgumentException("Either 'bytes' or 'uri' must be present in the file data");
        }
        if (bytes != null && uri != null) {
            throw new IllegalArgumentException("Only one of 'bytes' or 'uri' can be present in the file data");
        }
    }
}

