package com.frynd.debouncer.accumulator;

import com.frynd.debouncer.accumulator.impl.MapAccumulator;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Static utility methods for {@link Accumulator#drain(Consumer)}
 */
public class Drainers {
    private static final Consumer NO_OP_DRAINER = t -> {
        //no-op drainer
    };

    private Drainers() {
        //no-op static utils constructor
    }

    /**
     * Convenience no-op consumer for draining an accumulator
     *
     * @param <R> the result type of the accumulator
     * @return a no-op consumer for draining an accumulator
     * @see Accumulator#drain(Consumer)
     */
    @SuppressWarnings("unchecked")
    public static <R> Consumer<R> noopDrainer() {
        return NO_OP_DRAINER;
    }

    /**
     * Convenience method for creating a consumer that iterates over an iterable and applies
     * the {@code action} to each item.
     * Exceptions thrown by {@code action} are relayed to the caller of the consumer.
     *
     * @param action the action to be applied to each item.
     * @param <R>    the type contained in the iterable
     * @param <I>    the type of the iterable
     * @return a consumer that iterates over an iterable and applies {@code action} to each item
     * @throws NullPointerException if {@code action} is null
     */
    public static <R, I extends Iterable<R>> Consumer<I> drainForEach(Consumer<? super R> action) {
        Objects.requireNonNull(action);
        return col -> col.forEach(action);
    }

    /**
     * Convenience method for creating a consumer that iterates over a map of accumulators, and passes the key and
     * accumulated values to the {@code action}.
     * Primarily designed for use with {@link MapAccumulator}
     * <p>
     * Usage: <pre>{@code
     *      Map<String, Accumulator<Record, List<Record>>> mapping = ...;
     *      BiConsumer<String, List<Record>> consumer = ...;
     *      drainMap(consumer).accept(mapping);
     * }</pre>
     * Exceptions thrown by {@code action} are relayed to the caller of the consumer.
     * <strong>Note:</strong> All accumulators in the map will be drained by this consumer.
     *
     * @param action the action to be applied to each key/accumulated value pair
     * @param <K>    the key type of the map to be consumed
     * @param <V>    the value type of the map to be consumed
     * @param <R>    the return type of the accumulators in the map to be consumed
     * @return a consumer that applies the {@code action} to each key/accumulated value pair in a passed map
     * @throws NullPointerException if {@code action} is null.
     * @see MapAccumulator
     */
    public static <K, V, R> Consumer<Map<K, Accumulator<V, R>>> drainMap(BiConsumer<K, R> action) {
        Objects.requireNonNull(action);
        return map -> map.forEach(
                (key, accumulator) -> accumulator.drain(
                        result -> action.accept(key, result)
                )
        );
    }
}
