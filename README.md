# A2A Apex SDK for Salesforce

A comprehensive Salesforce Apex implementation of the Agent-to-Agent (A2A) Protocol, enabling seamless integration of AI agents with Salesforce applications.

## Overview

The A2A Apex SDK provides a complete framework for building and integrating A2A-compliant agents within the Salesforce platform. It includes client libraries, server implementations, UI components, and enterprise features like authentication, logging, and batch processing.

## Features

### Core Protocol Support
- **Full A2A Protocol Implementation**: Complete support for all A2A message types and operations
- **JSON-RPC Support**: Native JSON-RPC 2.0 implementation for agent communication
- **Message Types**: Text, Data, and File parts with full serialization/deserialization
- **Agent Cards**: Complete agent capability discovery and metadata support

### Client Features
- **A2A Client**: Full-featured client for communicating with A2A agents
- **Card Resolution**: Automatic agent card discovery and caching
- **Error Handling**: Comprehensive error categorization and recovery strategies
- **Retry Logic**: Built-in retry mechanisms with exponential backoff

### Server Features
- **REST API Endpoint**: Expose Salesforce functionality as A2A agents
- **Request Handlers**: Extensible framework for handling agent requests
- **Task Management**: Complete task lifecycle management with persistence
- **Platform Events**: Real-time task updates and notifications

### Enterprise Features
- **Authentication**: Multiple auth providers (Bearer Token, API Key, OAuth2)
- **Credential Management**: Secure storage using Custom Metadata and Protected Settings
- **Logging Framework**: Structured logging with multiple levels and persistence
- **Error Handling**: Comprehensive error categorization and recovery strategies
- **Batch Processing**: Process large volumes of tasks asynchronously
- **Queueable Operations**: Chain multiple operations with retry logic

### UI Components
- **Lightning Web Components**: Modern UI for agent interaction
  - Agent Card Display
  - Task Monitor with real-time updates
  - Message Composer
- **Flow Integration**: Declarative tools for no-code integration
  - Send A2A Message action
  - Get Task Status action
  - Monitor Task action

### Security
- **Permission Sets**: Granular access control
  - A2A Administrator: Full access
  - A2A User: Read-only access
- **Field-Level Security**: Proper FLS enforcement
- **CRUD Permissions**: Respects object and field permissions

## Installation

### Prerequisites
- Salesforce DX CLI
- Salesforce org with API enabled
- VS Code with Salesforce extensions (recommended)

### Deploy to Org

1. Clone the repository:
```bash
git clone https://github.com/your-org/a2a-apex-sdk.git
cd a2a-apex-sdk
```

2. Authorize your org:
```bash
sfdx force:auth:web:login -a myorg
```

3. Deploy the source:
```bash
sfdx force:source:deploy -p force-app -u myorg
```

4. Assign permission sets:
```bash
# For administrators
sfdx force:user:permset:assign -n A2A_Administrator -u myorg

# For regular users
sfdx force:user:permset:assign -n A2A_User -u myorg
```

## Quick Start

### Client Usage

#### Basic Message Sending
```apex
// Create a client
A2AClient client = new A2AClient('https://api.example.com/agent');

// Build a message
A2AMessage message = new A2AMessage.Builder()
    .withRole('user')
    .withTextPart('Hello, agent!')
    .build();

// Send the message
A2AClient.A2AMessageSendParams params = new A2AClient.A2AMessageSendParams(message);
A2AClient.A2ASendMessageResponse response = client.sendMessage(params);

// Check the response
if (response.error != null) {
    System.debug('Error: ' + response.error.message);
} else {
    Map<String, Object> result = (Map<String, Object>) response.result.data;
    String taskId = (String) result.get('id');
    System.debug('Task created: ' + taskId);
}
```

#### Using Authentication
```apex
// API Key authentication
A2AAuthProvider auth = new A2AApiKeyAuth('your-api-key');
A2AHttpClient httpClient = new A2AHttpClient.Builder()
    .withAuthProvider(auth)
    .build();

A2AClient client = new A2AClient(httpClient, 'https://api.example.com/agent');
```

#### Using Credential Store
```apex
// Get credentials from secure storage
A2AAuthProvider auth = A2ACredentialStore.getCredential('MyAgentCredential');
A2AHttpClient httpClient = new A2AHttpClient.Builder()
    .withAuthProvider(auth)
    .build();

A2AClient client = new A2AClient(httpClient, 'https://api.example.com/agent');
```

### Server Usage

#### Creating a Custom Request Handler
```apex
public class MyAgentHandler implements A2ARequestHandler {
    
    public A2AEventKind onMessageSend(A2AMessageSendParams params, A2AServerCallContext context) {
        // Process the incoming message
        A2AMessage message = params.message;
        
        // Extract text content
        String userQuery = '';
        for (A2APart part : message.parts) {
            if (part instanceof A2ATextPart) {
                userQuery = ((A2ATextPart) part).text;
                break;
            }
        }
        
        // Create a task
        String taskId = A2AUtils.generateTaskId();
        
        // Process asynchronously
        System.enqueueJob(new ProcessAgentRequest(taskId, userQuery));
        
        // Return task reference
        A2AEventKind result = new A2AEventKind();
        result.type = 'task';
        result.data = new Map<String, Object>{
            'id' => taskId,
            'status' => 'RUNNING'
        };
        
        return result;
    }
    
    // Implement other required methods...
}
```

### Lightning Web Component Usage

Add the Agent Card component to your Lightning page:

```xml
<template>
    <c-a2a-agent-card 
        agent-url="https://api.example.com/agent"
        card-title="My AI Assistant">
    </c-a2a-agent-card>
</template>
```

### Flow Integration

