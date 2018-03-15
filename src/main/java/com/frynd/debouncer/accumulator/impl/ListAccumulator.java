package com.frynd.debouncer.accumulator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Accumulator that accumulates all items into a list until drained.
 * Usage: <pre>{@code
 *     ListAccumulator<String> accumulator = new ListAccumulator<>();
 *     accumulator.accumulate("a");
 *     accumulator.accumulate("b");
 *     accumulator.accumulate("c");
 *     accumulator.drain(System.out::println); //prints "[a,b,c]";
 * }</pre>
 * <strong>Note:</strong> This implementation is not synchronized.
 *
 * @param <T> The type of item accumulated.
 * @see com.frynd.debouncer.drainer.Drainers#drainIterable(Consumer)
 */
public class ListAccumulator<T> extends CollectionAccumulator<T, List<T>> {
    public ListAccumulator() {
        super(new ArrayList<>());
    }
}
