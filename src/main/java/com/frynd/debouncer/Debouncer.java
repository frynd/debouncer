package com.frynd.debouncer;

import com.frynd.debouncer.accumulator.Accumulator;
import com.frynd.debouncer.accumulator.decorator.AccumulatingOn;
import com.frynd.debouncer.accumulator.decorator.DrainingOn;
import com.frynd.debouncer.drainer.Drainers;
import com.frynd.debouncer.regulator.Regulator;
import com.frynd.debouncer.regulator.impl.ImmediateRegulator;

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
 *                                .draining(drainer)
 *                                .build();
 * debouncer.accumulate(item);
 * }</pre>
 *
 * @param <V> the value types to be accumulated.
 * @see #accumulating(Accumulator)
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
     * If no further configuration is provided, will use an {@link ImmediateRegulator} with a
     * no-op drainer. It is recommended to replace these.
     *
     * @param accumulator the accumulator the debouncer will use to accumulate events
     * @param <V>         the value type to be accumulated
     * @param <R>         the result type to be consumed later
     * @return A builder for a debouncer that will use {@code accumulator} as the base accumulator.
     * @see Accumulator
     * @see DebouncerBuilder#regulating(Function)
     * @see DebouncerBuilder#draining(Consumer)
     */
    public static <V, R> DebouncerBuilder<V, R> accumulating(Accumulator<V, R> accumulator) {
        Objects.requireNonNull(accumulator);
        return new DebouncerBuilder<>(accumulator, ImmediateRegulator::new, Drainers.noopDrainer());

    }

    /**
     * Builder for Debouncer.
     *
     * @param <V> the value type to be accumulated
     * @param <R> the result type to be consumed later
     */
    public static class DebouncerBuilder<V, R> {
        private final Accumulator<V, R> accumulator;
        private final Function<Runnable, Regulator> regulatorFactory;
        private final Consumer<? super R> drainer;

        private DebouncerBuilder(Accumulator<V, R> accumulator,
                                 Function<Runnable, Regulator> regulatorFactory,
                                 Consumer<? super R> drainer) {
            this.accumulator = accumulator;
            this.regulatorFactory = regulatorFactory;
            this.drainer = drainer;
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
            return new DebouncerBuilder<>(new AccumulatingOn<>(accumulator, executor), regulatorFactory, drainer);
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
            return new DebouncerBuilder<>(new DrainingOn<>(accumulator, executor), regulatorFactory, drainer);
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
         * @return A new builder that will invoke the regulator factory to create the regulator
         * @see Regulator
         */
        public DebouncerBuilder<V, R> regulating(Function<Runnable, Regulator> regulatorFactory) {
            Objects.requireNonNull(regulatorFactory);
            return new DebouncerBuilder<>(accumulator, regulatorFactory, drainer);
        }

        /**
         * Set the drainer action for the debouncer.
         * This is the recipient of the state accumulated by the {@code accumulator} when the {@code regulator}
         * determines that it is time.
         *
         * @param drainer the recipient of the accumulated state
         * @return A new builder that will invoke the {@code drainer} during debounce.
         * @see com.frynd.debouncer.drainer.Drainers
         */
        public DebouncerBuilder<V, R> draining(Consumer<? super R> drainer) {
            Objects.requireNonNull(drainer);
            return new DebouncerBuilder<>(accumulator, regulatorFactory, drainer);
        }

        /**
         * Build a Debouncer based on this builder.
         * Will set the runnable on the regulator.
         *
         * @return the Debouncer that has been configured by this builder.
         */
        public Debouncer<V> build() {
            //accumulator, drainer, and regulator have all been null checked.
            Regulator regulator = regulatorFactory.apply(() -> accumulator.drain(drainer));
            Objects.requireNonNull(regulator, "Result of regulator factory was null.");

            return new Debouncer<>(accumulator, regulator);
        }
    }

}
