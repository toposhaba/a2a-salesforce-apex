package io.a2a.util;

import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import mutiny.zero.BackpressureStrategy;
import mutiny.zero.Tube;
import mutiny.zero.TubeConfiguration;
import mutiny.zero.ZeroPublisher;
import mutiny.zero.operators.Transform;

public class AsyncUtils {

    private static final int DEFAULT_TUBE_BUFFER_SIZE = 256;

    public static TubeConfiguration createTubeConfig() {
        return createTubeConfig(DEFAULT_TUBE_BUFFER_SIZE);
    }

    public static TubeConfiguration createTubeConfig(int bufferSize) {
        return new TubeConfiguration()
                .withBackpressureStrategy(BackpressureStrategy.BUFFER)
                .withBufferSize(256);
    }

    public static <T> void consumer(
            TubeConfiguration config,
            Flow.Publisher<T> source,
            BiFunction<Consumer<Throwable>, T, Boolean> nextFunction) {

        AtomicReference<Throwable> setError = new AtomicReference<>();

        BiFunction<Consumer<Throwable>, T, Boolean> wrappedNextFunction = new BiFunction<Consumer<Throwable>, T, Boolean>() {
            @Override
            public Boolean apply(Consumer<Throwable> throwableConsumer, T t) {
                return nextFunction.apply(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (throwable != null) {
                            setError.set(throwable);
                        }
                    }
                }, t);
            }
        };

        ZeroPublisher.create(config, tube -> {
            source.subscribe(new ConsumingSubscriber<>(nextFunction));
        })
                .subscribe(new Flow.Subscriber<Object>() {
                    private Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(Object item) {
                        subscription.request(1);
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
    }

    public static <T> Flow.Publisher<T> processor(
            TubeConfiguration config,
            Flow.Publisher<T> source,
            BiFunction<Consumer<Throwable>, T, Boolean> nextFunction) {

        return ZeroPublisher.create(config, tube -> {
            source.subscribe(new ProcessingSubscriber<>(tube, nextFunction));
        });
    }

    public static <T, N> Flow.Publisher<N> convertingProcessor(Flow.Publisher<T> source, Function<T, N> converterFunction) {
        return new Transform<>(source, converterFunction);
    }


    private static class ConsumingSubscriber<T> implements Flow.Subscriber<T> {
        private Flow.Subscription subscription;
        private final BiFunction<Consumer<Throwable>, T, Boolean> nextFunction;
        private final Consumer<T> publishNextConsumer;
        private final Consumer<Throwable> failureOrCompleteConsumer;

        public ConsumingSubscriber(BiFunction<Consumer<Throwable>, T, Boolean> nextFunction) {
            this(nextFunction, null, null);
        }

        protected ConsumingSubscriber(
                BiFunction<Consumer<Throwable>, T, Boolean> nextFunction,
                Consumer<T> publishNextConsumer,
                Consumer<Throwable> failureOrCompleteConsumer) {
            Assert.checkNotNullParam("nextFunction", nextFunction);
            this.nextFunction = nextFunction;
            this.publishNextConsumer = publishNextConsumer != null ? publishNextConsumer : t -> {};
            this.failureOrCompleteConsumer = failureOrCompleteConsumer != null ? failureOrCompleteConsumer : t -> {};
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(T item) {
            AtomicBoolean errorRaised = new AtomicBoolean(false);

            Consumer<Throwable> errorConsumer = t -> {
                        errorRaised.set(true);
                        onError(t);
            };
            boolean continueProcessing = false;
            try {
                continueProcessing = nextFunction.apply(errorConsumer, item);
            } catch (Throwable t) {
                errorConsumer.accept(t);
            }
            if (!continueProcessing || errorRaised.get()) {
                subscription.cancel();
            } else {
                if (publishNextConsumer != null) {
                    publishNextConsumer.accept(item);
                }
                subscription.request(1);
            }
        }


        @Override
        public void onError(Throwable throwable) {
            subscription.cancel();
            failureOrCompleteConsumer.accept(throwable);
        }

        @Override
        public void onComplete() {
            subscription.cancel();
            failureOrCompleteConsumer.accept(null);
        }
    }

    private static class ProcessingSubscriber<T> extends ConsumingSubscriber<T> {
        private Flow.Subscription subscription;
        private final Tube<T> tube;

        public ProcessingSubscriber(Tube<T> tube, BiFunction<Consumer<Throwable>, T, Boolean> nextFunction) {
            super(
                    nextFunction,
                    tube::send,
                    t -> {
                        if (t == null) {
                            tube.complete();
                        } else {
                            tube.fail(t);
                        }
                    }
            );
            Assert.checkNotNullParam("tube", tube);
            this.tube = tube;
        }
    }

    private static class ConvertingProcessingSubscriber<T, N> implements Flow.Subscriber<T> {
        private Flow.Subscription subscription;
        private Tube<N> tube;
        private final BiFunction<Consumer<Throwable>, T, N> converterBiFunction;

        public ConvertingProcessingSubscriber(Tube<N> tube, Function<T, N> converterFunction) {
            Assert.checkNotNullParam("tube", tube);
            Assert.checkNotNullParam("converterFunction", converterFunction);
            this.tube = tube;
            this.converterBiFunction = (throwableConsumer, t) -> converterFunction.apply(t);
        }

        public ConvertingProcessingSubscriber(Tube<N> tube, BiFunction<Consumer<Throwable>, T, N> converterBiFunction) {
            Assert.checkNotNullParam("tube", tube);
            Assert.checkNotNullParam("converterBiFunction", converterBiFunction);
            this.tube = tube;
            this.converterBiFunction = converterBiFunction;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(T item) {
            AtomicBoolean errorRaised = new AtomicBoolean(false);
            Consumer<Throwable> errorConsumer = t -> {
                        errorRaised.set(true);
                        onError(t);
            };

            N converted = null;
            try {
                converted = converterBiFunction.apply(errorConsumer, item);
            } catch (Throwable t) {
                errorConsumer.accept(t);
                return;
            }
            if (!errorRaised.get()) {
                tube.send(converted);
                subscription.request(1);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            subscription.cancel();
            tube.fail(throwable);
        }

        @Override
        public void onComplete() {
            subscription.cancel();
            tube.complete();
        }
    }
}
