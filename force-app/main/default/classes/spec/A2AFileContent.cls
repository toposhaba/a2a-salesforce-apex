/**
 * Abstract base class for file content in FilePart
 * Converted from Java: io.a2a.spec.FileContent
 */
public abstract class A2AFileContent implements A2ASerializable {
    public String mimeType { get; set; }
    public String name { get; set; }
    
    /**
     * Get the type of file content (bytes or uri)
     */
    public abstract String getContentType();
    
    /**
     * Serialize to a Map
     */
    public virtual Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        if (mimeType != null) result.put('mimeType', mimeType);
        if (name != null) result.put('name', name);
        return result;
    }
}