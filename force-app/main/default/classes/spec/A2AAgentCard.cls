/**
 * A public metadata file that describes an agent's capabilities, skills, endpoint URL, and
 * authentication requirements. Clients use this for discovery.
 * Converted from Java io.a2a.spec.AgentCard
 */
public class A2AAgentCard implements A2ASerializable {
    public String name { get; set; }
    public String description { get; set; }
    public String url { get; set; }
    public A2AAgentProvider provider { get; set; }
    public String version { get; set; }
    public String documentationUrl { get; set; }
    public A2AAgentCapabilities capabilities { get; set; }
    public List<String> defaultInputModes { get; set; }
    public List<String> defaultOutputModes { get; set; }
    public List<A2AAgentSkill> skills { get; set; }
    public Boolean supportsAuthenticatedExtendedCard { get; set; }
    public Map<String, A2ASecurityScheme> securitySchemes { get; set; }
    public List<Map<String, List<String>>> security { get; set; }
    public String iconUrl { get; set; }
    public List<A2AAgentInterface> additionalInterfaces { get; set; }
    public String preferredTransport { get; set; }
    public String protocolVersion { get; set; }
    
    private static final String TEXT_MODE = 'text';
    
    public A2AAgentCard() {
        // Initialize collections
        this.defaultInputModes = new List<String>();
        this.defaultOutputModes = new List<String>();
        this.skills = new List<A2AAgentSkill>();
        this.securitySchemes = new Map<String, A2ASecurityScheme>();
        this.security = new List<Map<String, List<String>>>();
        this.additionalInterfaces = new List<A2AAgentInterface>();
    }
    
    public A2AAgentCard(String name, String description, String url, A2AAgentProvider provider,
                       String version, String documentationUrl, A2AAgentCapabilities capabilities,
                       List<String> defaultInputModes, List<String> defaultOutputModes, List<A2AAgentSkill> skills,
                       Boolean supportsAuthenticatedExtendedCard, Map<String, A2ASecurityScheme> securitySchemes,
                       List<Map<String, List<String>>> security, String iconUrl, List<A2AAgentInterface> additionalInterfaces,
                       String preferredTransport, String protocolVersion) {
        // Validate required fields
        A2AAssert.checkNotNullParam('capabilities', capabilities);
        A2AAssert.checkNotNullParam('defaultInputModes', defaultInputModes);
        A2AAssert.checkNotNullParam('defaultOutputModes', defaultOutputModes);
        A2AAssert.checkNotNullParam('description', description);
        A2AAssert.checkNotNullParam('name', name);
        A2AAssert.checkNotNullParam('skills', skills);
        A2AAssert.checkNotNullParam('url', url);
        A2AAssert.checkNotNullParam('version', version);
        A2AAssert.checkNotNullParam('protocolVersion', protocolVersion);
        
        this.name = name;
        this.description = description;
        this.url = url;
        this.provider = provider;
        this.version = version;
        this.documentationUrl = documentationUrl;
        this.capabilities = capabilities;
        this.defaultInputModes = defaultInputModes;
        this.defaultOutputModes = defaultOutputModes;
        this.skills = skills;
        this.supportsAuthenticatedExtendedCard = supportsAuthenticatedExtendedCard;
        this.securitySchemes = securitySchemes;
        this.security = security;
        this.iconUrl = iconUrl;
        this.additionalInterfaces = additionalInterfaces;
        this.preferredTransport = preferredTransport;
        this.protocolVersion = protocolVersion;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        
        // Required fields
        result.put('name', name);
        result.put('description', description);
        result.put('url', url);
        result.put('version', version);
        result.put('protocolVersion', protocolVersion);
        
        // Capabilities
        if (capabilities != null) {
            result.put('capabilities', capabilities.serialize());
        }
        
        // Lists
        if (defaultInputModes != null && !defaultInputModes.isEmpty()) {
            result.put('defaultInputModes', defaultInputModes);
        }
        if (defaultOutputModes != null && !defaultOutputModes.isEmpty()) {
            result.put('defaultOutputModes', defaultOutputModes);
        }
        
        // Skills
        if (skills != null && !skills.isEmpty()) {
            List<Map<String, Object>> skillsList = new List<Map<String, Object>>();
            for (A2AAgentSkill skill : skills) {
                skillsList.add(skill.serialize());
            }
            result.put('skills', skillsList);
        }
        
        // Optional fields
        if (provider != null) {
            result.put('provider', provider.serialize());
        }
        if (documentationUrl != null) {
            result.put('documentationUrl', documentationUrl);
        }
        if (supportsAuthenticatedExtendedCard != null) {
            result.put('supportsAuthenticatedExtendedCard', supportsAuthenticatedExtendedCard);
        }
        if (iconUrl != null) {
            result.put('iconUrl', iconUrl);
        }
        if (preferredTransport != null) {
            result.put('preferredTransport', preferredTransport);
        }
        
        // Security schemes
        if (securitySchemes != null && !securitySchemes.isEmpty()) {
            Map<String, Object> schemesMap = new Map<String, Object>();
            for (String key : securitySchemes.keySet()) {
                schemesMap.put(key, securitySchemes.get(key).serialize());
            }
            result.put('securitySchemes', schemesMap);
        }
        
        // Security requirements
        if (security != null && !security.isEmpty()) {
            result.put('security', security);
        }
        
        // Additional interfaces
        if (additionalInterfaces != null && !additionalInterfaces.isEmpty()) {
            List<Map<String, Object>> interfacesList = new List<Map<String, Object>>();
            for (A2AAgentInterface agentInterface : additionalInterfaces) {
                interfacesList.add(agentInterface.serialize());
            }
            result.put('additionalInterfaces', interfacesList);
        }
        
        return result;
    }
    
