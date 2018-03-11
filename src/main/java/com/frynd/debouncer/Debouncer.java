package com.frynd.debouncer;

import com.frynd.debouncer.accumulator.Accumulator;
import com.frynd.debouncer.accumulator.decorator.AccumulatingOn;
import com.frynd.debouncer.accumulator.decorator.DrainingOn;
import com.frynd.debouncer.regulator.Regulator;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Debouncing class.
 * Accumulates events into an {@code Accumulator}, then drains to a {@code Consumer} with the flow between
 * the two regulated by a {@code Regulator}.
 * Usage:<pre>{@code
 * Debouncer debouncer = Debouncer.accumulating(accumulator)
 *                                .regulating(regulator)
 *                                .draining(drainer);
 * debouncer.accumulate(item);
 * }</pre>
 *
 * @param <V> the value types to be accumulated.
 * @see Accumulator
 * @see Regulator
 * @see Consumer
 * @see com.frynd.debouncer.drainer.Drainers
 */
public class Debouncer<V> {
    private final Accumulator<V, ?> accumulator;
    private final Regulator regulator;

    private <R> Debouncer(Accumulator<V, R> accumulator, Regulator regulator) {
        this.accumulator = accumulator;
        this.regulator = regulator;
    }

    /**
     * Accumulates another event into the debouncer, and requests action from the regulator.
     *
     * @param value value to be accumulated
     */
    public void accumulate(V value) {
        accumulator.accumulate(value);
        regulator.requestAction();
    }

    /**
     * Start debouncer builder with an accumulator.
     *
     * @param accumulator the accumulator the debouncer will use to accumulate events
     * @param <V>         the value type to be accumulated
     * @param <R>         the result type to be consumed later
     * @return The first phase of the builder, which allows configuration of the accumulator and setting the
     * regulator factory
     * @see Accumulator
     * @see DebouncerBuilder#regulating(Function)
     */
    public static <V, R> DebouncerBuilder<V, R> accumulating(Accumulator<V, R> accumulator) {
        Objects.requireNonNull(accumulator);
        return new DebouncerBuilder<>(accumulator);

    }

    /**
     * Phased builder for Debouncer.
     * Allows:
     * - accumulatingOn - decorate current accumulator with executor based accumulation
     * - drainingOn - decorate current accumulator with executor based draining
     * - regulating - set regulator and move to next phase.
     *
     * @param <V> the value type to be accumulated
     * @param <R> the result type to be consumed later
     */
    public static class DebouncerBuilder<V, R> {
        private final Accumulator<V, R> accumulator;

        private DebouncerBuilder(Accumulator<V, R> accumulator) {
            this.accumulator = accumulator;
        }

        /**
         * Returns a debouncer builder whose result debouncer will accumulate values on
         * a separate {@code executor}.
         * Useful if the accumulation calculation is involved, and slowing down callers
         * off accumulate is undesirable.
         *
         * @param executor the executor to run accumulation on
         * @return a new builder that uses executor based accumulation
         * @see AccumulatingOn
         * @see java.util.concurrent.Executors
         */
        public DebouncerBuilder<V, R> accumulatingOn(Executor executor) {
            Objects.requireNonNull(executor);
            return new DebouncerBuilder<>(new AccumulatingOn<>(accumulator, executor));
        }

        /**
         * Returns a debouncer builder whose result debouncer will drain values on
         * a separate {@code executor}.
         * Useful if the draining method should be on a specified thread, e.g. a
         * UI thread.
         *
         * @param executor the executor to run drains on
         * @return a new builder that uses executor based draining
         * @see DrainingOn
         * @see java.util.concurrent.Executors
         * @see javafx.application.Platform#runLater(Runnable)
         */
        public DebouncerBuilder<V, R> drainingOn(Executor executor) {
            Objects.requireNonNull(executor);
            return new DebouncerBuilder<>(new DrainingOn<>(accumulator, executor));
        }

        /**
         * Set the regulator factory.
         * <p>
         * e.g. <pre>{@code
         *  runnable -> DelayedRegulator(scheduler, delayMillis, runnable);
         * }</pre>
         * <p>
         * Since many regulators need a final {@code Runnable}, a function that that takes a runnable
         * and returns a regulator is required.
         * The factory will only be invoked once.
         *
         * @param regulatorFactory factory method that accepts a runnable and returns a regulator
         * @return the next phase of the builder which allows setting the drainer
         * @see Regulator
         * @see DebouncerBuilderRegulation#draining(Consumer)
         */
        public DebouncerBuilderRegulation<V, R> regulating(Function<Runnable, Regulator> regulatorFactory) {
            Objects.requireNonNull(regulatorFactory);
            return new DebouncerBuilderRegulation<>(this, regulatorFactory);
        }
    }

    /**
     * Final phase of debounce builder.
     * Allows setting the draining function.
     *
     * @param <V> the value type to be accumulated
     * @param <R> the result type to be consumed later
     */
    public static class DebouncerBuilderRegulation<V, R> {
        private final DebouncerBuilder<V, R> accumulation;
        private final Function<Runnable, Regulator> regulatorFactory;

        private DebouncerBuilderRegulation(DebouncerBuilder<V, R> accumulation,
                                           Function<Runnable, Regulator> regulatorFactory) {
            this.accumulation = accumulation;
            this.regulatorFactory = regulatorFactory;
        }

        /**
         * Set the drainer for the debouncer.
         * The drainer will be invoked with the current result value from the accumulator.
         * <br/>
         * <strong>Note:</strong> the accumulator may choose to reuse the value passed to consumer,
         * so care must be taken when deciding what values should be used outside of the consumer.
         *
         * @param drainer the drainer to be invoked when the regulator runs
         * @return the debouncer that has been built so far
         * @see Accumulator#drain(Consumer)
         * @see com.frynd.debouncer.drainer.Drainers
         */
        public Debouncer<V> draining(Consumer<? super R> drainer) {
            Objects.requireNonNull(drainer);
            Accumulator<V, R> accumulator = accumulation.accumulator;
            Regulator regulator = regulatorFactory.apply(() -> accumulator.drain(drainer));
            return new Debouncer<>(accumulator, regulator);
        }
    }

}
