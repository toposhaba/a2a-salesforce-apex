package io.a2a.server.apps;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.wildfly.common.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.CancelTaskRequest;
import io.a2a.spec.CancelTaskResponse;
import io.a2a.spec.GetTaskRequest;
import io.a2a.spec.GetTaskResponse;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.Part;
import io.a2a.spec.SendMessageRequest;
import io.a2a.spec.SendMessageResponse;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskNotFoundError;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TextPart;
import io.a2a.spec.UnsupportedOperationError;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
public class A2AServerResourceTest {

    @Inject
    TaskStore taskStore;

    private static final Task MINIMAL_TASK = new Task.Builder()
            .id("task-123")
            .contextId("session-xyz")
            .status(new TaskStatus(TaskState.SUBMITTED))
            .build();

    private static final Task CANCEL_TASK = new Task.Builder()
            .id("cancel-task-123")
            .contextId("session-xyz")
            .status(new TaskStatus(TaskState.SUBMITTED))
            .build();

    private static final Task CANCEL_TASK_NOT_SUPPORTED = new Task.Builder()
            .id("cancel-task-not-supported-123")
            .contextId("session-xyz")
            .status(new TaskStatus(TaskState.SUBMITTED))
            .build();

    private static final Message MESSAGE = new Message.Builder()
            .messageId("111")
            .role(Message.Role.AGENT)
            .parts(new TextPart("test message"))
            .build();

    @Test
    public void testGetTaskSuccess() throws Exception {
        taskStore.save(MINIMAL_TASK);
        try {
            GetTaskRequest request = new GetTaskRequest("1", new TaskQueryParams(MINIMAL_TASK.getId()));
            GetTaskResponse response = given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .when()
                    .post("/")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(GetTaskResponse.class);
            assertEquals("1", response.getId());
            assertEquals("task-123", response.getResult().getId());
            assertEquals("session-xyz", response.getResult().getContextId());
            assertEquals(TaskState.SUBMITTED, response.getResult().getStatus().state());
            assertNull(response.getError());
        } catch (Exception e) {
        } finally {
            taskStore.delete(MINIMAL_TASK.getId());
        }
    }

    @Test
    public void testGetTaskNotFound() throws Exception {
        assertTrue(taskStore.get("non-existent-task") == null);
        GetTaskRequest request = new GetTaskRequest("1", new TaskQueryParams("non-existent-task"));
        GetTaskResponse response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/")
                .then()
                .statusCode(200)
                .extract()
                .as(GetTaskResponse.class);
        assertEquals("1", response.getId());
        // this should be an instance of TaskNotFoundError, see https://github.com/fjuma/a2a-java-sdk/issues/23
        assertInstanceOf(JSONRPCError.class, response.getError());
        assertEquals(new TaskNotFoundError().getCode(), response.getError().getCode());
        assertNull(response.getResult());
    }

    @Test
    public void testCancelTaskSuccess() throws Exception {
        taskStore.save(CANCEL_TASK);
        try {
            CancelTaskRequest request = new CancelTaskRequest("1", new TaskIdParams(CANCEL_TASK.getId()));
            CancelTaskResponse response = given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .when()
                    .post("/")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(CancelTaskResponse.class);
            assertNull(response.getError());
            assertEquals(request.getId(), response.getId());
            Task task = response.getResult();
            assertEquals(CANCEL_TASK.getId(), task.getId());
            assertEquals(CANCEL_TASK.getContextId(), task.getContextId());
            assertEquals(TaskState.CANCELED, task.getStatus().state());
        } catch (Exception e) {
        } finally {
            taskStore.delete(CANCEL_TASK.getId());
        }
    }

    @Test
    public void testCancelTaskNotSupported() throws Exception {
        taskStore.save(CANCEL_TASK_NOT_SUPPORTED);
        try {
            CancelTaskRequest request = new CancelTaskRequest("1", new TaskIdParams(CANCEL_TASK_NOT_SUPPORTED.getId()));
            CancelTaskResponse response = given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .when()
                    .post("/")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(CancelTaskResponse.class);
            assertEquals(request.getId(), response.getId());
            assertNull(response.getResult());
            // this should be an instance of UnsupportedOperationError, see https://github.com/fjuma/a2a-java-sdk/issues/23
            assertInstanceOf(JSONRPCError.class, response.getError());
            assertEquals(new UnsupportedOperationError().getCode(), response.getError().getCode());
        } catch (Exception e) {
        } finally {
            taskStore.delete(CANCEL_TASK_NOT_SUPPORTED.getId());
        }
    }

    @Test
    public void testCancelTaskNotFound() throws Exception {
        CancelTaskRequest request = new CancelTaskRequest("1", new TaskIdParams("non-existent-task"));
        CancelTaskResponse response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/")
                .then()
                .statusCode(200)
                .extract()
                .as(CancelTaskResponse.class);
        assertEquals(request.getId(), response.getId());
        assertNull(response.getResult());
        // this should be an instance of UnsupportedOperationError, see https://github.com/fjuma/a2a-java-sdk/issues/23
        assertInstanceOf(JSONRPCError.class, response.getError());
        assertEquals(new TaskNotFoundError().getCode(), response.getError().getCode());
    }

    @Test
    public void testSendMessageNewMessageSuccess() throws Exception {
        assertTrue(taskStore.get(MINIMAL_TASK.getId()) == null);
        Message message = new Message.Builder(MESSAGE)
                .taskId(MINIMAL_TASK.getId())
                .contextId(MINIMAL_TASK.getContextId())
                .build();
        SendMessageRequest request = new SendMessageRequest("1", new MessageSendParams("1", message, null, null));
        SendMessageResponse response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when()
                .post("/")
                .then()
                .statusCode(200)
                .extract()
                .as(SendMessageResponse.class);
        assertNull(response.getError());
        Message messageResponse =  (Message) response.getResult();
        assertEquals(MESSAGE.getMessageId(), messageResponse.getMessageId());
        assertEquals(MESSAGE.getRole(), messageResponse.getRole());
        Part<?> part = messageResponse.getParts().get(0);
        assertEquals(Part.Type.TEXT, part.getType());
        assertEquals("test message", ((TextPart) part).getText());
    }

    @Test
    public void testSendMessageExistingTaskSuccess() throws Exception {
        // Need to figure out how to tweak the AgentExecutorProducer so it works for this test and others
        taskStore.save(MINIMAL_TASK);
        try {
            Message message = new Message.Builder(MESSAGE)
                    .taskId(MINIMAL_TASK.getId())
                    .contextId(MINIMAL_TASK.getContextId())
                    .build();
            SendMessageRequest request = new SendMessageRequest("1", new MessageSendParams("1", message, null, null));
            SendMessageResponse response = given()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .when()
                    .post("/")
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(SendMessageResponse.class);
            assertNull(response.getError());
            Message messageResponse = (Message) response.getResult();
            assertEquals(MESSAGE.getMessageId(), messageResponse.getMessageId());
            assertEquals(MESSAGE.getRole(), messageResponse.getRole());
            Part<?> part = messageResponse.getParts().get(0);
            assertEquals(Part.Type.TEXT, part.getType());
            assertEquals("test message", ((TextPart) part).getText());
        } catch (Exception e) {
        } finally {
            taskStore.delete(MINIMAL_TASK.getId());
        }
    }
}