    public static A2AAgentCard deserialize(String jsonString) {
        Map<String, Object> jsonMap = (Map<String, Object>) JSON.deserializeUntyped(jsonString);
        return deserialize(jsonMap);
    }
    
    public static A2AAgentCard deserialize(Map<String, Object> jsonMap) {
        if (jsonMap == null) return null;
        
        A2AAgentCard card = new A2AAgentCard();
        
        // Basic fields
        card.name = (String) jsonMap.get('name');
        card.description = (String) jsonMap.get('description');
        card.url = (String) jsonMap.get('url');
        card.version = (String) jsonMap.get('version');
        card.protocolVersion = (String) jsonMap.get('protocolVersion');
        card.documentationUrl = (String) jsonMap.get('documentationUrl');
        card.iconUrl = (String) jsonMap.get('iconUrl');
        card.preferredTransport = (String) jsonMap.get('preferredTransport');
        card.supportsAuthenticatedExtendedCard = (Boolean) jsonMap.get('supportsAuthenticatedExtendedCard');
        
        // Provider
        Map<String, Object> providerMap = (Map<String, Object>) jsonMap.get('provider');
        if (providerMap != null) {
            card.provider = A2AAgentProvider.deserialize(providerMap);
        }
        
        // Capabilities
        Map<String, Object> capabilitiesMap = (Map<String, Object>) jsonMap.get('capabilities');
        if (capabilitiesMap != null) {
            card.capabilities = A2AAgentCapabilities.deserialize(capabilitiesMap);
        }
        
        // Lists
        List<Object> inputModes = (List<Object>) jsonMap.get('defaultInputModes');
        if (inputModes != null) {
            card.defaultInputModes = new List<String>();
            for (Object mode : inputModes) {
                card.defaultInputModes.add((String) mode);
            }
        }
        
        List<Object> outputModes = (List<Object>) jsonMap.get('defaultOutputModes');
        if (outputModes != null) {
            card.defaultOutputModes = new List<String>();
            for (Object mode : outputModes) {
                card.defaultOutputModes.add((String) mode);
            }
        }
        
        // Skills
        List<Object> skillsList = (List<Object>) jsonMap.get('skills');
        if (skillsList != null) {
            card.skills = new List<A2AAgentSkill>();
            for (Object skillObj : skillsList) {
                card.skills.add(A2AAgentSkill.deserialize((Map<String, Object>) skillObj));
            }
        }
        
        // Security schemes
        Map<String, Object> schemesMap = (Map<String, Object>) jsonMap.get('securitySchemes');
        if (schemesMap != null) {
            card.securitySchemes = new Map<String, A2ASecurityScheme>();
            for (String key : schemesMap.keySet()) {
                card.securitySchemes.put(key, A2ASecurityScheme.deserialize((Map<String, Object>) schemesMap.get(key)));
            }
        }
        
        // Security requirements
        card.security = (List<Map<String, List<String>>>) jsonMap.get('security');
        
        // Additional interfaces
        List<Object> interfacesList = (List<Object>) jsonMap.get('additionalInterfaces');
        if (interfacesList != null) {
            card.additionalInterfaces = new List<A2AAgentInterface>();
            for (Object interfaceObj : interfacesList) {
                card.additionalInterfaces.add(A2AAgentInterface.deserialize((Map<String, Object>) interfaceObj));
            }
        }
        
        return card;
    }
    
