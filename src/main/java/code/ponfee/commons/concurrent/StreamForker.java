package code.ponfee.commons.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The class use in fork {@link Stream},
 * from book "Java 8 In Action"<p>
 * 
 * Usage:
 * <pre> {@code
 *   Stream<Integer> stream = Stream.of(1, 2, 3, 4, 4, 5, 5);
 *   StreamForker.Results results = new StreamForker<>(stream)
 *     .fork(1, s -> s.max(Integer::compareTo)) // 直接聚合
 *     .fork(2, s -> s.distinct().reduce(0, Integer::sum))
 *     .getResults();
 * } </pre>
 * 
 * @param <T>
 * @author Java 8 In Action
 */
public class StreamForker<T> {

    private final Stream<T> stream;
    private final Map<Object, Function<Stream<T>, ?>> forks = new HashMap<>();

    public StreamForker(Stream<T> stream) {
        this.stream = stream;
    }

    public StreamForker<T> fork(Object key, Function<Stream<T>, ?> f) {
        forks.put(key, f);
        return this;
    }

    public Results getResults() {
        ForkingStreamConsumer<T> consumer = build();
        try {
            stream.sequential().forEach(consumer);
        } finally {
            consumer.finish();
        }
        return consumer;
    }

    private ForkingStreamConsumer<T> build() {
        List<BlockingQueue<T>> queues = new ArrayList<>();

        Map<Object, Future<?>> actions = forks.entrySet().stream().reduce(
            new HashMap<>(),
            (map, e) -> {
                map.put(e.getKey(), getOperationResult(queues, e.getValue()));
                return map;
            },
            (m1, m2) -> {
                m1.putAll(m2);
                return m1;
            }
        );

        return new ForkingStreamConsumer<>(queues, actions);
    }

    private Future<?> getOperationResult(List<BlockingQueue<T>> queues, Function<Stream<T>, ?> f) {
        BlockingQueue<T> queue = new LinkedBlockingQueue<>();
        queues.add(queue);
        Stream<T> source = StreamSupport.stream(new BlockingQueueSpliterator<>(queue), false);
        return CompletableFuture.supplyAsync(() -> f.apply(source));
    }

    public interface Results {
        <R> R get(Object key);
    }

    @SuppressWarnings("unchecked")
    private static class ForkingStreamConsumer<T> implements Consumer<T>, Results {
        private static final Object END_OF_STREAM = new Object();

        private final List<BlockingQueue<T>> queues;
        private final Map<Object, Future<?>> actions;

        ForkingStreamConsumer(List<BlockingQueue<T>> queues, Map<Object, Future<?>> actions) {
            this.queues = queues;
            this.actions = actions;
        }

        @Override
        public void accept(T t) {
            queues.forEach(q -> q.add(t));
        }

        @Override
        public <R> R get(Object key) {
            try {
                return ((Future<R>) actions.get(key)).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void finish() {
            accept((T) END_OF_STREAM);
        }
    }

    private static class BlockingQueueSpliterator<T> implements Spliterator<T> {
        private final BlockingQueue<T> q;

        BlockingQueueSpliterator(BlockingQueue<T> q) {
            this.q = q;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            T t;
            while (true) { // if occur exception, then keep take
                try {
                    t = q.take();
                    break;
                } catch (InterruptedException ignored) {
                }
            }

            if (t != ForkingStreamConsumer.END_OF_STREAM) {
                action.accept(t);
                return true;
            }

            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    }

}