1. Create a new Flow
2. Add an Action element
3. Search for "Send A2A Message"
4. Configure the action:
   - Agent URL: Your agent endpoint
   - Message Text: The message to send
   - Role: 'user' (default)
5. Use the output Task ID for monitoring

## Architecture

### Directory Structure
```
force-app/main/default/
├── classes/
│   ├── spec/           # A2A protocol specifications
│   ├── client/         # Client implementations
│   ├── server/         # Server implementations
│   ├── http/           # HTTP utilities
│   ├── auth/           # Authentication providers
│   ├── batch/          # Batch and async processing
│   ├── common/         # Utilities and helpers
│   └── tests/          # Test classes
├── lwc/                # Lightning Web Components
│   ├── a2aAgentCard/
│   └── a2aTaskMonitor/
├── objects/            # Custom objects
│   ├── A2A_Task__c/
│   └── A2A_Log__c/
└── permissionsets/     # Permission sets
```

### Key Components

#### Protocol Layer
- `A2AMessage`: Core message structure with parts
- `A2APart`: Base class for message parts (Text, Data, File)
- `A2AAgentCard`: Agent metadata and capabilities
- `A2ASecurityScheme`: Authentication schemes

#### Client Layer
- `A2AClient`: Main client for agent communication
- `A2ACardResolver`: Agent card discovery
- `A2AHttpClient`: HTTP communication with retry logic

#### Server Layer
- `A2AServerREST`: REST API endpoint
- `A2ARequestHandler`: Interface for handling requests
- `A2ATaskManager`: Task lifecycle management
- `A2AJSONRPCHandler`: JSON-RPC request processing

#### Storage Layer
- `A2ATaskStore`: Interface for task persistence
- `A2ACustomObjectTaskStore`: Database persistence
- `A2AInMemoryTaskStore`: Memory storage for testing

## Advanced Features

### Batch Processing
```apex
// Process multiple tasks in batch
A2ATaskProcessorBatch.A2ATaskProcessorConfig config = 
    new A2ATaskProcessorBatch.A2ATaskProcessorConfig();
config.taskStatus = 'SUBMITTED';
config.notificationEmail = 'admin@example.com';

A2ATaskProcessorBatch batch = new A2ATaskProcessorBatch(agentUrl, config);
Database.executeBatch(batch, 50);
```

### Queueable Chaining
```apex
// Send multiple messages with chaining
List<A2AMessageQueueable.MessageRequest> requests = 
    new List<A2AMessageQueueable.MessageRequest>();

for (String query : queries) {
    A2AMessage msg = new A2AMessage.Builder()
        .withTextPart(query)
        .build();
    requests.add(new A2AMessageQueueable.MessageRequest(agentUrl, msg));
}

A2AMessageQueueable.A2AMessageQueueableConfig config = 
    new A2AMessageQueueable.A2AMessageQueueableConfig();
config.maxRetries = 3;
config.delayBetweenMessages = 2; // seconds

A2AMessageQueueable.enqueueMessages(requests, config);
```

### Error Handling
```apex
try {
    // A2A operation
} catch (Exception e) {
    A2AErrorHandler.A2AError error = A2AErrorHandler.handleException(e, 
        new Map<String, Object>{'operation' => 'sendMessage'});
    
    if (A2AErrorHandler.isRetryable(error)) {
        // Retry logic
        Integer delay = A2AErrorHandler.calculateRetryDelay(attemptNumber);
    }
    
    // Show user-friendly message
    String userMessage = A2AErrorHandler.getUserMessage(error);
}
```

### Logging
```apex
// Configure logging
A2ALogger.configure(
    A2ALogger.LogLevel.DEBUG,  // Minimum level
    true,                      // Console logging
    true                       // Database persistence
);

// Log operations
A2ALogger.info('AGENT', 'Processing request', new Map<String, Object>{
    'agentUrl' => agentUrl,
    'messageId' => messageId
});

// Log task events
A2ALogger.logTaskEvent(taskId, 'status_update', A2ATaskManager.A2ATaskStatus.COMPLETED);

// Flush logs to database
A2ALogger.flush();
```

## Testing

### Unit Tests
Run all tests:
```bash
sfdx force:apex:test:run -u myorg -r human
```

Run specific test class:
```bash
sfdx force:apex:test:run -u myorg -t A2AMessageTest -r human
```

### Test Coverage
The SDK includes comprehensive test classes:
- `A2AAgentCardTest`: Agent card serialization/deserialization
- `A2AMessageTest`: Message and parts testing
- `A2ATaskManagerTest`: Task management testing

## Best Practices

### Security
1. Always use the credential store for sensitive data
2. Implement proper authentication for all agents
3. Use permission sets to control access
4. Validate all input data

### Performance
1. Use batch processing for large volumes
2. Implement proper retry strategies
3. Monitor governor limits
4. Use Platform Events for real-time updates

### Error Handling
1. Use the error handler framework
2. Implement appropriate recovery strategies
3. Log all errors for debugging
4. Provide user-friendly error messages

## Troubleshooting

### Common Issues

#### Authentication Failures
- Verify credentials in credential store
- Check token expiry
- Ensure proper permissions

#### Network Errors
- Check Remote Site Settings
- Verify endpoint URLs
- Review firewall rules

#### Governor Limits
- Use batch processing
- Implement pagination
- Monitor API limits

### Debug Mode
Enable debug logging:
```apex
A2ALogger.configure(A2ALogger.LogLevel.DEBUG, true, true);
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- GitHub Issues: [github.com/your-org/a2a-apex-sdk/issues](https://github.com/your-org/a2a-apex-sdk/issues)
- Documentation: [docs.example.com/a2a-apex-sdk](https://docs.example.com/a2a-apex-sdk)
- Email: support@example.com



