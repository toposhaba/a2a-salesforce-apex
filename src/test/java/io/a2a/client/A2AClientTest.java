package io.a2a.client;

import static io.a2a.client.JsonMessages.AGENT_CARD;
import static io.a2a.client.JsonMessages.CANCEL_TASK_TEST_REQUEST;
import static io.a2a.client.JsonMessages.CANCEL_TASK_TEST_RESPONSE;
import static io.a2a.client.JsonMessages.GET_TASK_PUSH_NOTIFICATION_CONFIG_TEST_REQUEST;
import static io.a2a.client.JsonMessages.GET_TASK_PUSH_NOTIFICATION_CONFIG_TEST_RESPONSE;
import static io.a2a.client.JsonMessages.GET_TASK_TEST_REQUEST;
import static io.a2a.client.JsonMessages.GET_TASK_TEST_RESPONSE;
import static io.a2a.client.JsonMessages.SEND_TASK_ERROR_TEST_RESPONSE;
import static io.a2a.client.JsonMessages.SEND_TASK_TEST_REQUEST;
import static io.a2a.client.JsonMessages.SEND_TASK_TEST_RESPONSE;
import static io.a2a.client.JsonMessages.SEND_TASK_WITH_ERROR_TEST_REQUEST;
import static io.a2a.spec.A2A.toUserMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.HashMap;
import java.util.List;

import io.a2a.spec.AuthenticationInfo;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskPushNotificationRequest;
import io.a2a.spec.GetTaskPushNotificationResponse;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.JSONRPCError;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.JsonBody;

import io.a2a.spec.Artifact;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.PushNotificationConfig;
import io.a2a.spec.SendTaskResponse;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskSendParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;

public class A2AClientTest {

    private ClientAndServer server;

