/**
 * Utility class for parameter validation and assertions
 * Converted from Java io.a2a.util.Assert
 */
public class A2AAssert {
    
    /**
     * Check that the named parameter is not null. Use a standard exception message if it is.
     * @param name the parameter name
     * @param value the parameter value
     * @return the value that was passed in
     * @throws A2AException if the value is null
     */
    public static Object checkNotNullParam(String name, Object value) {
        checkNotNullParamChecked('name', name);
        checkNotNullParamChecked(name, value);
        return value;
    }
    
    /**
     * Check that the named String parameter is not null
     */
    public static String checkNotNullParam(String name, String value) {
        checkNotNullParamChecked('name', name);
        checkNotNullParamChecked(name, value);
        return value;
    }
    
    /**
     * Check that the named List parameter is not null
     */
    public static List<Object> checkNotNullParam(String name, List<Object> value) {
        checkNotNullParamChecked('name', name);
        checkNotNullParamChecked(name, value);
        return value;
    }
    
    /**
     * Check that the named Map parameter is not null
     */
    public static Map<String, Object> checkNotNullParam(String name, Map<String, Object> value) {
        checkNotNullParamChecked('name', name);
        checkNotNullParamChecked(name, value);
        return value;
    }
    
    private static void checkNotNullParamChecked(String name, Object value) {
        if (value == null) {
            throw new A2AException('Parameter \'' + name + '\' may not be null');
        }
    }
    
    /**
     * Validates that value is null, String, or Integer
     */
    public static void isNullOrStringOrInteger(Object value) {
        if (!(value == null || value instanceof String || value instanceof Integer)) {
            throw new A2AException('Id must be null, a String, or an Integer');
        }
    }
}