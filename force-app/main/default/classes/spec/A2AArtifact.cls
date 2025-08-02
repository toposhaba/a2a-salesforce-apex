/**
 * Represents an artifact produced by a task
 * Converted from Java: io.a2a.spec.Artifact
 */
public class A2AArtifact implements A2ASerializable {
    
    public String id { get; set; }
    public String mimeType { get; set; }
    public String title { get; set; }
    public String url { get; set; }
    public List<A2APart> parts { get; set; }
    public Map<String, Object> metadata { get; set; }
    public DateTime createdAt { get; set; }
    
    /**
     * Default constructor
     */
    public A2AArtifact() {
        this.parts = new List<A2APart>();
        this.metadata = new Map<String, Object>();
        this.id = A2AUtils.generateUniqueId('artifact');
        this.createdAt = DateTime.now();
    }
    
    /**
     * Constructor with parts
     */
    public A2AArtifact(List<A2APart> parts) {
        this();
        this.parts = parts != null ? parts : new List<A2APart>();
    }
    
    /**
     * Constructor with all fields
     */
    public A2AArtifact(String id, String mimeType, String title, String url, List<A2APart> parts) {
        this.id = id;
        this.mimeType = mimeType;
        this.title = title;
        this.url = url;
        this.parts = parts != null ? parts : new List<A2APart>();
        this.metadata = new Map<String, Object>();
        this.createdAt = DateTime.now();
    }
    
    /**
     * Serialize to Map
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        if (id != null) result.put('id', id);
        if (mimeType != null) result.put('mimeType', mimeType);
        if (title != null) result.put('title', title);
        if (url != null) result.put('url', url);
        
        if (parts != null && !parts.isEmpty()) {
            List<Map<String, Object>> partsList = new List<Map<String, Object>>();
            for (A2APart part : parts) {
                partsList.add(part.serialize());
            }
            result.put('parts', partsList);
        }
        
        if (metadata != null && !metadata.isEmpty()) {
            result.put('metadata', metadata);
        }
        
        if (createdAt != null) {
            result.put('createdAt', createdAt.format());
        }
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AArtifact deserialize(Map<String, Object> jsonMap) {
        A2AArtifact artifact = new A2AArtifact();
        
        artifact.id = (String) jsonMap.get('id');
        artifact.mimeType = (String) jsonMap.get('mimeType');
        artifact.title = (String) jsonMap.get('title');
        artifact.url = (String) jsonMap.get('url');
        artifact.metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        // Deserialize parts
        List<Object> partsList = (List<Object>) jsonMap.get('parts');
        if (partsList != null) {
            artifact.parts = new List<A2APart>();
            for (Object partObj : partsList) {
                Map<String, Object> partMap = (Map<String, Object>) partObj;
                A2APart part = A2APart.deserialize(partMap);
                if (part != null) {
                    artifact.parts.add(part);
                }
            }
        }
        
        // Parse date
        String createdAtStr = (String) jsonMap.get('createdAt');
        if (createdAtStr != null) {
            try {
                artifact.createdAt = DateTime.parse(createdAtStr);
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
        
        return artifact;
    }
    
    /**
     * Builder class
     */
    public class Builder {
        private A2AArtifact artifact;
        
        public Builder() {
            this.artifact = new A2AArtifact();
        }
        
        public Builder withId(String id) {
            artifact.id = id;
            return this;
        }
        
        public Builder withMimeType(String mimeType) {
            artifact.mimeType = mimeType;
            return this;
        }
        
        public Builder withTitle(String title) {
            artifact.title = title;
            return this;
        }
        
        public Builder withUrl(String url) {
            artifact.url = url;
            return this;
        }
        
        public Builder withParts(List<A2APart> parts) {
            artifact.parts = parts != null ? parts : new List<A2APart>();
            return this;
        }
        
        public Builder withPart(A2APart part) {
            if (part != null) {
                artifact.parts.add(part);
            }
            return this;
        }
        
        public Builder withMetadata(Map<String, Object> metadata) {
            artifact.metadata = metadata != null ? metadata : new Map<String, Object>();
            return this;
        }
        
        public A2AArtifact build() {
            return artifact;
        }
    }
}