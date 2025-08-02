/**
 * Utility class for common A2A operations
 */
public class A2AUtils {
    
    /**
     * Generate a unique ID for tasks and contexts
     */
    public static String generateUniqueId(String prefix) {
        String timestamp = String.valueOf(DateTime.now().getTime());
        String randomPart = String.valueOf(Math.random()).substring(2, 8);
        return prefix + '_' + timestamp + '_' + randomPart;
    }
    
    /**
     * Generate a task ID
     */
    public static String generateTaskId() {
        return generateUniqueId('task');
    }
    
    /**
     * Generate a context ID
     */
    public static String generateContextId() {
        return generateUniqueId('ctx');
    }
    
    /**
     * Generate a message ID
     */
    public static String generateMessageId() {
        return generateUniqueId('msg');
    }
    
    /**
     * Safely parse JSON
     */
    public static Object parseJson(String jsonString) {
        if (String.isBlank(jsonString)) {
            return null;
        }
        
        try {
            return JSON.deserializeUntyped(jsonString);
        } catch (Exception e) {
            System.debug(LoggingLevel.ERROR, 'Failed to parse JSON: ' + e.getMessage());
            return null;
        }
    }
    
    /**
     * Safely serialize to JSON
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return JSON.serialize(obj);
        } catch (Exception e) {
            System.debug(LoggingLevel.ERROR, 'Failed to serialize to JSON: ' + e.getMessage());
            return null;
        }
    }
    
    /**
     * Pretty print JSON
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return JSON.serializePretty(obj);
        } catch (Exception e) {
            System.debug(LoggingLevel.ERROR, 'Failed to serialize to pretty JSON: ' + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if string is a valid URL
     */
    public static Boolean isValidUrl(String url) {
        if (String.isBlank(url)) {
            return false;
        }
        
        // Basic URL validation pattern
        Pattern urlPattern = Pattern.compile(
            '^(https?|ftp)://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$'
        );
        
        Matcher matcher = urlPattern.matcher(url);
        return matcher.matches();
    }
    
    /**
     * Truncate string to specified length
     */
    public static String truncate(String str, Integer maxLength) {
        if (String.isBlank(str) || maxLength <= 0) {
            return str;
        }
        
        if (str.length() <= maxLength) {
            return str;
        }
        
        return str.substring(0, maxLength);
    }
    
    /**
     * Convert map keys to lowercase
     */
    public static Map<String, Object> lowercaseKeys(Map<String, Object> inputMap) {
        if (inputMap == null) {
            return null;
        }
        
        Map<String, Object> result = new Map<String, Object>();
        for (String key : inputMap.keySet()) {
            result.put(key.toLowerCase(), inputMap.get(key));
        }
        
        return result;
    }
    
    /**
     * Merge two maps (second map overwrites first)
     */
    public static Map<String, Object> mergeMaps(Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> result = new Map<String, Object>();
        
        if (map1 != null) {
            result.putAll(map1);
        }
        
        if (map2 != null) {
            result.putAll(map2);
        }
        
        return result;
    }
    
    /**
     * Get nested value from map using dot notation
     */
    public static Object getNestedValue(Map<String, Object> inputMap, String path) {
        if (inputMap == null || String.isBlank(path)) {
            return null;
        }
        
        List<String> parts = path.split('\\.');
        Object current = inputMap;
        
        for (String part : parts) {
            if (current instanceof Map<String, Object>) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }
    
    /**
     * Set nested value in map using dot notation
     */
    public static void setNestedValue(Map<String, Object> inputMap, String path, Object value) {
        if (inputMap == null || String.isBlank(path)) {
            return;
        }
        
        List<String> parts = path.split('\\.');
        Map<String, Object> current = inputMap;
        
        for (Integer i = 0; i < parts.size() - 1; i++) {
            String part = parts[i];
            if (!current.containsKey(part) || !(current.get(part) instanceof Map<String, Object>)) {
                current.put(part, new Map<String, Object>());
            }
            current = (Map<String, Object>) current.get(part);
        }
        
        current.put(parts[parts.size() - 1], value);
    }
    
    /**
     * Convert object to string safely
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        
        return String.valueOf(obj);
    }
    
    /**
     * Parse boolean value from various inputs
     */
    public static Boolean toBoolean(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        
        String str = toString(obj).toLowerCase();
        return str == 'true' || str == '1' || str == 'yes' || str == 'y';
    }
    
    /**
     * Create a timestamp string
     */
    public static String getTimestamp() {
        return DateTime.now().format('yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'');
    }
    
    /**
     * Check if running in sandbox
     */
    public static Boolean isSandbox() {
        return [SELECT IsSandbox FROM Organization LIMIT 1].IsSandbox;
    }
    
    /**
     * Get org base URL
     */
    public static String getOrgBaseUrl() {
        return URL.getOrgDomainUrl().toExternalForm();
    }
    
    /**
     * Chunk a list into smaller lists
     */
    public static List<List<Object>> chunkList(List<Object> inputList, Integer chunkSize) {
        List<List<Object>> chunks = new List<List<Object>>();
        
        if (inputList == null || inputList.isEmpty() || chunkSize <= 0) {
            return chunks;
        }
        
        List<Object> currentChunk = new List<Object>();
        
        for (Object item : inputList) {
            currentChunk.add(item);
            
            if (currentChunk.size() >= chunkSize) {
                chunks.add(currentChunk);
                currentChunk = new List<Object>();
            }
        }
        
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk);
        }
        
        return chunks;
    }
}