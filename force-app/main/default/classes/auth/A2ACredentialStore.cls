/**
 * Secure credential storage for A2A authentication
 * Uses Custom Metadata Types for configuration and Protected Custom Settings for secrets
 */
public with sharing class A2ACredentialStore {
    
    /**
     * Credential types
     */
    public enum CredentialType {
        API_KEY,
        BEARER_TOKEN,
        OAUTH2_CLIENT,
        BASIC_AUTH
    }
    
    /**
     * Credential configuration
     */
    public class CredentialConfig {
        public String name { get; set; }
        public CredentialType type { get; set; }
        public String endpoint { get; set; }
        public Map<String, String> metadata { get; set; }
        public Boolean isActive { get; set; }
        
        public CredentialConfig() {
            this.metadata = new Map<String, String>();
            this.isActive = true;
        }
    }
    
    /**
     * Get credential by name
     */
    public static A2AAuthProvider getCredential(String credentialName) {
        if (String.isBlank(credentialName)) {
            throw new A2AException('Credential name cannot be blank');
        }
        
        try {
            // First check Custom Metadata for configuration
            A2A_Credential__mdt credConfig = getCredentialConfig(credentialName);
            
            if (credConfig == null) {
                throw new A2AException('Credential configuration not found: ' + credentialName);
            }
            
            if (!credConfig.Is_Active__c) {
                throw new A2AException('Credential is not active: ' + credentialName);
            }
            
            // Get the actual credential value from Protected Custom Setting
            String credentialValue = getProtectedCredentialValue(credentialName);
            
            if (String.isBlank(credentialValue)) {
                throw new A2AException('Credential value not found: ' + credentialName);
            }
            
            // Create appropriate auth provider based on type
            return createAuthProvider(credConfig, credentialValue);
            
        } catch (Exception e) {
            A2ALogger.error('CREDENTIAL_STORE', 'Failed to retrieve credential: ' + credentialName, e);
            throw new A2AException('Failed to retrieve credential: ' + e.getMessage(), e);
        }
    }
    
    /**
     * Store credential (for admin use)
     */
    public static void storeCredential(String credentialName, String credentialValue) {
        if (!Schema.sObjectType.A2A_Protected_Credential__c.isCreateable()) {
            throw new A2AException('Insufficient permissions to store credentials');
        }
        
        try {
            // Check if credential already exists
            A2A_Protected_Credential__c existingCred = A2A_Protected_Credential__c.getInstance(credentialName);
            
            if (existingCred != null && existingCred.Id != null) {
                // Update existing
                existingCred.Credential_Value__c = credentialValue;
                existingCred.Last_Modified__c = DateTime.now();
                update existingCred;
            } else {
                // Create new
                A2A_Protected_Credential__c newCred = new A2A_Protected_Credential__c();
                newCred.Name = credentialName;
                newCred.Credential_Value__c = credentialValue;
                newCred.Last_Modified__c = DateTime.now();
                insert newCred;
            }
            
            A2ALogger.info('CREDENTIAL_STORE', 'Credential stored successfully', new Map<String, Object>{
                'credentialName' => credentialName
            });
            
        } catch (Exception e) {
            A2ALogger.error('CREDENTIAL_STORE', 'Failed to store credential', e);
            throw new A2AException('Failed to store credential: ' + e.getMessage(), e);
        }
    }
    
    /**
     * List available credentials (metadata only, no values)
     */
    public static List<CredentialConfig> listCredentials() {
        List<CredentialConfig> configs = new List<CredentialConfig>();
        
        try {
            List<A2A_Credential__mdt> credMetadata = [
                SELECT DeveloperName, Label, Credential_Type__c, 
                       Endpoint__c, Is_Active__c, Additional_Config__c
                FROM A2A_Credential__mdt
                WHERE Is_Active__c = true
                ORDER BY Label
            ];
            
            for (A2A_Credential__mdt cred : credMetadata) {
                CredentialConfig config = new CredentialConfig();
                config.name = cred.DeveloperName;
                config.type = parseCredentialType(cred.Credential_Type__c);
                config.endpoint = cred.Endpoint__c;
                config.isActive = cred.Is_Active__c;
                
                if (String.isNotBlank(cred.Additional_Config__c)) {
                    config.metadata = (Map<String, String>) JSON.deserialize(
                        cred.Additional_Config__c, 
                        Map<String, String>.class
                    );
                }
                
                configs.add(config);
            }
            
        } catch (Exception e) {
            A2ALogger.error('CREDENTIAL_STORE', 'Failed to list credentials', e);
        }
        
        return configs;
    }
    
    /**
     * Get credential configuration from Custom Metadata
     */
    private static A2A_Credential__mdt getCredentialConfig(String credentialName) {
        List<A2A_Credential__mdt> credentials = [
            SELECT DeveloperName, Label, Credential_Type__c, 
                   Endpoint__c, Is_Active__c, Additional_Config__c
            FROM A2A_Credential__mdt
            WHERE DeveloperName = :credentialName
            LIMIT 1
        ];
        
        return credentials.isEmpty() ? null : credentials[0];
    }
    
    /**
     * Get protected credential value
     */
    private static String getProtectedCredentialValue(String credentialName) {
        A2A_Protected_Credential__c protectedCred = A2A_Protected_Credential__c.getInstance(credentialName);
        return protectedCred != null ? protectedCred.Credential_Value__c : null;
    }
    
    /**
     * Create auth provider based on configuration
     */
    private static A2AAuthProvider createAuthProvider(A2A_Credential__mdt config, String credentialValue) {
        CredentialType credType = parseCredentialType(config.Credential_Type__c);
        
        switch on credType {
            when API_KEY {
                // Parse API key configuration
                Map<String, String> apiKeyConfig = parseAdditionalConfig(config.Additional_Config__c);
                String keyName = apiKeyConfig.get('keyName') != null ? 
                    apiKeyConfig.get('keyName') : 'X-API-Key';
                String location = apiKeyConfig.get('location');
                
                if ('query'.equalsIgnoreCase(location)) {
                    return new A2AApiKeyAuth(credentialValue, keyName, A2AApiKeyAuth.ApiKeyLocation.QUERY_PARAM);
                } else {
                    return new A2AApiKeyAuth(credentialValue, keyName);
                }
            }
            when BEARER_TOKEN {
                // Check if refresh is configured
                Map<String, String> tokenConfig = parseAdditionalConfig(config.Additional_Config__c);
                String refreshToken = tokenConfig.get('refreshToken');
                
                if (String.isNotBlank(refreshToken) && String.isNotBlank(config.Endpoint__c)) {
                    A2ABearerTokenAuth auth = new A2ABearerTokenAuth(
                        credentialValue, 
                        refreshToken, 
                        config.Endpoint__c
                    );
                    
                    // Set additional refresh params if any
                    tokenConfig.remove('refreshToken');
                    if (!tokenConfig.isEmpty()) {
                        auth.setRefreshParams(tokenConfig);
                    }
                    
                    return auth;
                } else {
                    return new A2ABearerTokenAuth(credentialValue);
                }
            }
            when OAUTH2_CLIENT {
                // OAuth2 implementation would go here
                throw new A2AException('OAuth2 client credentials not yet implemented');
            }
            when BASIC_AUTH {
                // Basic auth implementation would go here
                throw new A2AException('Basic authentication not yet implemented');
            }
            when else {
                throw new A2AException('Unsupported credential type: ' + config.Credential_Type__c);
            }
        }
    }
    
    /**
     * Parse credential type from string
     */
    private static CredentialType parseCredentialType(String typeStr) {
        if (String.isBlank(typeStr)) {
            return CredentialType.API_KEY;
        }
        
        try {
            return CredentialType.valueOf(typeStr.toUpperCase().replace(' ', '_'));
        } catch (Exception e) {
            A2ALogger.warn('CREDENTIAL_STORE', 'Unknown credential type: ' + typeStr);
            return CredentialType.API_KEY;
        }
    }
    
    /**
     * Parse additional configuration JSON
     */
    private static Map<String, String> parseAdditionalConfig(String configJson) {
        if (String.isBlank(configJson)) {
            return new Map<String, String>();
        }
        
        try {
            return (Map<String, String>) JSON.deserialize(configJson, Map<String, String>.class);
        } catch (Exception e) {
            A2ALogger.warn('CREDENTIAL_STORE', 'Failed to parse additional config: ' + e.getMessage());
            return new Map<String, String>();
        }
    }
}