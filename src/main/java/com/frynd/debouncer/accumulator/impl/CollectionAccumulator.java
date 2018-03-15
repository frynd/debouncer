package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.accumulator.Accumulator;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Accumulator that accumulates all items into a provided collection.
 * <br/>
 * Usage: <pre>{@code
 *     CollectionAccumulator<String, Set<String>> accumulator = new CollectionAccumulator<>(new LinkedHashSet<>());
 *     accumulator.accumulate("a");
 *     accumulator.accumulate("a");
 *     accumulator.accumulate("b");
 *     accumulator.accumulate("a");
 *     accumulator.accumulate("c");
 *     accumulator.accumulate("a");
 *     accumulator.drain(System.out::println); //prints "[a,b,c]";
 * }</pre>
 * <strong>Note:</strong> This implementation is only synchronized in as much as
 * the backing collection is synchronized.
 *
 * @param <V> the value type to be accumulated
 * @param <C> the collection type to be accumulated into
 * @see com.frynd.debouncer.drainer.Drainers#drainIterable(Consumer)
 */
public class CollectionAccumulator<V, C extends Collection<V>> implements Accumulator<V, C> {
    private final C collection;

    /**
     * Create an accumulator which stores all items in the provided backing
     * {@code collection}.
     * The backing collection is the same collection provided to the consumer
     * when {@code drain} is invoked.
     *
     * @param collection the backing collection to store items into
     */
    public CollectionAccumulator(C collection) {
        Objects.requireNonNull(collection);
        this.collection = collection;
    }

    /**
     * Add the item to the current result collection
     *
     * @param item the item to accumulate
     */
    @Override
    public void accumulate(V item) {
        collection.add(item);
    }

    /**
     * Drains all the currently accumulated values into the {@code consumer},
     * then clears the current value.
     * <strong>Note:</strong> This means the collection passed into the {@code consumer}
     * should not be used outside of the consumer.
     *
     * @param consumer the consumer to pass the currently accumulated values to
     * @throws NullPointerException if {@code consumer} is null
     * @see com.frynd.debouncer.drainer.Drainers#drainIterable(Consumer)
     */
    @Override
    public void drain(Consumer<? super C> consumer) {
        Objects.requireNonNull(consumer);
        consumer.accept(collection);
        collection.clear();
    }
}