    // Builder pattern implementation
    public class Builder {
        private String name;
        private String description;
        private String url;
        private A2AAgentProvider provider;
        private String version;
        private String documentationUrl;
        private A2AAgentCapabilities capabilities;
        private List<String> defaultInputModes;
        private List<String> defaultOutputModes;
        private List<A2AAgentSkill> skills;
        private Boolean supportsAuthenticatedExtendedCard = false;
        private Map<String, A2ASecurityScheme> securitySchemes;
        private List<Map<String, List<String>>> security;
        private String iconUrl;
        private List<A2AAgentInterface> additionalInterfaces;
        private String preferredTransport;
        private String protocolVersion;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder url(String url) {
            this.url = url;
            return this;
        }
        
        public Builder provider(A2AAgentProvider provider) {
            this.provider = provider;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder documentationUrl(String documentationUrl) {
            this.documentationUrl = documentationUrl;
            return this;
        }
        
        public Builder capabilities(A2AAgentCapabilities capabilities) {
            this.capabilities = capabilities;
            return this;
        }
        
        public Builder defaultInputModes(List<String> defaultInputModes) {
            this.defaultInputModes = defaultInputModes;
            return this;
        }
        
        public Builder defaultOutputModes(List<String> defaultOutputModes) {
            this.defaultOutputModes = defaultOutputModes;
            return this;
        }
        
        public Builder skills(List<A2AAgentSkill> skills) {
            this.skills = skills;
            return this;
        }
        
        public Builder supportsAuthenticatedExtendedCard(Boolean supportsAuthenticatedExtendedCard) {
            this.supportsAuthenticatedExtendedCard = supportsAuthenticatedExtendedCard;
            return this;
        }
        
        public Builder securitySchemes(Map<String, A2ASecurityScheme> securitySchemes) {
            this.securitySchemes = securitySchemes;
            return this;
        }
        
        public Builder security(List<Map<String, List<String>>> security) {
            this.security = security;
            return this;
        }
        
        public Builder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }
        
        public Builder additionalInterfaces(List<A2AAgentInterface> additionalInterfaces) {
            this.additionalInterfaces = additionalInterfaces;
            return this;
        }
        
        public Builder preferredTransport(String preferredTransport) {
            this.preferredTransport = preferredTransport;
            return this;
        }
        
        public Builder protocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }
        
        public A2AAgentCard build() {
            return new A2AAgentCard(name, description, url, provider, version, documentationUrl,
                    capabilities, defaultInputModes, defaultOutputModes, skills,
                    supportsAuthenticatedExtendedCard, securitySchemes, security, iconUrl,
                    additionalInterfaces, preferredTransport, protocolVersion);
        }
    }
}