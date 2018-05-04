package com.frynd.debouncer.accumulator.impl;

import java.util.List;
import java.util.function.Consumer;

/**
 * Accumulator that accumulates all items into a list until drained.
 * <br/>
 * Usage: <pre>{@code
 *     ListAccumulator<String> accumulator = new ListAccumulator<>(new ArrayList<>());
 *     accumulator.accumulate("a");
 *     accumulator.accumulate("b");
 *     accumulator.accumulate("c");
 *     accumulator.drain(System.out::println); //prints "[a,b,c]";
 * }</pre>
 * <p>
 * For most usages, {@link java.util.ArrayList} is a good backing list, as it is
 * cleared between drains, so maintains its allocated size.
 * </p>
 * <p>
 * <strong>Note:</strong> This implementation is only synchronized in as much as
 * the backing list is synchronized.
 * </p><p>
 * <strong>Note:</strong> The backing list should not be modified outside of this class. Behavior
 * for such modifications are not guaranteed.<br/>
 * </p>
 *
 * @param <T> The type of item accumulated.
 * @see CollectionAccumulator
 * @see com.frynd.debouncer.drainer.Drainers#drainIterable(Consumer)
 */
public class ListAccumulator<T> extends CollectionAccumulator<T, List<T>> {

    /**
     * Create a ListAccumulator that uses {@code backingList}
     * as the accumulation store.<br/>
     * <strong>Note:</strong> The backing list should not be modified outside of this class. Behavior
     * for such modifications is not guaranteed.
     *
     * @param backingList the accumulation store that will be used to store the events.
     */
    public ListAccumulator(List<T> backingList) {
        super(backingList);
    }
}