    @BeforeEach
    public void setUp() {
        server = new ClientAndServer(4001);
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testA2AClientSendTask() throws Exception {
        this.server.when(
                        request()
                                .withMethod("POST")
                                .withPath("/tasks/send")
                                .withBody(JsonBody.json(SEND_TASK_TEST_REQUEST))

                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(SEND_TASK_TEST_RESPONSE)
                );

        A2AClient client = new A2AClient("http://localhost:4001");
        Message message = toUserMessage("tell me a joke");
        TaskSendParams params = new TaskSendParams.Builder()
                .id("task-1234")
                .sessionId("session-1234")
                .message(message)
                .build();

        SendTaskResponse response = client.sendTask("request-1234", params);

        assertEquals("2.0", response.getJsonrpc());
        assertNotNull(response.getId());
        Object result = response.getResult();
        assertInstanceOf(Task.class, result);
        Task task = (Task) result;
        assertEquals("de38c76d-d54c-436c-8b9f-4c2703648d64", task.id());
        assertNotNull(task.sessionId());
        assertEquals(TaskState.COMPLETED,task.status().state());
        assertEquals(1, task.artifacts().size());
        Artifact artifact = task.artifacts().get(0);
        assertEquals("joke", artifact.name());
        assertEquals(1, artifact.parts().size());
        Part<?> part = artifact.parts().get(0);
        assertEquals(Part.Type.TEXT, part.getType());
        assertEquals("Why did the chicken cross the road? To get to the other side!", ((TextPart) part).getText());
        assertTrue(task.metadata().isEmpty());
    }

    @Test
    public void testA2AClientSendTaskWithError() throws Exception {
        this.server.when(
                        request()
                                .withMethod("POST")
                                .withPath("/tasks/send")
                                .withBody(JsonBody.json(SEND_TASK_WITH_ERROR_TEST_REQUEST))

                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(SEND_TASK_ERROR_TEST_RESPONSE)
                );

        A2AClient client = new A2AClient("http://localhost:4001");
        Message message = toUserMessage("tell me a joke");
        TaskSendParams params = new TaskSendParams.Builder()
                .id("task-1234")
                .sessionId("session-1234")
                .message(message)
                .build();

        SendTaskResponse response = client.sendTask("request-1234-with-error", params);

        assertEquals("2.0", response.getJsonrpc());
        assertNotNull(response.getId()); // Not in JSON so it is generated
        Object result = response.getResult();
        assertNull(result);
        JSONRPCError error = response.getError();
        assertNotNull(error);
        assertEquals(-32702, error.getCode());
        assertEquals("Invalid parameters", error.getMessage());
        assertEquals("Hello world", error.getData());
    }

    @Test
    public void testA2AClientGetTask() throws Exception {
        this.server.when(
                        request()
                                .withMethod("POST")
                                .withPath("/tasks/get")
                                .withBody(JsonBody.json(GET_TASK_TEST_REQUEST))

                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(GET_TASK_TEST_RESPONSE)
                );

        A2AClient client = new A2AClient("http://localhost:4001");
        GetTaskResponse response = client.getTask("request-1234",
                new TaskQueryParams("de38c76d-d54c-436c-8b9f-4c2703648d64", 10));

        assertEquals("2.0", response.getJsonrpc());
        assertEquals(1, response.getId());
        Object result = response.getResult();
        assertInstanceOf(Task.class, result);
        Task task = (Task) result;
        assertEquals("de38c76d-d54c-436c-8b9f-4c2703648d64", task.id());
        assertEquals("c295ea44-7543-4f78-b524-7a38915ad6e4", task.sessionId());
        assertEquals(TaskState.COMPLETED, task.status().state());
        assertEquals(1, task.artifacts().size());
        Artifact artifact = task.artifacts().get(0);
        assertEquals(1, artifact.parts().size());
        Part<?> part = artifact.parts().get(0);
        assertEquals(Part.Type.TEXT, part.getType());
        assertEquals("Why did the chicken cross the road? To get to the other side!", ((TextPart) part).getText());
        assertTrue(task.metadata().isEmpty());
        List<Message> history = task.history();
        assertNotNull(history);
        assertEquals(1, history.size());
        Message message = history.get(0);
        assertEquals(Message.Role.USER, message.role());
        List<Part> parts = message.parts();
        assertNotNull(parts);
        assertEquals(1, parts.size());
        part = parts.get(0);
        assertEquals(Part.Type.TEXT, part.getType());
        assertEquals("tell me a joke", ((TextPart)part).getText());
        assertTrue(task.metadata().isEmpty());
    }

    @Test
    public void testA2AClientCancelTask() throws Exception {
        this.server.when(
                        request()
                                .withMethod("POST")
                                .withPath("/tasks/cancel")
                                .withBody(JsonBody.json(CANCEL_TASK_TEST_REQUEST))

                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(CANCEL_TASK_TEST_RESPONSE)
                );

        A2AClient client = new A2AClient("http://localhost:4001");
        CancelTaskResponse response = client.cancelTask("request-1234",
                new TaskIdParams("de38c76d-d54c-436c-8b9f-4c2703648d64", new HashMap<>()));

        assertEquals("2.0", response.getJsonrpc());
        assertEquals(1, response.getId());
        Object result = response.getResult();
        assertInstanceOf(Task.class, result);
        Task task = (Task) result;
        assertEquals("de38c76d-d54c-436c-8b9f-4c2703648d64", task.id());
        assertEquals("c295ea44-7543-4f78-b524-7a38915ad6e4", task.sessionId());
        assertEquals(TaskState.CANCELED, task.status().state());
        assertTrue(task.metadata().isEmpty());
    }

    @Test
    public void testA2AClientGetTaskPushNotificationConfig() throws Exception {
        this.server.when(
                        request()
                                .withMethod("POST")
                                .withPath("/tasks/pushNotification/get")
                                .withBody(JsonBody.json(GET_TASK_PUSH_NOTIFICATION_CONFIG_TEST_REQUEST))

                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(GET_TASK_PUSH_NOTIFICATION_CONFIG_TEST_RESPONSE)
                );

        A2AClient client = new A2AClient("http://localhost:4001");
        GetTaskPushNotificationResponse response = client.getTaskPushNotificationConfig("1",
                new TaskIdParams("de38c76d-d54c-436c-8b9f-4c2703648d64", new HashMap<>()));
        assertEquals("2.0", response.getJsonrpc());
        assertEquals(1, response.getId());
        assertInstanceOf(TaskPushNotificationConfig.class, response.getResult());
        TaskPushNotificationConfig taskPushNotificationConfig = (TaskPushNotificationConfig) response.getResult();
        PushNotificationConfig pushNotificationConfig = taskPushNotificationConfig.pushNotificationConfig();
        assertNotNull(pushNotificationConfig);
        assertEquals("https://example.com/callback", pushNotificationConfig.url());
        AuthenticationInfo authenticationInfo = pushNotificationConfig.authentication();
        assertTrue(authenticationInfo.schemes().size() == 1);
        assertEquals("jwt", authenticationInfo.schemes().get(0));
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
}