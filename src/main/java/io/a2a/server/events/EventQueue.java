package io.a2a.server.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.a2a.util.TempLoggerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventQueue {

    private static final Logger log = new TempLoggerWrapper(LoggerFactory.getLogger(EventQueue.class));

    private final EventQueue parent;
    // TODO decide on a capacity (or more appropriate queue data structures)
    private final BlockingQueue<Event> queue = new ArrayBlockingQueue<Event>(1000);
    private volatile boolean closed = false;
    private CountDownLatch pollingStartedLatch = new CountDownLatch(1);


    protected EventQueue() {
        this(null);
    }

    protected EventQueue(EventQueue parent) {
        log.trace("Creating {}, parent: {}", this, parent);
        this.parent = parent;
    }

    public static EventQueue create() {
        return new MainQueue();
    }

    public void enqueueEvent(Event event) {
        if (closed) {
            log.warn("Queue is closed. Event will not be enqueued. {} {}", this, event);
            return;
            //throw new IllegalStateException("EventQueue is closed");
        }
        // Call toString() since for errors we don't really want the full stacktrace
        queue.add(event);
        log.debug("Enqueued event {} {}", event instanceof Throwable ? event.toString() : event, this);
    }

    // TODO This is internal use only, move somewhere else if it works (possibly make package private, and expose via the queue manager)
    public CountDownLatch getPollingStartedLatch() {
        return pollingStartedLatch;
    }

    abstract EventQueue tap();

    public Event dequeueEvent(int waitMilliSeconds) throws EventQueueClosedException {
        System.out.println(queue);
        if (closed && queue.isEmpty()) {
            log.debug("Queue is closed, and empty. Sending termination message. {}", this);
            throw new EventQueueClosedException();
        }
        try {
            if (waitMilliSeconds <= 0) {
                Event event = queue.poll();
                if (event != null) {
                    // Call toString() since for errors we don't really want the full stacktrace
                    log.debug("Dequeued event (no wait) {} {}", this, event instanceof Throwable ? event.toString() : event);
                }
                return event;
            }
            try {
                Event event = queue.poll(waitMilliSeconds, TimeUnit.MILLISECONDS);
                if (event != null) {
                    // Call toString() since for errors we don't really want the full stacktrace
                    log.debug("Dequeued event (waiting) {} {}", this, event instanceof Throwable ? event.toString() : event);
                }
                return event;
            } catch (InterruptedException e) {
                log.debug("Interrupted {}", this);
                Thread.currentThread().interrupt();
                return null;
            }
        } finally {
            log.debug("Signalling that queue polling started {}", this);
            pollingStartedLatch.countDown();
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
            log.debug("Closing {}", this);
            closed = true;
        }
        // Although the Python implementation drains the queue on closing,
        // here it makes events go missing
        // TODO do we actually need to drain it? If we do, we need some mechanism to determine that noone is
        //  polling any longer and drain it asynchronously once it is all done. That could perhaps be done
        //  via an EnhancedRunnable.DoneCallback.
        //queue.drainTo(new ArrayList<>());
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
