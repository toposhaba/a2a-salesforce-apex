/**
 * A fundamental file unit within a Message or Artifact.
 * Converted from Java: io.a2a.spec.FilePart
 */
public class A2AFilePart extends A2APart {
    public A2AFileContent file { get; set; }
    public Map<String, Object> metadata { get; set; }
    
    /**
     * Constructor with file content only
     */
    public A2AFilePart(A2AFileContent file) {
        this(file, null);
    }
    
    /**
     * Constructor with file content and metadata
     */
    public A2AFilePart(A2AFileContent file, Map<String, Object> metadata) {
        A2AAssert.checkNotNullParam('file', file);
        this.file = file;
        this.metadata = metadata;
    }
    
    /**
     * Returns the type of this part
     */
    public override String getType() {
        return 'file';
    }
    
    /**
     * Serialize this part to a Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        result.put('type', 'file');
        
        if (this.file != null) {
            result.put('file', this.file.serialize());
        }
        
        if (this.metadata != null && !this.metadata.isEmpty()) {
            result.put('metadata', this.metadata);
        }
        
        return result;
    }
    
    /**
     * Deserialize from a Map
     */
    public static A2AFilePart deserialize(Map<String, Object> jsonMap) {
        Map<String, Object> fileMap = (Map<String, Object>) jsonMap.get('file');
        Map<String, Object> metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        A2AFileContent fileContent = null;
        if (fileMap != null) {
            // Determine file content type
            if (fileMap.containsKey('bytes')) {
                fileContent = A2AFileWithBytes.deserialize(fileMap);
            } else if (fileMap.containsKey('uri')) {
                fileContent = A2AFileWithUri.deserialize(fileMap);
            }
        }
        
        return fileContent != null ? new A2AFilePart(fileContent, metadata) : null;
    }
    
    /**
     * Check if this part has metadata
     */
    public Boolean hasMetadata() {
        return this.metadata != null && !this.metadata.isEmpty();
    }
    
    /**
     * Get the file name
     */
    public String getFileName() {
        return this.file != null ? this.file.name : null;
    }
    
    /**
     * Get the MIME type
     */
    public String getMimeType() {
        return this.file != null ? this.file.mimeType : null;
    }
    
    /**
     * Check if this is a bytes-based file
     */
    public Boolean isFileWithBytes() {
        return this.file != null && this.file instanceof A2AFileWithBytes;
    }
    
    /**
     * Check if this is a URI-based file
     */
    public Boolean isFileWithUri() {
        return this.file != null && this.file instanceof A2AFileWithUri;
    }
}