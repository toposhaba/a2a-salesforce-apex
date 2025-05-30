package io.a2a.examples.helloworld.client;

import io.a2a.client.A2AClient;
import io.a2a.spec.A2A;
import io.a2a.spec.A2AServerException;
import io.a2a.spec.Message;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.SendMessageResponse;

/**
 * A simple example of using the A2A Java SDK to communicate with an A2A server.
 * This example is equivalent to the Python example provided in the A2A Python SDK.
 */
public class HelloWorldClient {

    private static final String SERVER_URL = "http://localhost:9999";
    private static final String MESSAGE_TEXT = "how much is 10 USD in INR?";

    public static void main(String[] args) {
        try {
            // Create an A2AClient
            A2AClient client = new A2AClient(SERVER_URL);

            Message message = A2A.toUserMessage(MESSAGE_TEXT); // the message ID will be automatically generated for you
            MessageSendParams params = new MessageSendParams.Builder()
                .message(message)
                .build();
            SendMessageResponse response = client.sendMessage(params);
            System.out.println("Message sent with ID: " + response.getId());
            System.out.println("Response: " + response.toString());
        } catch (A2AServerException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

} 