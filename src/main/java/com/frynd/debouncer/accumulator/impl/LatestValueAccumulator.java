package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.accumulator.Accumulator;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Accumulator that accumulates only the latest item.
 * <br/>
 * Usage: <pre>{@code
 *     LatestValueAccumulator<String> accumulator = new LatestValueAccumulator<>();
 *     accumulator.accumulate("a");
 *     accumulator.accumulate("b");
 *     accumulator.accumulate("c");
 *     accumulator.drain(System.out::println); //prints "c";
 * }</pre>
 * <strong>Note:</strong> This implementation is not synchronized.
 *
 * @param <T> The type of item accumulated.
 */
public class LatestValueAccumulator<T> implements Accumulator<T, T> {
    private T item = null;

    /**
     * Accumulates the item as the result item.
     *
     * @param item the item to accumulate as new result value
     */
    @Override
    public void accumulate(T item) {
        this.item = item;
    }

    /**
     * Drains the latest value passed to {@link #accumulate(Object)} into {@code consumer},
     * then resets the current result value to null.
     *
     * @param consumer the consumer to pass the current result value to
     * @throws NullPointerException if {@code consumer} is null
     */
    @Override
    public void drain(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer);
        consumer.accept(item);
        item = null;
    }
}
