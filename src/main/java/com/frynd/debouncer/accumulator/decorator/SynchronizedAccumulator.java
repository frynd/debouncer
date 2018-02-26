package com.frynd.debouncer.accumulator.decorator;

import com.frynd.debouncer.accumulator.Accumulator;

import java.util.function.Consumer;

/**
 * Accumulator decorator that wraps calls to the decorated accumulator in
 * synchronization blocks.
 * <br/>
 * Usage: <pre>{@code
 *      Accumulator<String, List<String>> accumulator = new SynchronizedAccumulator(
 *          new ListAccumulator()
 *      );
 * }</pre>
 * <strong>Note:</strong> Access to the decorated accumulator outside this wrapper are not synchronized.
 *
 * @param <V> The accumulating value type of the decorated accumulator
 * @param <R> The result type of the decorated accumulator
 */
public class SynchronizedAccumulator<V, R> extends AccumulatorDecorator<V, R> {
    private final Object mutex = new Object();

    /**
     * Construct a synchronized accumulator.
     *
     * @param decorated the accumulator to be synchronized
     */
    public SynchronizedAccumulator(Accumulator<V, R> decorated) {
        super(decorated);
    }

    @Override
    public void accumulate(V item) {
        synchronized (mutex) {
            super.accumulate(item);
        }
    }

    @Override
    public void drain(Consumer<? super R> consumer) {
        synchronized (mutex) {
            super.drain(consumer);
        }
    }
}
