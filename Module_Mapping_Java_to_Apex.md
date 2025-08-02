# Module-by-Module Java to Apex Mapping

## Overview
This document provides a detailed mapping of each Java module in the A2A SDK to its corresponding Apex implementation, including specific class conversions and architectural considerations.

## Module Conversion Map

### 1. Common Module (`common/`)

#### Java Structure
```
common/
└── src/main/java/io/a2a/
    └── util/
        ├── Assert.java
        ├── Utils.java
        └── [other utilities]
```

#### Apex Structure
```
force-app/main/default/classes/common/
├── A2AAssert.cls
├── A2AAssert.cls-meta.xml
├── A2AUtils.cls
├── A2AUtils.cls-meta.xml
├── A2AConstants.cls
└── A2AConstants.cls-meta.xml
```

#### Class Mappings

| Java Class | Apex Class | Key Changes |
|------------|------------|-------------|
| `Assert.java` | `A2AAssert.cls` | - Replace `IllegalArgumentException` with custom `A2AException`<br>- Implement null checks using Apex patterns |
| `Utils.java` | `A2AUtils.cls` | - String manipulation using Apex String methods<br>- Collection utilities using Apex List/Set/Map |

### 2. Spec Module (`spec/`)

#### Java Structure
```
spec/
└── src/main/java/io/a2a/spec/
    ├── AgentCard.java
    ├── Message.java
    ├── Task.java
    ├── JSONRPCError.java
    └── [60+ specification classes]
```

#### Apex Structure
```
force-app/main/default/classes/spec/
├── A2AAgentCard.cls
├── A2AMessage.cls
├── A2ATask.cls
├── A2AJSONRPCError.cls
├── A2APart.cls
├── A2ATextPart.cls
├── A2ADataPart.cls
├── A2AFilePart.cls
└── [corresponding Apex classes]
```

#### Key Class Conversions

##### AgentCard
**Java:**
```java
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record AgentCard(
    String name, 
    String description, 
    String url,
    AgentCapabilities capabilities,
    List<AgentSkill> skills
) {}
```

**Apex:**
```apex
public class A2AAgentCard implements A2ASerializable {
    public String name { get; set; }
    public String description { get; set; }
    public String url { get; set; }
    public A2AAgentCapabilities capabilities { get; set; }
    public List<A2AAgentSkill> skills { get; set; }
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new Map<String, Object>();
        if (name != null) result.put('name', name);
        if (description != null) result.put('description', description);
        if (url != null) result.put('url', url);
        if (capabilities != null) result.put('capabilities', capabilities.serialize());
        if (skills != null && !skills.isEmpty()) {
            List<Map<String, Object>> skillsList = new List<Map<String, Object>>();
            for (A2AAgentSkill skill : skills) {
                skillsList.add(skill.serialize());
            }
            result.put('skills', skillsList);
        }
        return result;
    }
    
    public static A2AAgentCard deserialize(String jsonString) {
        Map<String, Object> jsonMap = (Map<String, Object>) JSON.deserializeUntyped(jsonString);
        return deserialize(jsonMap);
    }
    
    public static A2AAgentCard deserialize(Map<String, Object> jsonMap) {
        A2AAgentCard card = new A2AAgentCard();
        card.name = (String) jsonMap.get('name');
        card.description = (String) jsonMap.get('description');
        card.url = (String) jsonMap.get('url');
        // ... deserialize nested objects
        return card;
    }
}
```

##### Message and Parts
**Java:**
```java
public record Message(
    String id,
    List<Part<?>> parts,
    MessageRole role
) {}

public sealed interface Part<T> permits TextPart, DataPart, FilePart {}
```

**Apex:**
```apex
public class A2AMessage {
    public String id { get; set; }
    public List<A2APart> parts { get; set; }
    public String role { get; set; }
}

public abstract class A2APart {
    public abstract String getType();
    public abstract Map<String, Object> serialize();
}

public class A2ATextPart extends A2APart {
    public String text { get; set; }
    
    public override String getType() {
        return 'text';
    }
    
    public override Map<String, Object> serialize() {
        return new Map<String, Object>{
            'type' => 'text',
            'text' => text
        };
    }
}
```

### 3. Client Module (`client/`)

#### Java Structure
```
client/
└── src/main/java/io/a2a/
    ├── A2A.java
    ├── client/
    │   ├── A2AClient.java
    │   ├── A2ACardResolver.java
    │   └── sse/
    │       └── SSEEventListener.java
    └── http/
        ├── A2AHttpClient.java
        └── JdkA2AHttpClient.java
```

