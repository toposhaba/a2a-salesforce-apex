/**
 * Test class for A2AMessage and Part classes
 */
@isTest
private class A2AMessageTest {
    
    @isTest
    static void testTextPartSerialization() {
        A2ATextPart textPart = new A2ATextPart('Hello, World!');
        
        System.assertEquals('text', textPart.getType());
        System.assertEquals('Hello, World!', textPart.text);
        
        Map<String, Object> serialized = textPart.serialize();
        System.assertEquals('text', serialized.get('type'));
        System.assertEquals('Hello, World!', serialized.get('text'));
        
        // Test deserialization
        A2ATextPart deserialized = A2ATextPart.deserialize(serialized);
        System.assertEquals('Hello, World!', deserialized.text);
    }
    
    @isTest
    static void testDataPartSerialization() {
        Map<String, Object> data = new Map<String, Object>{
            'key1' => 'value1',
            'key2' => 123,
            'key3' => true
        };
        
        Map<String, Object> metadata = new Map<String, Object>{
            'source' => 'test'
        };
        
        A2ADataPart dataPart = new A2ADataPart(data, metadata);
        
        System.assertEquals('data', dataPart.getType());
        System.assertEquals(data, dataPart.data);
        System.assertEquals(metadata, dataPart.metadata);
        
        Map<String, Object> serialized = dataPart.serialize();
        System.assertEquals('data', serialized.get('type'));
        System.assertEquals(data, serialized.get('data'));
        System.assertEquals(metadata, serialized.get('metadata'));
        
        // Test deserialization
        A2ADataPart deserialized = A2ADataPart.deserialize(serialized);
        System.assertEquals('value1', deserialized.getValue('key1'));
        System.assertEquals(123, deserialized.getValue('key2'));
        System.assertEquals(true, deserialized.getValue('key3'));
        System.assertEquals(true, deserialized.hasMetadata());
    }
    
    @isTest
    static void testFilePartWithBytes() {
        A2AFileWithBytes fileContent = new A2AFileWithBytes('text/plain', 'test.txt', 'SGVsbG8sIFdvcmxkIQ==');
        A2AFilePart filePart = new A2AFilePart(fileContent);
        
        System.assertEquals('file', filePart.getType());
        System.assertEquals('test.txt', filePart.getFileName());
        System.assertEquals('text/plain', filePart.getMimeType());
        System.assertEquals(true, filePart.isFileWithBytes());
        System.assertEquals(false, filePart.isFileWithUri());
        
        Map<String, Object> serialized = filePart.serialize();
        System.assertEquals('file', serialized.get('type'));
        System.assertNotEquals(null, serialized.get('file'));
        
        // Test blob conversion
        A2AFileWithBytes bytesFile = (A2AFileWithBytes) filePart.file;
        Blob content = bytesFile.getBlob();
        System.assertEquals('Hello, World!', content.toString());
    }
    
    @isTest
    static void testFilePartWithUri() {
        A2AFileWithUri fileContent = new A2AFileWithUri('image/png', 'logo.png', 'https://example.com/logo.png');
        A2AFilePart filePart = new A2AFilePart(fileContent);
        
        System.assertEquals('file', filePart.getType());
        System.assertEquals('logo.png', filePart.getFileName());
        System.assertEquals('image/png', filePart.getMimeType());
        System.assertEquals(false, filePart.isFileWithBytes());
        System.assertEquals(true, filePart.isFileWithUri());
        
        // Test URI validation
        A2AFileWithUri uriFile = (A2AFileWithUri) filePart.file;
        System.assertEquals(true, uriFile.isValidUrl());
    }
    
    @isTest
    static void testMessageSerialization() {
        // Create message with multiple parts
        A2AMessage message = new A2AMessage();
        message.id = 'msg_123';
        message.role = 'user';
        message.contextId = 'ctx_456';
        
        List<A2APart> parts = new List<A2APart>();
        parts.add(new A2ATextPart('This is a test message'));
        
        Map<String, Object> data = new Map<String, Object>{
            'temperature' => 0.7,
            'max_tokens' => 100
        };
        parts.add(new A2ADataPart(data));
        
        message.parts = parts;
        
        // Test serialization
        Map<String, Object> serialized = message.serialize();
        System.assertEquals('msg_123', serialized.get('id'));
        System.assertEquals('user', serialized.get('role'));
        System.assertEquals('ctx_456', serialized.get('contextId'));
        
        List<Object> serializedParts = (List<Object>) serialized.get('parts');
        System.assertEquals(2, serializedParts.size());
        
        // Test JSON serialization
        String jsonString = JSON.serialize(serialized);
        System.assert(jsonString.contains('msg_123'));
        System.assert(jsonString.contains('This is a test message'));
        
        // Test deserialization
        A2AMessage deserialized = A2AMessage.deserialize(jsonString);
        System.assertEquals('msg_123', deserialized.id);
        System.assertEquals('user', deserialized.role);
        System.assertEquals('ctx_456', deserialized.contextId);
        System.assertEquals(2, deserialized.parts.size());
        
        // Verify parts
        A2APart firstPart = deserialized.parts[0];
        System.assertEquals('text', firstPart.getType());
        System.assert(firstPart instanceof A2ATextPart);
        
        A2APart secondPart = deserialized.parts[1];
        System.assertEquals('data', secondPart.getType());
        System.assert(secondPart instanceof A2ADataPart);
    }
    
    @isTest
    static void testMessageBuilder() {
        A2AMessage message = new A2AMessage.Builder()
            .withId('test_id')
            .withRole('assistant')
            .withContextId('test_context')
            .withTextPart('Hello from builder')
            .withDataPart(new Map<String, Object>{'test' => true})
            .build();
        
        System.assertEquals('test_id', message.id);
        System.assertEquals('assistant', message.role);
        System.assertEquals('test_context', message.contextId);
        System.assertEquals(2, message.parts.size());
    }
}