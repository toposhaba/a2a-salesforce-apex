/**
 * File content with URI reference
 * Converted from Java: io.a2a.spec.FileWithUri
 */
public class A2AFileWithUri extends A2AFileContent {
    public String uri { get; set; }
    
    /**
     * Default constructor
     */
    public A2AFileWithUri() {
        super();
    }
    
    /**
     * Constructor with all parameters
     */
    public A2AFileWithUri(String mimeType, String name, String uri) {
        this.mimeType = mimeType;
        this.name = name;
        this.uri = uri;
    }
    
    /**
     * Get the content type
     */
    public override String getContentType() {
        return 'uri';
    }
    
    /**
     * Serialize to Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        if (uri != null) result.put('uri', uri);
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AFileWithUri deserialize(Map<String, Object> jsonMap) {
        A2AFileWithUri file = new A2AFileWithUri();
        file.mimeType = (String) jsonMap.get('mimeType');
        file.name = (String) jsonMap.get('name');
        file.uri = (String) jsonMap.get('uri');
        return file;
    }
    
    /**
     * Check if URI is a valid URL
     */
    public Boolean isValidUrl() {
        if (String.isBlank(uri)) return false;
        
        try {
            // Basic URL validation in Apex
            return uri.startsWith('http://') || uri.startsWith('https://') || 
                   uri.startsWith('ftp://') || uri.startsWith('file://');
        } catch (Exception e) {
            return false;
        }
    }
}