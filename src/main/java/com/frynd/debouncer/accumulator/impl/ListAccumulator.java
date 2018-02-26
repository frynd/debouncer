package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.accumulator.Accumulator;
import com.frynd.debouncer.accumulator.Drainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Accumulator that accumulates only the latest item.
 * Usage: <pre>{@code
 *     Accumulator<String> accumulator = new ListAccumulator<>();
 *     accumulator.accumulate("a");
 *     accumulator.accumulate("b");
 *     accumulator.accumulate("c");
 *     accumulator.drain(System.out::println); //prints "[a,b,c]";
 * }</pre>
 * <strong>Note:</strong> This implementation is not synchronized.
 *
 * @param <T> The type of item accumulated.
 * @see Drainers#drainForEach(Consumer)
 */
public class ListAccumulator<T> implements Accumulator<T, List<T>> {
    private final ArrayList<T> list = new ArrayList<>();

    /**
     * Append item to the current result value
     *
     * @param item the item to append
     */
    @Override
    public void accumulate(T item) {
        list.add(item);
    }

    /**
     * Drains all the currently accumulated values into the {@code consumer},
     * then clears the current value.
     * <strong>Note:</strong> This means the list passed into the {@code consumer}
     * should not be used outside of the consumer.
     *
     * @param consumer the consumer to pass the currently accumulated values to
     * @throws NullPointerException if {@code consumer} is null
     * @see Drainers#drainForEach(Consumer)
     */
    @Override
    public void drain(Consumer<? super List<T>> consumer) {
        Objects.requireNonNull(consumer);
        consumer.accept(list);
        list.clear();
    }
}
