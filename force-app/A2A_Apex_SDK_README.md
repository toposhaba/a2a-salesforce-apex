# A2A Salesforce Apex SDK

This is the Salesforce Apex implementation of the A2A (Agent-to-Agent) Protocol SDK, converted from the original Java implementation.

## Overview

The A2A Apex SDK enables Salesforce applications to interact with A2A protocol-compliant agents. It provides a comprehensive set of classes for:

- Agent discovery and capabilities
- Message exchange
- Task management
- Security and authentication

## Project Structure

```
force-app/main/default/classes/
├── common/          # Common utilities and base classes
│   ├── A2AAssert    # Parameter validation utilities
│   ├── A2AException # Base exception class
│   └── A2ASerializable # Interface for JSON serialization
├── spec/            # A2A protocol specifications
│   ├── A2AAgentCard # Agent metadata and capabilities
│   ├── A2AMessage   # Message structure
│   ├── A2APart      # Message parts (Text, File, Data)
│   └── ...          # Other protocol specifications
├── client/          # Client implementation
│   ├── A2A          # Main entry point
│   ├── A2AClient    # HTTP client for agent communication
│   └── A2ACardResolver # Agent card resolution
├── server/          # Server implementation (TODO)
└── http/            # HTTP utilities
    └── A2AHttpClient # HTTP client wrapper
```

## Key Differences from Java Implementation

### 1. Language Constraints
- **No Generics**: Apex doesn't support generics, so type-specific implementations are used
- **No Annotations**: Jackson annotations replaced with manual JSON serialization
- **No Records**: Java records converted to Apex classes with properties
- **No Interfaces with Default Methods**: All interfaces use abstract methods only

### 2. Platform-Specific Adaptations
- **HTTP Callouts**: Using Salesforce's Http class instead of Java HTTP clients
- **Async Operations**: Using @future, Queueable, and Batch Apex instead of CompletableFuture
- **Streaming**: Platform Events and CometD instead of SSE/WebSockets
- **Security**: Named Credentials and Auth Providers for authentication

### 3. Salesforce Enhancements
- **Custom Objects**: For storing agent configurations and message history
- **Lightning Components**: UI components for agent interaction (TODO)
- **Flow Integration**: Invocable actions for declarative use (TODO)
- **Platform Events**: For real-time updates and notifications

## Usage Examples

### Creating a Client

```apex
// Create a client for an agent
A2AClient client = A2A.client('https://agent.example.com');

// Get agent capabilities
A2AAgentCard card = client.getAgentCard();
System.debug('Agent name: ' + card.name);
System.debug('Agent capabilities: ' + card.capabilities);
```

### Sending a Message

```apex
// Create a text message
A2AMessage message = A2A.toUserMessage('Hello, agent!');

// Send the message
Map<String, Object> response = client.sendMessage(message);
System.debug('Task ID: ' + response.get('taskId'));
```

### Working with Agent Cards

```apex
// Build an agent card
A2AAgentCard.Builder builder = new A2AAgentCard.Builder();
A2AAgentCard card = builder
    .name('My Agent')
    .description('An example A2A agent')
    .url('https://myagent.example.com')
    .version('1.0.0')
    .protocolVersion('0.1.0')
    .capabilities(new A2AAgentCapabilities(true, false, false, null))
    .defaultInputModes(new List<String>{'text'})
    .defaultOutputModes(new List<String>{'text'})
    .skills(new List<A2AAgentSkill>())
    .build();
```

## Current Implementation Status

### Completed
- ✅ Common utilities (Assert, Exception, Serializable interface)
- ✅ Core spec classes (AgentCard, Message, Parts)
- ✅ Basic client functionality
- ✅ JSON serialization/deserialization

### In Progress
- 🚧 Full client implementation
- 🚧 Server implementation
- 🚧 Streaming support
- 🚧 Authentication schemes

### TODO
- ⬜ Platform Events integration
- ⬜ Custom Objects and metadata
- ⬜ Lightning Web Components
- ⬜ Flow actions
- ⬜ Test coverage
- ⬜ Documentation
- ⬜ Package creation

## Testing

All classes require test coverage of at least 75% for deployment. Test classes should follow the pattern:

```apex
@isTest
private class A2AClassNameTest {
    @isTest
    static void testMethodName() {
        // Test implementation
    }
}
```

## Deployment

### Using SFDX

```bash
# Authenticate to your org
sfdx auth:web:login -a myorg

# Deploy the source
sfdx force:source:deploy -p force-app -u myorg

# Run tests
sfdx force:apex:test:run -n A2A -u myorg
```

### Using Metadata API

The project can also be deployed using the Metadata API through tools like Workbench or VS Code.

## Contributing

When contributing to the Apex SDK:

1. Follow Apex coding standards
2. Maintain compatibility with the A2A protocol
3. Add appropriate test coverage
4. Update documentation
5. Consider governor limits in your implementation

## License

This Apex SDK maintains the same license as the original Java A2A SDK.

## Notes

This is an initial conversion focusing on core functionality. Many features are still in development. The implementation prioritizes:

1. Protocol compliance
2. Salesforce platform best practices
3. Governor limit awareness
4. Maintainability and extensibility

For questions or issues, please refer to the main A2A SDK repository.