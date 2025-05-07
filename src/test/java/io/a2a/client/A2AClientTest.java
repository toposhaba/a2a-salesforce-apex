package io.a2a.client;

import static io.a2a.spec.A2A.toUserMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import io.a2a.spec.Artifact;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.SendTaskResponse;
import io.a2a.spec.Task;
import io.a2a.spec.TaskSendParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;

public class A2AClientTest {

    private static ClientAndServer server;

    @BeforeAll
    public static void setUp() {
        server = new ClientAndServer(4001);
    }

    @AfterAll
    public static void tearDown() {
        server.stop();
    }

    @Test
    public void testA2AClientSendTask() throws Exception {
        this.server.when(
                        request()
                                .withMethod("POST")
                                .withPath("/tasks/send")

                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(TASK_RESPONSE)
                );

        A2AClient client = new A2AClient("http://localhost:4001");
        Message message = toUserMessage("tell me a joke");
        TaskSendParams params = new TaskSendParams.Builder()
                .id("task-1234")
                .message(message)
                .build();

        SendTaskResponse response = client.sendTask(params);

        assertEquals("2.0", response.jsonrpc());
        assertTrue(response.id() != null);
        Object result = response.result();
        assertTrue(result instanceof Task);
        Task task = (Task) result;
        assertEquals("de38c76d-d54c-436c-8b9f-4c2703648d64", task.id());
        assertTrue(task.sessionId() != null);
        assertTrue(task.status().state() == TaskState.COMPLETED);
        assertTrue(task.artifacts().size() == 1);
        Artifact artifact = task.artifacts().get(0);
        assertEquals("joke", artifact.name());
        assertTrue(artifact.parts().size() == 1);
        Part part = artifact.parts().get(0);
        assertEquals(Part.Type.TEXT, part.getType());
        assertEquals("Why did the chicken cross the road? To get to the other side!", ((TextPart) part).getText());
        assertTrue(task.metadata().isEmpty());
    }

    @Test
    public void testA2AClientGetAgentCard() throws Exception {
        this.server.when(
                        request()
                                .withMethod("GET")
                                .withPath("/.well-known/agent.json")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(AGENT_CARD)
                );

        A2AClient client = new A2AClient("http://localhost:4001");
        assertEquals("Google Maps Agent", client.getAgentCard().name());
    }

    private static final String TASK_RESPONSE = """
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

    private static final String AGENT_CARD = """
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
}