package io.a2a.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import io.a2a.spec.Message;
import io.a2a.spec.SendTaskResponse;
import io.a2a.spec.TaskSendParams;
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