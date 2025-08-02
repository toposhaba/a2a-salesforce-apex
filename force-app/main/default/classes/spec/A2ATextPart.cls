/**
 * A fundamental text unit of an Artifact or Message
 * Converted from Java io.a2a.spec.TextPart
 */
public class A2ATextPart extends A2APart {
    public String text { get; set; }
    
    public A2ATextPart() {
        super();
    }
    
    public A2ATextPart(String text) {
        this(text, null);
    }
    
    public A2ATextPart(String text, Map<String, Object> metadata) {
        super();
        A2AAssert.checkNotNullParam('text', text);
        this.text = text;
        if (metadata != null) {
            this.metadata = metadata;
        }
    }
    
    public override String getKind() {
        return KIND_TEXT;
    }
    
    public override Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        result.put('kind', getKind());
        result.put('text', text);
        if (metadata != null && !metadata.isEmpty()) {
            result.put('metadata', metadata);
        }
        return result;
    }
    
    public static A2ATextPart deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        String text = (String) jsonMap.get('text');
        Map<String, Object> metadata = (Map<String, Object>) jsonMap.get('metadata');
        
        return new A2ATextPart(text, metadata);
    }
}