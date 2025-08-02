/**
 * Represents an HTTP authentication security scheme.
 * Converted from Java: io.a2a.spec.HTTPAuthSecurityScheme
 */
public class A2AHTTPAuthSecurityScheme extends A2ASecurityScheme {
    public static final String HTTP = 'http';
    
    public String bearerFormat { get; set; }
    public String scheme { get; set; }
    
    /**
     * Constructor with bearer format, scheme, and description
     */
    public A2AHTTPAuthSecurityScheme(String bearerFormat, String scheme, String description) {
        this(bearerFormat, scheme, description, HTTP);
    }
    
    /**
     * Constructor with all parameters
     */
    public A2AHTTPAuthSecurityScheme(String bearerFormat, String scheme, String description, String type) {
        A2AAssert.checkNotNullParam('scheme', scheme);
        
        if (!HTTP.equals(type)) {
            throw new A2AException('Invalid type for HTTPAuthSecurityScheme: ' + type);
        }
        
        this.bearerFormat = bearerFormat;
        this.scheme = scheme;
        this.description = description;
        this.type = type;
    }
    
    /**
     * Get the scheme type
     */
    public override String getSchemeType() {
        return 'HTTP';
    }
    
    /**
     * Serialize to Map
     */
    public override Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        if (bearerFormat != null) result.put('bearerFormat', bearerFormat);
        if (scheme != null) result.put('scheme', scheme);
        
        return result;
    }
    
    /**
     * Deserialize from Map
     */
    public static A2AHTTPAuthSecurityScheme deserialize(Map<String, Object> jsonMap) {
        String bearerFormat = (String) jsonMap.get('bearerFormat');
        String scheme = (String) jsonMap.get('scheme');
        String description = (String) jsonMap.get('description');
        String type = (String) jsonMap.get('type');
        
        return new A2AHTTPAuthSecurityScheme(bearerFormat, scheme, description, type != null ? type : HTTP);
    }
    
    /**
     * Builder class for fluent API
     */
    public class Builder {
        private String bearerFormat;
        private String scheme;
        private String description;
        
        public Builder withBearerFormat(String bearerFormat) {
            this.bearerFormat = bearerFormat;
            return this;
        }
        
        public Builder withScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }
        
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public A2AHTTPAuthSecurityScheme build() {
            return new A2AHTTPAuthSecurityScheme(bearerFormat, scheme, description);
        }
    }
}