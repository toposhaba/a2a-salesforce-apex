/**
 * A fundamental unit within a Message or Artifact
 * Converted from Java io.a2a.spec.Part
 */
public abstract class A2APart implements A2ASerializable {
    public Map<String, Object> metadata { get; set; }
    
    // Part type constants
    public static final String KIND_TEXT = 'text';
    public static final String KIND_FILE = 'file';
    public static final String KIND_DATA = 'data';
    
    public A2APart() {
        this.metadata = new Map<String, Object>();
    }
    
    /**
     * Get the kind/type of this part
     */
    public abstract String getKind();
    
    /**
     * Serialize the part to a map
     */
    public abstract override Map<String, Object> serialize();
    
    /**
     * Factory method to deserialize a Part based on its kind
     */
    public static A2APart deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        String kind = (String) jsonMap.get('kind');
        if (kind == null) {
            throw new A2AException('Part kind is required');
        }
        
        switch on kind {
            when 'text' {
                return A2ATextPart.deserialize(jsonMap);
            }
            when 'file' {
                return A2AFilePart.deserialize(jsonMap);
            }
            when 'data' {
                return A2ADataPart.deserialize(jsonMap);
            }
            when else {
                throw new A2AException('Unknown part kind: ' + kind);
            }
        }
    }
}