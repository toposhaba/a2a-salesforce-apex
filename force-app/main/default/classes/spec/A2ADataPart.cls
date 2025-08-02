/**
 * A data part of a Message or Artifact
 * Placeholder implementation - TODO: Complete based on Java io.a2a.spec.DataPart
 */
public class A2ADataPart extends A2APart {
    public String mimeType { get; set; }
    public Object data { get; set; }
    
    public A2ADataPart() {
        super();
    }
    
    public override String getKind() {
        return KIND_DATA;
    }
    
    public override Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        result.put('kind', getKind());
        if (mimeType != null) result.put('mimeType', mimeType);
        if (data != null) result.put('data', data);
        if (metadata != null && !metadata.isEmpty()) {
            result.put('metadata', metadata);
        }
        return result;
    }
    
    public static A2ADataPart deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2ADataPart part = new A2ADataPart();
        part.mimeType = (String) jsonMap.get('mimeType');
        part.data = jsonMap.get('data');
        part.metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        return part;
    }
}