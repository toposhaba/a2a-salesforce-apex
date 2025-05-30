package io.a2a.http;

import io.a2a.spec.Task;

public interface A2AHttpClient {
    int post(String url, Task task);
}