#### Apex Structure
```
force-app/main/default/classes/client/
├── A2A.cls
├── A2AClient.cls
├── A2ACardResolver.cls
├── A2AHttpClient.cls
├── A2AHttpRequest.cls
├── A2AHttpResponse.cls
├── A2AStreamingClient.cls
└── A2ACalloutMock.cls
```

#### Key Conversions

##### A2AClient
**Java:**
```java
public class A2AClient {
    private final A2AHttpClient httpClient;
    
    public CompletableFuture<SendMessageResponse> sendMessage(MessageSendParams params) {
        // Async implementation
    }
}
```

**Apex:**
```apex
public class A2AClient {
    private String agentUrl;
    private A2AHttpClient httpClient;
    
    public A2AClient(String agentUrl) {
        this.agentUrl = agentUrl;
        this.httpClient = new A2AHttpClient();
    }
    
    public A2ASendMessageResponse sendMessage(A2AMessageSendParams params) {
        A2AHttpRequest request = new A2AHttpRequest();
        request.setEndpoint(agentUrl + '/tasks');
        request.setMethod('POST');
        request.setBody(JSON.serialize(params.serialize()));
        
        A2AHttpResponse response = httpClient.send(request);
        return A2ASendMessageResponse.deserialize(response.getBody());
    }
    
    @future(callout=true)
    public static void sendMessageAsync(String agentUrl, String paramsJson) {
        // Async implementation using @future
    }
}
```

##### Streaming Support
**Java:** Uses SSE (Server-Sent Events)
**Apex:** Platform Events + CometD

```apex
public class A2AStreamingClient {
    public static void subscribeToTask(String taskId) {
        // Subscribe to Platform Event
        EventBus.subscribe('A2A_Task_Event__e', new A2ATaskEventListener());
    }
}

public class A2ATaskEventListener implements EventBus.EventListener {
    public void onEvent(EventBus.Event event) {
        A2A_Task_Event__e taskEvent = (A2A_Task_Event__e) event.getData();
        // Handle event
    }
}
```

### 4. SDK Server Common (`sdk-server-common/`)

#### Java Structure
```
sdk-server-common/
└── src/main/java/io/a2a/server/
    ├── agentexecution/
    │   ├── AgentExecutor.java
    │   └── RequestContext.java
    ├── events/
    │   └── EventQueue.java
    ├── tasks/
    │   └── TaskUpdater.java
    └── requesthandlers/
        └── [various handlers]
```

#### Apex Structure
```
force-app/main/default/classes/server/
├── A2AAgentExecutor.cls
├── A2ARequestContext.cls
├── A2AEventPublisher.cls
├── A2ATaskManager.cls
├── A2ARestResource.cls
└── handlers/
    ├── A2ASendMessageHandler.cls
    ├── A2AGetTaskHandler.cls
    └── A2ACancelTaskHandler.cls
```

#### REST Resource Implementation

**Java:**
```java
@Path("/")
public class A2AResource {
    @POST
    @Path("/tasks")
    public Response sendMessage(SendMessageRequest request) {
        // Handle request
    }
}
```

**Apex:**
```apex
@RestResource(urlMapping='/a2a/v1/*')
global with sharing class A2ARestResource {
    @HttpPost
    global static void doPost() {
        RestRequest req = RestContext.request;
        RestResponse res = RestContext.response;
        
        String path = req.requestURI.substringAfter('/a2a/v1');
        
        try {
            if (path == '/tasks') {
                A2ASendMessageHandler handler = new A2ASendMessageHandler();
                handler.handle(req, res);
            }
            // ... other paths
        } catch (Exception e) {
            res.statusCode = 500;
            res.responseBody = Blob.valueOf(JSON.serialize(new A2AError(e.getMessage())));
        }
    }
}
```

### 5. Reference Implementation (`reference-impl/`)

This module won't be directly converted. Instead, we'll create:

#### Apex Implementation Structure
```
force-app/main/default/
├── applications/
│   └── A2A_Agent_Manager.app-meta.xml
├── flexipages/
│   ├── A2A_Agent_Configuration.flexipage-meta.xml
│   └── A2A_Task_Monitor.flexipage-meta.xml
├── lwc/
│   ├── a2aAgentCard/
│   ├── a2aTaskMonitor/
│   └── a2aMessageViewer/
└── objects/
    ├── A2A_Agent_Card__c/
    ├── A2A_Task__c/
    └── A2A_Message__c/
```

