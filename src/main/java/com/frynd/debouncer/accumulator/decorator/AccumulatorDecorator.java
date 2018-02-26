package com.frynd.debouncer.accumulator.decorator;

import com.frynd.debouncer.accumulator.Accumulator;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implements the decorator pattern over an accumulator.
 *
 * @param <V> the value type of the decorated accumulator
 * @param <R> the result type of the decorated accumulator
 */
public class AccumulatorDecorator<V, R> implements Accumulator<V, R> {
    private final Accumulator<V, R> decorated;

    /**
     * Construct an empty decorator over another accumulator
     * @param decorated the decorated accumulator
     */
    protected AccumulatorDecorator(Accumulator<V, R> decorated) {
        this.decorated = Objects.requireNonNull(decorated);
    }

    @Override
    public void accumulate(V item) {
        getDecorated().accumulate(item);
    }

    @Override
    public void drain(Consumer<? super R> consumer) {
        getDecorated().drain(consumer);
    }


    /**
     * Get the decorated accumulator.
     * @return the decorated accumulator.
     */
    protected Accumulator<V, R> getDecorated() {
        return decorated;
    }
}
