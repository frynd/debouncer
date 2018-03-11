package com.frynd.debouncer.accumulator.decorator;

import com.frynd.debouncer.accumulator.Accumulator;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Accumulator decorator for off-loading accumulation to a separate executor.
 * <br/>
 * Usage: <pre>{@code
 *      Accumulator<String, List<String>> accumulator = ...;
 *      Executor executor = ...; //e.g. Executors.newSingleThreadExecutor()
 *      accumulator = new AccumulatingOn(accumulator, executor);
 *
 * }</pre>
 * @param <V> The accumulating value type of the decorated accumulator
 * @param <R> The result type of the decorated accumulator
 * @see java.util.concurrent.Executors
 */
public class AccumulatingOn<V, R> extends AccumulatorDecorator<V, R> {
    private final Executor executor;

    /**
     * Create an accumulator that executes accumulation actions on the executor.
     * @param decorated the decorated accumulator
     * @param executor the executor to run accumulations on
     */
    public AccumulatingOn(Accumulator<V, R> decorated, Executor executor) {
        super(decorated);
        Objects.requireNonNull(executor);
        this.executor = executor;
    }

    @Override
    public void accumulate(V item) {
        executor.execute(() -> super.accumulate(item));
    }
}
