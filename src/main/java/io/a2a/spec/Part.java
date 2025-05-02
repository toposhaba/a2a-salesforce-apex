package io.a2a.spec;

import java.util.Map;

/**
 * A fundamental unit with a Message or Artifact.
 * @param <T> the type of unit
 */
public interface Part<T> {
    enum Type {
        TEXT,
        FILE,
        DATA;
    }

    Type getType();

    T getPart();

    Map<String, Object> getMetadata();

}