package io.a2a.util;

import static io.a2a.util.AsyncUtils.consumer;
import static io.a2a.util.AsyncUtils.convertingProcessor;
import static io.a2a.util.AsyncUtils.createTubeConfig;
import static io.a2a.util.AsyncUtils.processor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import mutiny.zero.ZeroPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AsyncUtilsTest {

    @Test
    public void testConsumer() throws Exception {
        List<String> toSend = List.of("A", "B", "C");
        Flow.Publisher<String> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        consumer(createTubeConfig(), publisher, (errorConsumer, s) -> {
            received.add(s);
            latch.countDown();
            return true;
        });


        latch.await(2, TimeUnit.SECONDS);
        assertEquals(toSend, received);
    }

    @Test
    public void testCancelConsumer() throws Exception {
        List<String> toSend = List.of("A", "B", "C", "D");
        Flow.Publisher<String> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        consumer(createTubeConfig(), publisher, (errorConsumer, s) -> {
            latch.countDown();
            if (s.equals("C")) {
                return false;
            }
            received.add(s);
            return true;
        });

        Thread.sleep(500);
        assertEquals(toSend.subList(0, 2), received);
    }

    @Test
    public void testErrorConsumer() throws Exception {
        List<String> toSend = List.of("A", "B", "C", "D");
        Flow.Publisher<String> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        consumer(createTubeConfig(), publisher, (errorConsumer, s) -> {
            try {
                latch.countDown();
                if (s.equals("C")) {
                    throw new IllegalStateException();
                }
                received.add(s);
            } catch (Exception e) {
                errorConsumer.accept(e);
            }
            return true;
        });

        Thread.sleep(500);
        assertEquals(toSend.subList(0, 2), received);
    }

    @Test
    public void testUncaughtErrorConsumer() throws Exception {
        List<String> toSend = List.of("A", "B", "C", "D");
        Flow.Publisher<String> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        consumer(createTubeConfig(), publisher, (errorConsumer, s) -> {
            latch.countDown();
            if (s.equals("C")) {
                throw new IllegalStateException();
            }
            received.add(s);
            return true;
        });

        Thread.sleep(500);
        assertEquals(toSend.subList(0, 2), received);
    }

    @Test
    public void testProcessor() throws Exception {
        List<String> toSend = List.of("A", "B", "C");
        Flow.Publisher<String> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        List<String> processed = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        Flow.Publisher<String> processedPublisher =
                processor(createTubeConfig(), publisher, (errorConsumer, s) -> {
            processed.add(s);
            latch.countDown();
            return true;
        });

        processedPublisher.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                subscription.request(1);
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(toSend, received);
        assertEquals(toSend, processed);
    }

    @Test
    public void testErrorProcessor() throws Exception {
        List<String> toSend = List.of("A", "B", "C", "D");
        Flow.Publisher<String> publisher = ZeroPublisher.fromIterable(toSend);

        AtomicReference<Throwable> error = new AtomicReference<>();
        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);


        Flow.Publisher<String> processedPublisher =
                processor(createTubeConfig(), publisher, (errorConsumer, s) -> {
            latch.countDown();
            if (s.equals("C")) {
                errorConsumer.accept(new IllegalStateException());
            }
            return true;
        });

        processedPublisher.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                subscription.request(1);
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Thread.sleep(500);
        assertEquals(toSend.subList(0, 2), received);
        assertNotNull(error.get());
    }

    @Test
    public void testUncaughtErrorProcessor() throws Exception {
        List<String> toSend = List.of("A", "B", "C", "D");
        Flow.Publisher<String> publisher = ZeroPublisher.fromIterable(toSend);

        AtomicReference<Throwable> error = new AtomicReference<>();
        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);


        Flow.Publisher<String> processedPublisher =
                processor(createTubeConfig(), publisher, (errorConsumer, s) -> {
            latch.countDown();
            if (s.equals("C")) {
                throw new IllegalStateException();
            }
            return true;
        });

        processedPublisher.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                subscription.request(1);
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Thread.sleep(500);
        assertEquals(toSend.subList(0, 2), received);
        assertNotNull(error.get());
    }

    @Test
    public void testConvertingProcessor() throws Exception {
        List<Integer> toSend = List.of(1, 2, 3);
        Flow.Publisher<Integer> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        Flow.Publisher<String> convertingPublisher =
                convertingProcessor(createTubeConfig(), publisher, String::valueOf);

        convertingPublisher.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                subscription.request(1);
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(toSend.stream().map(String::valueOf).toList(), received);
    }

    @Test
    public void testErrorConvertingProcessor() throws Exception {
        List<Integer> toSend = List.of(1, 2, 3, 4);
        Flow.Publisher<Integer> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        Flow.Publisher<String> convertingPublisher =
                convertingProcessor(createTubeConfig(), publisher, (errorConsumer, i) -> {
                    try {
                        if (i == 3) {
                            throw new IllegalStateException();
                        }
                        return String.valueOf(i);
                    } catch (Throwable t) {
                        errorConsumer.accept(t);
                        return "";
                    }
                });

        convertingPublisher.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                subscription.request(1);
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(toSend.stream().map(String::valueOf).toList().subList(0, 2), received);
    }

    @Test
    public void testUncaughtErrorConvertingProcessor() throws Exception {
        List<Integer> toSend = List.of(1, 2, 3, 4);
        Flow.Publisher<Integer> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        Flow.Publisher<String> convertingPublisher =
                convertingProcessor(createTubeConfig(), publisher, i -> {
                    if (i == 3) {
                        throw new IllegalStateException();
                    }
                    return String.valueOf(i);
                });

        convertingPublisher.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                subscription.request(1);
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertEquals(toSend.stream().map(String::valueOf).toList().subList(0, 2), received);
    }

    @Test
    public void testConvertingAndProcessingProcessor() throws Exception {
        List<Integer> toSend = List.of(1, 2, 3);
        Flow.Publisher<Integer> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        List<Integer> processed = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        Flow.Publisher<Integer> processedPublisher =
                processor(createTubeConfig(), publisher, (errorConsumer, i) -> {
            processed.add(i);
            return true;
        });

        Flow.Publisher<String> convertingPublisher =
                convertingProcessor(createTubeConfig(), processedPublisher, String::valueOf);

                convertingPublisher.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                subscription.request(1);
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });
        latch.await(2, TimeUnit.SECONDS);
        assertEquals(toSend, processed);
        assertEquals(toSend.stream().map(String::valueOf).toList(), received);
    }

    @Test
    public void testCancelProcessor() throws Exception {
        List<String> toSend = List.of("A", "B", "C", "D");
        Flow.Publisher<String> publisher = ZeroPublisher.fromIterable(toSend);

        List<String> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);

        Flow.Publisher<String> processedPublisher =
                processor(createTubeConfig(), publisher, (errorConsumer, s) -> {
            latch.countDown();
            if (s.equals("C")) {
                return false;
            }
            return true;
        });

        processedPublisher.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                subscription.request(1);
                received.add(item);
                latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
            }

            @Override
            public void onComplete() {
                subscription.cancel();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Thread.sleep(500);
        assertEquals(toSend.subList(0, 2), received);
    }

    @Test
    public void testMutinyZeroErrorPropagationSanityTest() {
        Flow.Publisher<String> source = ZeroPublisher.fromItems("a", "b", "c");

        Flow.Publisher<String> processor = ZeroPublisher.create(createTubeConfig(), tube -> {
            source.subscribe(new Flow.Subscriber<String>() {
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(1);
                }

                @Override
                public void onNext(String item) {
                    if (item.equals("c")) {
                        onError(new IllegalStateException());
                    }
                    tube.send(item);
                    subscription.request(1);
                }

                @Override
                public void onError(Throwable throwable) {
                    tube.fail(throwable);
                    subscription.cancel();
                }

                @Override
                public void onComplete() {
                    tube.complete();
                }
            });
        });

      Flow.Publisher<String> processor2 = ZeroPublisher.create(createTubeConfig(), tube -> {
            processor.subscribe(new Flow.Subscriber<String>() {
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    this.subscription = subscription;
                    subscription.request(1);
                }

                @Override
                public void onNext(String item) {
                    if (item.equals("c")) {
                        onError(new IllegalStateException());
                    }
                    tube.send(item);
                    subscription.request(1);
                }

                @Override
                public void onError(Throwable throwable) {
                    tube.fail(throwable);
                    subscription.cancel();
                }

                @Override
                public void onComplete() {
                    tube.complete();
                }
            });
        });

        List<Object> results = new ArrayList<>();

        processor2.subscribe(new Flow.Subscriber<String>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                results.add(item);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                results.add(throwable);
                subscription.cancel();
            }

            @Override
            public void onComplete() {
            }
        });

        assertEquals(3, results.size());
        assertEquals("a", results.get(0));
        assertEquals("b", results.get(1));
        assertInstanceOf(IllegalStateException.class, results.get(2));

    }
}
