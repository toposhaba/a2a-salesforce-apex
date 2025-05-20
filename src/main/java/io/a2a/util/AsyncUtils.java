package io.a2a.util;

import java.util.concurrent.Flow;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import mutiny.zero.BackpressureStrategy;
import mutiny.zero.Tube;
import mutiny.zero.TubeConfiguration;

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

    public static class ConsumingSubscriber<T> implements Flow.Subscriber<T> {
        private Flow.Subscription subscription;
        private final BiFunction<ConsumingSubscriber<T>, T, Boolean> nextFunction;
        private final Consumer<T> publishNextConsumer;
        private final Consumer<Throwable> failureOrCompleteConsumer;

        public ConsumingSubscriber(BiFunction<ConsumingSubscriber<T>, T, Boolean> nextFunction) {
            this(nextFunction, null, null);
        }

        protected ConsumingSubscriber(
                BiFunction<ConsumingSubscriber<T>, T, Boolean> nextFunction,
                Consumer<T> publishNextConsumer,
                Consumer<Throwable> failureOrCompleteConsumer) {
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
            subscription.cancel();
            boolean continueProcessing = nextFunction.apply(this, item);
            if (publishNextConsumer != null) {
                publishNextConsumer.accept(item);
            }
            if (!continueProcessing) {
                subscription.cancel();
            } else {
                subscription.request(1);
            }
        }


        @Override
        public void onError(Throwable throwable) {
            failureOrCompleteConsumer.accept(throwable);
        }

        @Override
        public void onComplete() {
            subscription.cancel();
            failureOrCompleteConsumer.accept(null);
        }
    }

    public static class PublishingSubscriber<T> extends ConsumingSubscriber<T> {
        private Flow.Subscription subscription;
        private final Tube<T> tube;

        public PublishingSubscriber(Tube<T> tube, BiFunction<ConsumingSubscriber<T>, T, Boolean> nextFunction) {
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

            this.tube = tube;
        }

        @Override
        public void onError(Throwable throwable) {
            super.onError(throwable);
            tube.fail(throwable);
        }

        @Override
        public void onComplete() {
            tube.complete();
            super.onComplete();
        }
    }

}
