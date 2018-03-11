package com.frynd.debouncer.accumulator;

import com.frynd.debouncer.drainer.Drainers;

import java.util.function.Consumer;

/**
 * Accumulator interface.
 * Accumulates values that may later be consumed.
 *
 * @param <V> value types to be accumulated
 * @param <R> result type to be consumed
 */
public interface Accumulator<V, R> {

    /**
     * Accumulate item into the accumulator.
     * The implementation of this is accumulator specific.
     * This may e.g. set the current result value of this accumulator to the value
     * or it may append it to the current value.
     *
     * @param item the item to accumulate
     */
    void accumulate(V item);

    /**
     * Drain the value of the accumulator.
     * The accumulator is reset after this.
     * The implementation is allowed to retain and reuse the value passed to the consumer.
     * E.g. an accumulator that appends accumulated values to a list may retain the list,
     * and clear it after the consumer is finished.
     * Therefore, it is not recommended to attempt to directly extract the value.
     *
     * @param consumer the consumer to pass the current result value to
     * @see Drainers
     */
    void drain(Consumer<? super R> consumer);
}
