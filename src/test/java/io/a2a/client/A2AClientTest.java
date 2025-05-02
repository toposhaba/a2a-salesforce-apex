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

    private static final String TASK_RESPONSE = "{\n" +
            " \"jsonrpc\": \"2.0\",\n" +
            " \"id\": 1,\n" +
            " \"result\": {\n" +
            "  \"id\": \"de38c76d-d54c-436c-8b9f-4c2703648d64\",\n" +
            "  \"sessionId\": \"c295ea44-7543-4f78-b524-7a38915ad6e4\",\n" +
            "  \"status\": {\n" +
            "   \"state\": \"completed\"\n" +
            "  },\n" +
            "  \"artifacts\": [\n" +
            "   {\n" +
            "    \"name\": \"joke\",\n" +
            "    \"parts\": [\n" +
            "     {\n" +
            "      \"type\": \"text\",\n" +
            "      \"text\": \"Why did the chicken cross the road? To get to the other side!\"\n" +
            "     }\n" +
            "    ]\n" +
            "   }\n" +
            "  ],\n" +
            "  \"metadata\": {}\n" +
            " }\n" +
            "}";

    private static final String AGENT_CARD = "{\n" +
            "  \"name\": \"Google Maps Agent\",\n" +
            "  \"description\": \"Plan routes, remember places, and generate directions\",\n" +
            "  \"url\": \"https://maps-agent.google.com\",\n" +
            "  \"provider\": {\n" +
            "    \"organization\": \"Google\",\n" +
            "    \"url\": \"https://google.com\"\n" +
            "  },\n" +
            "  \"version\": \"1.0.0\",\n" +
            "  \"authentication\": {\n" +
            "    \"schemes\": [\"OAuth2\"]\n" +
            "  },\n" +
            "  \"defaultInputModes\": [\n" +
            "    \"text/plain\"\n" +
            "  ],\n" +
            "  \"defaultOutputModes\": [\n" +
            "    \"text/plain\",\n" +
            "    \"application/html\"\n" +
            "  ],\n" +
            "  \"capabilities\": {\n" +
            "    \"streaming\": true,\n" +
            "    \"pushNotifications\": false\n" +
            "  },\n" +
            "  \"skills\": [\n" +
            "    {\n" +
            "      \"id\": \"route-planner\",\n" +
            "      \"name\": \"Route planning\",\n" +
            "      \"description\": \"Helps plan routing between two locations\",\n" +
            "      \"tags\": [\n" +
            "        \"maps\",\n" +
            "        \"routing\",\n" +
            "        \"navigation\"\n" +
            "      ],\n" +
            "      \"examples\": [\n" +
            "        \"plan my route from Sunnyvale to Mountain View\",\n" +
            "        \"what's the commute time from Sunnyvale to San Francisco at 9AM\",\n" +
            "        \"create turn by turn directions from Sunnyvale to Mountain View\"\n" +
            "      ],\n" +
            "      \"outputModes\": [\n" +
            "        \"application/html\",\n" +
            "        \"video/mp4\"\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"custom-map\",\n" +
            "      \"name\": \"My Map\",\n" +
            "      \"description\": \"Manage a custom map with your own saved places\",\n" +
            "      \"tags\": [\n" +
            "        \"custom-map\",\n" +
            "        \"saved-places\"\n" +
            "      ],\n" +
            "      \"examples\": [\n" +
            "        \"show me my favorite restaurants on the map\",\n" +
            "        \"create a visual of all places I've visited in the past year\"\n" +
            "      ],\n" +
            "      \"outputModes\": [\n" +
            "        \"application/html\"\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}