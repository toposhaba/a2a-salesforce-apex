package io.a2a.spec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents content within a FilePart.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
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

