package io.a2a.server.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventQueue {
    // TODO decide on a capacity (or more appropriate queue data structures)
    private final BlockingQueue<Event> queue = new ArrayBlockingQueue<Event>(1000);
    private final List<EventQueue> children = new ArrayList<>();

    public void enqueueEvent(Event event) {
        queue.add(event);
        children.forEach(eq -> eq.enqueueEvent(event));
    }

    public Event dequeueEvent(int waitMilliSeconds) {
        if (waitMilliSeconds <= 0) {
            return queue.poll();
        }
        try {
            return queue.poll(waitMilliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void taskDone() {
        // TODO Not sure if needed yet. BlockingQueue.poll()/.take() remove the events.
    }

    public EventQueue tap() {
        EventQueue child = new EventQueue();
        children.add(child);
        return child;
    }

    public void close() {
        queue.drainTo(new ArrayList<>());
        children.forEach(EventQueue::close);

    }
}
