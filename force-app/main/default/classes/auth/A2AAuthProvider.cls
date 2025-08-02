/**
 * Authentication provider interface for A2A security
 * Supports multiple authentication mechanisms
 */
public interface A2AAuthProvider {
    
    /**
     * Get authentication headers for HTTP requests
     */
    Map<String, String> getAuthHeaders();
    
    /**
     * Validate if current authentication is valid
     */
    Boolean isValid();
    
    /**
     * Refresh authentication if supported
     */
    void refresh();
    
    /**
     * Get authentication type
     */
    String getAuthType();
}