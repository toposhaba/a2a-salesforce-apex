/**
 * Interface for objects that can be serialized to JSON
 * Replaces Jackson annotations functionality
 */
public interface A2ASerializable {
    /**
     * Serialize the object to a Map representation
     * @return Map representation of the object
     */
    Map<String, Object> serialize();
}