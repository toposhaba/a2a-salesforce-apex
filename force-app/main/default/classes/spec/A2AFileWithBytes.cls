/**
 * File content with base64-encoded bytes
 * Converted from Java: io.a2a.spec.FileWithBytes
 */
public class A2AFileWithBytes extends A2AFileContent {
    public String bytes { get; set; }
    
    /**
     * Default constructor
     */
    public A2AFileWithBytes() {
        super();
    }
    
    /**
     * Constructor with all parameters
     */
    public A2AFileWithBytes(String mimeType, String name, String bytes) {
        this.mimeType = mimeType;
        this.name = name;
        this.bytes = bytes;
    }
    
    /**
     * Get the content type
     */
    public override String getContentType() {
        return 'bytes';
    }
    
    /**
     * Serialize to Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        if (bytes != null) result.put('bytes', bytes);
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AFileWithBytes deserialize(Map<String, Object> jsonMap) {
        A2AFileWithBytes file = new A2AFileWithBytes();
        file.mimeType = (String) jsonMap.get('mimeType');
        file.name = (String) jsonMap.get('name');
        file.bytes = (String) jsonMap.get('bytes');
        return file;
    }
    
    /**
     * Convert bytes to Blob
     */
    public Blob getBlob() {
        return bytes != null ? EncodingUtil.base64Decode(bytes) : null;
    }
    
    /**
     * Set Blob content
     */
    public void setBlob(Blob content) {
        this.bytes = content != null ? EncodingUtil.base64Encode(content) : null;
    }
}