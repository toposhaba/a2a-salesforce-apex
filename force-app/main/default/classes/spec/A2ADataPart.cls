/**
 * A fundamental data unit within a Message or Artifact.
 * Converted from Java: io.a2a.spec.DataPart
 */
public class A2ADataPart extends A2APart {
    public Map<String, Object> data { get; set; }
    public Map<String, Object> metadata { get; set; }
    
    /**
     * Constructor with data only
     */
    public A2ADataPart(Map<String, Object> data) {
        this(data, null);
    }
    
    /**
     * Constructor with data and metadata
     */
    public A2ADataPart(Map<String, Object> data, Map<String, Object> metadata) {
        A2AAssert.checkNotNullParam('data', data);
        this.data = data;
        this.metadata = metadata;
    }
    
    /**
     * Returns the type of this part
     */
    public override String getType() {
        return 'data';
    }
    
    /**
     * Serialize this part to a Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        result.put('type', 'data');
        result.put('data', this.data);
        
        if (this.metadata != null && !this.metadata.isEmpty()) {
            result.put('metadata', this.metadata);
        }
        
        return result;
    }
    
    /**
     * Deserialize from a Map
     */
    public static A2ADataPart deserialize(Map<String, Object> jsonMap) {
        Map<String, Object> data = (Map<String, Object>) jsonMap.get('data');
        Map<String, Object> metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        return new A2ADataPart(data, metadata);
    }
    
    /**
     * Check if this part has metadata
     */
    public Boolean hasMetadata() {
        return this.metadata != null && !this.metadata.isEmpty();
    }
    
    /**
     * Get a specific value from the data map
     */
    public Object getValue(String key) {
        return this.data != null ? this.data.get(key) : null;
    }
    
    /**
     * Add or update a value in the data map
     */
    public void putValue(String key, Object value) {
        if (this.data == null) {
            this.data = new Map<String, Object>();
        }
        this.data.put(key, value);
    }
}