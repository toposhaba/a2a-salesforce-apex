/**
 * Represents an API Key security scheme
 * Converted from Java io.a2a.spec.APIKeySecurityScheme
 */
public class A2AAPIKeySecurityScheme extends A2ASecurityScheme {
    public String name { get; set; }
    public String location { get; set; } // 'cookie', 'header', or 'query'
    
    // Location constants
    public static final String LOCATION_COOKIE = 'cookie';
    public static final String LOCATION_HEADER = 'header';
    public static final String LOCATION_QUERY = 'query';
    
    public A2AAPIKeySecurityScheme() {
        super(TYPE_API_KEY);
    }
    
    public A2AAPIKeySecurityScheme(String name, String location, String description) {
        super(TYPE_API_KEY);
        A2AAssert.checkNotNullParam('name', name);
        A2AAssert.checkNotNullParam('location', location);
        
        this.name = name;
        this.location = location;
        this.description = description;
    }
    
    public override Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        result.put('type', type);
        if (name != null) result.put('name', name);
        if (location != null) result.put('in', location);
        if (description != null) result.put('description', description);
        return result;
    }
    
    public static A2AAPIKeySecurityScheme deserialize(Map<String, Object> jsonMap) {
        A2AAPIKeySecurityScheme scheme = new A2AAPIKeySecurityScheme();
        scheme.name = (String) jsonMap.get('name');
        scheme.location = (String) jsonMap.get('in');
        scheme.description = (String) jsonMap.get('description');
        return scheme;
    }
}