/**
 * Placeholder for HTTP Auth Security Scheme
 * TODO: Implement based on Java io.a2a.spec.HTTPAuthSecurityScheme
 */
public class A2AHTTPAuthSecurityScheme extends A2ASecurityScheme {
    public String scheme { get; set; }
    public String bearerFormat { get; set; }
    
    public A2AHTTPAuthSecurityScheme() {
        super(TYPE_HTTP);
    }
    
    public override Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        result.put('type', type);
        if (scheme != null) result.put('scheme', scheme);
        if (bearerFormat != null) result.put('bearerFormat', bearerFormat);
        if (description != null) result.put('description', description);
        return result;
    }
    
    public static A2AHTTPAuthSecurityScheme deserialize(Map<String, Object> jsonMap) {
        A2AHTTPAuthSecurityScheme scheme = new A2AHTTPAuthSecurityScheme();
        scheme.scheme = (String) jsonMap.get('scheme');
        scheme.bearerFormat = (String) jsonMap.get('bearerFormat');
        scheme.description = (String) jsonMap.get('description');
        return scheme;
    }
}