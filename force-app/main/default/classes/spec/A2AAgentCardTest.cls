/**
 * Test class for A2AAgentCard
 */
@isTest
private class A2AAgentCardTest {
    
    @isTest
    static void testAgentCardSerializationDeserialization() {
        // Create test agent card
        A2AAgentCard card = new A2AAgentCard();
        card.name = 'Test Agent';
        card.description = 'Test Description';
        card.url = 'https://example.com/agent';
        
        // Create capabilities
        A2AAgentCapabilities capabilities = new A2AAgentCapabilities();
        capabilities.streaming = true;
        capabilities.pushNotifications = false;
        card.capabilities = capabilities;
        
        // Create skills
        List<A2AAgentSkill> skills = new List<A2AAgentSkill>();
        A2AAgentSkill skill = new A2AAgentSkill();
        skill.name = 'data_processing';
        skill.description = 'Can process data';
        skills.add(skill);
        card.skills = skills;
        
        // Test serialization
        Map<String, Object> serialized = card.serialize();
        System.assertNotEquals(null, serialized);
        System.assertEquals('Test Agent', serialized.get('name'));
        System.assertEquals('Test Description', serialized.get('description'));
        System.assertEquals('https://example.com/agent', serialized.get('url'));
        
        // Test JSON serialization
        String jsonString = JSON.serialize(serialized);
        System.assertNotEquals(null, jsonString);
        System.assert(jsonString.contains('Test Agent'));
        
        // Test deserialization
        A2AAgentCard deserializedCard = A2AAgentCard.deserialize(jsonString);
        System.assertNotEquals(null, deserializedCard);
        System.assertEquals('Test Agent', deserializedCard.name);
        System.assertEquals('Test Description', deserializedCard.description);
        System.assertEquals('https://example.com/agent', deserializedCard.url);
        System.assertNotEquals(null, deserializedCard.capabilities);
        System.assertEquals(true, deserializedCard.capabilities.streaming);
        System.assertEquals(false, deserializedCard.capabilities.pushNotifications);
        System.assertEquals(1, deserializedCard.skills.size());
        System.assertEquals('data_processing', deserializedCard.skills[0].name);
    }
    
    @isTest
    static void testAgentCardWithNullFields() {
        // Create minimal agent card
        A2AAgentCard card = new A2AAgentCard();
        card.name = 'Minimal Agent';
        card.url = 'https://example.com/minimal';
        
        // Test serialization with null fields
        Map<String, Object> serialized = card.serialize();
        System.assertNotEquals(null, serialized);
        System.assertEquals('Minimal Agent', serialized.get('name'));
        System.assertEquals(null, serialized.get('description')); // Should not include null description
        
        // Test deserialization
        String jsonString = JSON.serialize(serialized);
        A2AAgentCard deserializedCard = A2AAgentCard.deserialize(jsonString);
        System.assertEquals('Minimal Agent', deserializedCard.name);
        System.assertEquals(null, deserializedCard.description);
        System.assertEquals(null, deserializedCard.capabilities);
        System.assertEquals(null, deserializedCard.skills);
    }
    
    @isTest
    static void testAgentCapabilities() {
        A2AAgentCapabilities capabilities = new A2AAgentCapabilities();
        capabilities.streaming = true;
        capabilities.pushNotifications = true;
        capabilities.customCapabilities = new Map<String, Object>{
            'maxTokens' => 1000,
            'supportedLanguages' => new List<String>{'en', 'es', 'fr'}
        };
        
        Map<String, Object> serialized = capabilities.serialize();
        System.assertEquals(true, serialized.get('streaming'));
        System.assertEquals(true, serialized.get('pushNotifications'));
        System.assertNotEquals(null, serialized.get('customCapabilities'));
        
        // Test deserialization
        A2AAgentCapabilities deserialized = A2AAgentCapabilities.deserialize(serialized);
        System.assertEquals(true, deserialized.streaming);
        System.assertEquals(true, deserialized.pushNotifications);
        System.assertEquals(1000, deserialized.customCapabilities.get('maxTokens'));
    }
}