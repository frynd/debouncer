package com.frynd.debouncer.accumulator.decorator;

import com.frynd.debouncer.accumulator.Accumulator;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Accumulator decorator for off-loading draining to a separate executor.
 * This can be useful, if, for example, the consumers update the UI and need
 * to be on the UI thread.
 * <br/>
 * Usage: <pre>{@code
 *      Accumulator<String, List<String>> accumulator = ...;
 *      Executor executor = ...; //e.g. javafx.application.Platform::runLater
 *      accumulator = new DrainingOn(accumulator, executor);
 * }</pre>
 * @param <V> The accumulating value type of the decorated accumulator
 * @param <R> The result type of the decorated accumulator
 * @see java.util.concurrent.Executors
 * @see javafx.application.Platform#runLater(Runnable)
 */
public class DrainingOn<V, R> extends AccumulatorDecorator<V, R> {
    private final Executor executor;

    /**
     * Create an accumulator that executes draining actions on the executor.
     * @param decorated the decorated accumulator
     * @param executor the executor to run drains on
     */
    public DrainingOn(Accumulator<V, R> decorated, Executor executor) {
        super(decorated);
        Objects.requireNonNull(executor);
        this.executor = executor;
    }

    @Override
    public void drain(Consumer<? super R> consumer) {
        executor.execute(() -> super.drain(consumer));
    }
}