## Platform-Specific Conversions

### 1. Dependency Injection

**Java:** Uses CDI annotations
```java
@Inject
WeatherAgent weatherAgent;

@Produces
@PublicAgentCard
public AgentCard agentCard() { }
```

**Apex:** Factory Pattern + Custom Settings
```apex
public class A2AAgentFactory {
    private static Map<String, Type> agentRegistry = new Map<String, Type>();
    
    public static void registerAgent(String name, Type agentType) {
        agentRegistry.put(name, agentType);
    }
    
    public static A2AAgent getAgent(String name) {
        Type agentType = agentRegistry.get(name);
        return (A2AAgent) agentType.newInstance();
    }
}
```

### 2. Asynchronous Operations

**Java:** CompletableFuture
```java
CompletableFuture<Response> future = client.sendAsync(request);
```

**Apex:** Queueable + Continuation
```apex
public class A2AAsyncClient implements Queueable, Database.AllowsCallouts {
    public void execute(QueueableContext context) {
        // Async callout
    }
}

// For VF/LWC
public class A2AContinuationController {
    public Object startRequest() {
        Continuation con = new Continuation(60);
        con.continuationMethod = 'processResponse';
        // Setup request
        return con;
    }
}
```

### 3. Error Handling

**Java:** Custom exceptions with inheritance
```java
public class JSONRPCError extends Exception {
    private final int code;
    private final String message;
}
```

**Apex:** Custom exceptions with error codes
```apex
public class A2AException extends Exception {
    private Integer errorCode;
    private String errorType;
    
    public A2AException(String message, Integer code, String type) {
        this(message);
        this.errorCode = code;
        this.errorType = type;
    }
    
    public Map<String, Object> toJSON() {
        return new Map<String, Object>{
            'code' => errorCode,
            'message' => getMessage(),
            'type' => errorType
        };
    }
}
```

## Testing Strategy Mapping

### Java Testing
- JUnit 5
- Mockito
- REST Assured

### Apex Testing
```apex
@isTest
private class A2AClientTest {
    @isTest
    static void testSendMessage() {
        // Setup mock
        Test.setMock(HttpCalloutMock.class, new A2ACalloutMock());
        
        Test.startTest();
        A2AClient client = new A2AClient('https://test.com');
        A2ASendMessageResponse response = client.sendMessage(params);
        Test.stopTest();
        
        System.assertNotEquals(null, response);
    }
}

public class A2ACalloutMock implements HttpCalloutMock {
    public HTTPResponse respond(HTTPRequest req) {
        HttpResponse res = new HttpResponse();
        res.setStatusCode(200);
        res.setBody('{"task": {"id": "123"}}');
        return res;
    }
}
```

## Configuration Management

### Java
- application.properties
- Environment variables
- CDI configuration

### Apex
- Custom Settings
- Custom Metadata Types
- Named Credentials

```apex
// Custom Metadata Type: A2A_Agent_Config__mdt
public class A2AConfig {
    public static A2A_Agent_Config__mdt getConfig(String agentName) {
        return [SELECT DeveloperName, Endpoint__c, Timeout__c, Auth_Type__c
                FROM A2A_Agent_Config__mdt 
                WHERE DeveloperName = :agentName
                LIMIT 1];
    }
}
```

## Build and Deployment

### Java
- Maven build
- JAR packaging
- Container deployment

### Apex
- SFDX project
- Unlocked package
- Scratch org development

```json
// sfdx-project.json
{
  "packageDirectories": [
    {
      "path": "force-app",
      "default": true,
      "package": "A2A SDK",
      "versionName": "Version 1.0",
      "versionNumber": "1.0.0.NEXT"
    }
  ],
  "namespace": "a2a",
  "sfdcLoginUrl": "https://login.salesforce.com",
  "sourceApiVersion": "59.0"
}
```

## Migration Checklist

### Per Module Tasks
- [ ] Create Apex class structure
- [ ] Implement serialization/deserialization
- [ ] Add error handling
- [ ] Create test classes (>85% coverage)
- [ ] Add documentation
- [ ] Create permission sets
- [ ] Setup custom objects/metadata

### Cross-Module Tasks
- [ ] Implement common interfaces
- [ ] Create factory classes
- [ ] Setup Platform Events
- [ ] Configure Named Credentials
- [ ] Build LWC components
- [ ] Create admin UI
- [ ] Package configuration
