package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.accumulator.Accumulator;
import com.frynd.debouncer.drainer.Drainers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Accumulates values into sub-accumulators grouped by key.
 * Usage: <pre>{@code
 *      MapAccumulator<Record, String, Record> latestByUserId = new MapAccumulator<>(
 *        Record::getUserId,
 *        LatestValueAccumulator::new
 *      );
 *      //for Record(userId, value)
 *      latestByUserId.accumulate(new Record("waimarie", "10"));
 *      latestByUserId.accumulate(new Record("nikau", "57"));
 *      latestByUserId.accumulate(new Record("waimarie", "42"));
 *      //latestByUserId now has 2 ids, each associated with one record (through accumulation).
 *      //"waimarie" -> 42
 *      //"nikau" -> 57
 * }</pre>
 * <br/>
 * <strong>Note:</strong> The map accumulation mechanism is not synchronized.
 * <br/>
 * <strong>Note:</strong> Once drained the map does not clear. This allows the sub-accumulators to optimize
 * performance across map drains. E.g. A list accumulator may wish to maintain the space allocated. However,
 * after each drain, all sub-accumulators are drained.
 * <br/>
 *
 * @param <V> the value type to be accumulated
 * @param <K> the key type to group values by
 * @param <R> the result type of the sub-accumulators
 * @see Drainers#drainMap(BiConsumer)
 */
public class MapAccumulator<V, K, R> implements Accumulator<V, Map<K, Accumulator<V, R>>> {

    private Map<K, Accumulator<V, R>> map = new HashMap<>();

    private final Function<? super V, ? extends K> keyFunction;
    private final Supplier<Accumulator<V, R>> accumulatorSupplier;

    /**
     * Constructs a MapAccumulator that uses the {@code keyFunction} to determine the grouping key for each value, as
     * well as {@code accumulatorSupplier} for instantiating new accumulators when new grouping keys are encountered.
     *
     * @param keyFunction         the grouping key function
     * @param accumulatorSupplier the supplier of new accumulators. Should always return a new accumulator.
     * @throws NullPointerException if either {@code keyFunction} or {@code accumulatorSupplier} is null.
     */
    public MapAccumulator(Function<? super V, ? extends K> keyFunction,
                          Supplier<Accumulator<V, R>> accumulatorSupplier) {
        Objects.requireNonNull(keyFunction);
        Objects.requireNonNull(accumulatorSupplier);

        this.keyFunction = keyFunction;
        this.accumulatorSupplier = accumulatorSupplier;
    }

    /**
     * Accumulate the item, by applying the {@code keyFunction}, then passing it to the
     * associated accumulator.
     *
     * @param item the item to accumulate
     */
    @Override
    public void accumulate(V item) {
        K key = keyFunction.apply(item);
        Accumulator<V, R> accumulator = map.computeIfAbsent(key, k -> accumulatorSupplier.get());
        accumulator.accumulate(item);
    }

    /**
     * Drains the currently accumulated mapping of values into the {@code consumer}.
     * <br/>
     * <strong>Note:</strong> This does not clear out the key to sub-accumulator mapping
     * in order to allow sub-accumulators to not be recreated between drains.
     *
     * @param consumer the consumer to pass the current result value to
     * @throws NullPointerException if consumer is null.
     * @see Drainers#drainMap(BiConsumer)
     */
    @Override
    public void drain(Consumer<? super Map<K, Accumulator<V, R>>> consumer) {
        Objects.requireNonNull(consumer);
        consumer.accept(map);
        map.values().forEach(accumulator -> accumulator.drain(Drainers.noopDrainer()));
    }
}
