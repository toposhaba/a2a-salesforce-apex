/**
 * A file part of a Message or Artifact
 * Placeholder implementation - TODO: Complete based on Java io.a2a.spec.FilePart
 */
public class A2AFilePart extends A2APart {
    public String name { get; set; }
    public String mimeType { get; set; }
    public String uri { get; set; }
    public String data { get; set; } // Base64 encoded data
    
    public A2AFilePart() {
        super();
    }
    
    public override String getKind() {
        return KIND_FILE;
    }
    
    public override Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        result.put('kind', getKind());
        if (name != null) result.put('name', name);
        if (mimeType != null) result.put('mimeType', mimeType);
        if (uri != null) result.put('uri', uri);
        if (data != null) result.put('data', data);
        if (metadata != null && !metadata.isEmpty()) {
            result.put('metadata', metadata);
        }
        return result;
    }
    
    public static A2AFilePart deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AFilePart part = new A2AFilePart();
        part.name = (String) jsonMap.get('name');
        part.mimeType = (String) jsonMap.get('mimeType');
        part.uri = (String) jsonMap.get('uri');
        part.data = (String) jsonMap.get('data');
        part.metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        return part;
    }
}