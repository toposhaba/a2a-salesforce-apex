package io.a2a.server.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public abstract class EventQueue {
    private final EventQueue parent;
    // TODO decide on a capacity (or more appropriate queue data structures)
    private final BlockingQueue<Event> queue = new ArrayBlockingQueue<Event>(1000);
    private volatile boolean closed = false;


    protected EventQueue() {
        parent = null;
    }

    protected EventQueue(EventQueue parent) {
        this.parent = parent;
    }

    public static EventQueue create() {
        return new MainQueue();
    }

    public void enqueueEvent(Event event) {
        if (closed) {
            throw new IllegalStateException("EventQueue is closed");
        }
        queue.add(event);
    }

    abstract EventQueue tap();

    public Event dequeueEvent(int waitMilliSeconds) throws EventQueueClosedException {
        if (closed && queue.isEmpty()) {
            throw new EventQueueClosedException();
        }
        if (waitMilliSeconds <= 0) {
            return queue.poll();
        }
        try {
            Event event = queue.poll(waitMilliSeconds, TimeUnit.MILLISECONDS);
            return event;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void taskDone() {
        // TODO Not sure if needed yet. BlockingQueue.poll()/.take() remove the events.
    }

    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        queue.drainTo(new ArrayList<>());
    }

    static class MainQueue extends EventQueue {
        private final List<ChildQueue> children = new CopyOnWriteArrayList<>();

        EventQueue tap() {
            ChildQueue child = new ChildQueue(this);
            children.add(child);
            return child;
        }

        public void enqueueEvent(Event event) {
            super.enqueueEvent(event);
            children.forEach(eq -> eq.internalEnqueueEvent(event));
        }

        @Override
        public void close() {
            super.close();
            children.forEach(EventQueue::close);
        }
    }

    static class ChildQueue extends EventQueue {
        private final MainQueue parent;

        public ChildQueue(MainQueue parent) {
            this.parent = parent;
        }

        @Override
        public void enqueueEvent(Event event) {
            parent.enqueueEvent(event);
        }

        private void internalEnqueueEvent(Event event) {
            super.enqueueEvent(event);
        }

        @Override
        EventQueue tap() {
            throw new IllegalStateException("Can only tap the main queue");
        }
    }
}
