package io.a2a.client;

/**
 * Request and response messages used by the tests. These have been created following examples from
 * the <a href="https://google.github.io/A2A/specification/sample-messages">A2A sample messages</a>.
 */
public class JsonMessages {

    static final String AGENT_CARD = """
            {
              "name": "Google Maps Agent",
              "description": "Plan routes, remember places, and generate directions",
              "url": "https://maps-agent.google.com",
              "provider": {
                "organization": "Google",
                "url": "https://google.com"
              },
              "version": "1.0.0",
              "authentication": {
                "schemes": ["OAuth2"]
              },
              "defaultInputModes": [
                "text/plain"
              ],
              "defaultOutputModes": [
                "text/plain",
                "application/html"
              ],
              "capabilities": {
                "streaming": true,
                "pushNotifications": false
              },
              "skills": [
                {
                  "id": "route-planner",
                  "name": "Route planning",
                  "description": "Helps plan routing between two locations",
                  "tags": [
                    "maps",
                    "routing",
                    "navigation"
                  ],
                  "examples": [
                    "plan my route from Sunnyvale to Mountain View",
                    "what's the commute time from Sunnyvale to San Francisco at 9AM",
                    "create turn by turn directions from Sunnyvale to Mountain View"
                  ],
                  "outputModes": [
                    "application/html",
                    "video/mp4"
                  ]
                },
                {
                  "id": "custom-map",
                  "name": "My Map",
                  "description": "Manage a custom map with your own saved places",
                  "tags": [
                    "custom-map",
                    "saved-places"
                  ],
                  "examples": [
                    "show me my favorite restaurants on the map",
                    "create a visual of all places I've visited in the past year"
                  ],
                  "outputModes": [
                    "application/html"
                  ]
                }
              ]
            }""";

    static final String SEND_TASK_TEST_REQUEST = """
                {
                 "jsonrpc": "2.0",
                 "id": "request-1234",
                 "method": "tasks/send",
                 "params": {
                  "id": "task-1234",
                  "sessionId": "session-1234",
                  "message": {
                   "role": "user",
                   "parts": [
                    {
                     "type": "text",
                     "text": "tell me a joke"
                    }
                   ]
                  }
                 }
                }""";

    static final String SEND_TASK_TEST_RESPONSE = """
            {
             "jsonrpc": "2.0",
             "id": 1,
             "result": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64",
              "sessionId": "c295ea44-7543-4f78-b524-7a38915ad6e4",
              "status": {
               "state": "completed"
              },
              "artifacts": [
               {
                "name": "joke",
                "parts": [
                 {
                  "type": "text",
                  "text": "Why did the chicken cross the road? To get to the other side!"
                 }
                ]
               }
              ],
              "metadata": {}
             }
            }""";

    static final String SEND_TASK_WITH_ERROR_TEST_REQUEST = """
                {
                 "jsonrpc": "2.0",
                 "id": "request-1234-with-error",
                 "method": "tasks/send",
                 "params": {
                  "id": "task-1234",
                  "sessionId": "session-1234",
                  "message": {
                   "role": "user",
                   "parts": [
                    {
                     "type": "text",
                     "text": "tell me a joke"
                    }
                   ]
                  }
                 }
                }""";

    static final String SEND_TASK_ERROR_TEST_RESPONSE = """
            {
             "jsonrpc": "2.0",
             "error": {
                "code": -32702,
                "message": "Invalid parameters",
                "data": "Hello world"
             }
            }""";

    static final String GET_TASK_TEST_REQUEST = """
            {
             "jsonrpc": "2.0",
             "id": "request-1234",
             "method": "tasks/get",
             "params": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64",
              "historyLength": 10,
             }
            }
            """;

    static final String GET_TASK_TEST_RESPONSE = """
            {
             "jsonrpc": "2.0",
             "id": 1,
             "result": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64",
              "sessionId": "c295ea44-7543-4f78-b524-7a38915ad6e4",
              "status": {
               "state": "completed"
              },
              "artifacts": [
               {
                "parts": [
                 {
                  "type": "text",
                  "text": "Why did the chicken cross the road? To get to the other side!"
                 }
                ]
               }
              ],
              "history": [
               {
                "role": "user",
                "parts": [
                 {
                  "type": "text",
                  "text": "tell me a joke"
                 }
                ]
               }
              ],
              "metadata": {}
             }
            }
            """;

    static final String CANCEL_TASK_TEST_REQUEST = """
            {
             "jsonrpc": "2.0",
             "id": "request-1234",
             "method": "tasks/cancel",
             "params": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64",
              "metadata": {}
             }
            }
            """;

    static final String CANCEL_TASK_TEST_RESPONSE = """
            {
             "jsonrpc": "2.0",
             "id": 1,
             "result": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64",
              "sessionId": "c295ea44-7543-4f78-b524-7a38915ad6e4",
              "status": {
               "state": "canceled"
              },
              "metadata": {}
             }
            }
            """;

    static final String GET_TASK_PUSH_NOTIFICATION_CONFIG_TEST_REQUEST = """
            {
             "jsonrpc": "2.0",
             "id": "1",
             "method": "tasks/pushNotification/get",
             "params": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64"
             }
            }
            """;

    static final String GET_TASK_PUSH_NOTIFICATION_CONFIG_TEST_RESPONSE = """
            {
             "jsonrpc": "2.0",
             "id": 1,
             "result": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64",
              "pushNotificationConfig": {
               "url": "https://example.com/callback",
               "authentication": {
                "schemes": ["jwt"]
               }
              }
             }
            }
            """;

    static final String SET_TASK_PUSH_NOTIFICATION_CONFIG_TEST_REQUEST = """
            {
             "jsonrpc": "2.0",
             "id": "1",
             "method": "tasks/pushNotification/set",
             "params": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64",
              "pushNotificationConfig": {
               "url": "https://example.com/callback",
               "authentication": {
                "schemes": ["jwt"]
               }
              }
             }
            }""";

    static final String SET_TASK_PUSH_NOTIFICATION_CONFIG_TEST_RESPONSE = """
            {
             "jsonrpc": "2.0",
             "id": 1,
             "result": {
              "id": "de38c76d-d54c-436c-8b9f-4c2703648d64",
              "pushNotificationConfig": {
               "url": "https://example.com/callback",
               "authentication": {
                "schemes": ["jwt"]
               }
              }
             }
            }
            """;

}
