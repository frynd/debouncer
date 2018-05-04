package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.accumulator.Accumulator;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/**
 * Accumulator that stores accumulations in a BlockingQueue which drains to a
 * list which is passed to the consumer.
 * <br/>
 * Usage: <pre>{@code
 *     BlockingQueueAccumulator<String> accumulator = new BlockingQueueAccumulator<>();
 *     accumulator.accumulate("a");
 *     accumulator.accumulate("b");
 *     accumulator.accumulate("c");
 *     accumulator.drain(System.out::println); //prints "[a,b,c]";
 * }</pre>
 *
 * @param <T> The type of item accumulated.
 */
public class BlockingQueueAccumulator<T> implements Accumulator<T, List<T>> {
    private final BlockingQueue<T> queue;

    public BlockingQueueAccumulator(BlockingQueue<T> queue) {
        this.queue = queue;
    }

    /**
     * Accumulate the item into the current result.
     *
     * @param item the item to accumulate
     */
    @Override
    public void accumulate(T item) {
        queue.add(item);
    }

    /**
     * Drain all currently accumulated values into a list, which is then
     * passed to the {@code consumer}.
     *
     * @param consumer the consumer to pass the current result value to
     */
    @Override
    @SuppressWarnings("squid:S899")
    public void drain(Consumer<? super List<T>> consumer) {
        Objects.requireNonNull(consumer);
        List<T> list = new LinkedList<>();
        queue.drainTo(list);
        consumer.accept(list);
    }
}
