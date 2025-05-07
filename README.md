# Java A2A SDK (WIP)

This project (currently WIP) will provide a Java SDK implementation of Google's [Agent2Agent protocol (A2A)](https://google.github.io/A2A/).

## Specification

The majority of the classes required by the specification can currently be found in the [src/main/java/io/a2a/spec](https://github.com/fjuma/a2a-java-sdk/tree/main/src/main/java/io/a2a/spec) directory.

## Client

An *initial* [A2AClient](https://github.com/fjuma/a2a-java-sdk/blob/main/src/main/java/io/a2a/client/A2AClient.java) class has been added. This is very much work in progress, we are working on implementing the methods required by the client side of the protocol.

### Sample Usage

#### Send a task

```java
// Create an A2AClient (the URL specified is the server agent's URL)
A2AClient client = new A2AClient("http://localhost:1234");

// Send a text message to the server agent
Message message = A2A.toUserMessage("tell me a joke");
TaskSendParams params = new TaskSendParams.Builder()
        .id("task-1234") // id is optional
        .message(message)
        .build();
SendTaskResponse response = client.sendTask(params);        
```

#### Retrieve details about the server agent that this client agent is communicating with
```java
A2AClient client = new A2AClient("http://localhost:1234");
AgentCard serverAgentCard = client.getAgentCard();
```

## Server

TODO



