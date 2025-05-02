package io.a2a.spec;

/**
 * Constants related to the A2A protocol.
 */
public class A2A {

    public static final String CANCEL_TASK_REQUEST = "tasks/cancel";
    public static final String GET_TASK_PUSH_NOTIFICATION_REQUEST = "tasks/pushNotification/get";
    public static final String GET_TASK_REQUEST = "tasks/get";
    public static final String SET_TASK_PUSH_NOTIFICATION_REQUEST = "tasks/pushNotification/set";
    public static final String SEND_TASK_REQUEST = "tasks/send";
    public static final String SEND_TASK_RESUBSCRIPTION_REQUEST = "tasks/resubscribe";
    public static final String SEND_TASK_STREAMING_REQUEST = "tasks/sendSubscribe";

    public static final String JSONRPC_VERSION = "2.0";

    public static final String AGENT_CARD_REQUEST = ".well-known/agent.json";

    public static final String getRequestEndpoint(String agentUrl, String request) {
        return agentUrl.endsWith("/") ? agentUrl + request : agentUrl + "/" + request;
    }
}
