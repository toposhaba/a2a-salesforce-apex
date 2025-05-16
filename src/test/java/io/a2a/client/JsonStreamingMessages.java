package io.a2a.client;

/**
 * Contains JSON strings for testing SSE streaming.
 */
public class JsonStreamingMessages {

    public static final String STREAMING_TASK_EVENT = """
            data: {
                  "jsonrpc": "2.0",
                  "id": "1234",
                  "result": {
                    "type": "task",
                    "id": "task-123",
                    "contextId": "context-456",
                    "status": {
                      "state": "working"
                    }
                  }
            }
            """;


    public static final String STREAMING_MESSAGE_EVENT = """
            data: {
                  "jsonrpc": "2.0",
                  "id": "1234",
                  "result": {
                    "type": "message",
                    "role": "agent",
                    "messageId": "msg-123",
                    "contextId": "context-456",
                    "parts": [
                      {
                        "type": "text",
                        "text": "Hello, world!"
                      }
                    ]
                  }
            }""";

    public static final String STREAMING_STATUS_UPDATE_EVENT = """
            data: {
                  "jsonrpc": "2.0",
                  "id": "1234",
                  "result": {
                    "taskId": "1",
                    "contextId": "2",
                    "status": {
                        "state": "submitted"
                    },
                    "final": false,
                    "type": "status-update"
                  }
            }""";

    public static final String STREAMING_ARTIFACT_UPDATE_EVENT = """
             data: {
                  "jsonrpc": "2.0",
                  "id": "1234",
                  "result": {
                    "type": "artifact-update",
                    "taskId": "1",
                    "contextId": "2",
                    "append": false,
                    "lastChunk": true,
                    "artifact": {
                        "artifactId": "artifact-1",
                        "parts": [
                         {
                            "type": "text",
                            "text": "Why did the chicken cross the road? To get to the other side!"
                         }
                        ]
                    }
                  }
               }
            }""";

    public static final String STREAMING_ERROR_EVENT = """
            data: {
                  "jsonrpc": "2.0",
                  "id": "1234",
                  "error": {
                    "code": -32602,
                    "message": "Invalid parameters",
                    "data": "Missing required field"
                  }
             }""";

    public static final String STREAMING_COMPLETE_SEQUENCE = 
            STREAMING_TASK_EVENT + 
            STREAMING_MESSAGE_EVENT + 
            STREAMING_ARTIFACT_UPDATE_EVENT + 
            STREAMING_STATUS_UPDATE_EVENT;
} 