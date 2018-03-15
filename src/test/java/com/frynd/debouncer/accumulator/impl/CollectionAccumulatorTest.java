package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.drainer.Drainers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

class CollectionAccumulatorTest {

    private CollectionAccumulator<String, Set<String>> fixture;

    @BeforeEach
    void setUp()
    {
        fixture = new CollectionAccumulator<>(new LinkedHashSet<>());
    }

    @Test
    @DisplayName("New accumulator should have empty result.")
    void testInitialization() {
        fixture.drain(col -> Assertions.assertTrue(col.isEmpty(), "Newly created should be empty."));
    }

    @Test
    @DisplayName("Collection accumulator should accumulate each value.")
    void accumulate() {
        fixture.accumulate("umbrella");
        fixture.drain(col -> Assertions.assertIterableEquals(Collections.singletonList("umbrella"), col,
                "Accumulating a single value should result in a single value."));

        fixture.accumulate("inconclusive");
        fixture.accumulate("inconclusive");
        fixture.accumulate("questionable");
        fixture.accumulate("inconclusive");
        fixture.accumulate("inconclusive");
        fixture.accumulate("inconclusive");
        fixture.accumulate("questionable");
        fixture.accumulate("questionable");
        fixture.drain(col -> Assertions.assertIterableEquals(Arrays.asList("inconclusive", "questionable"), col,
                "Accumulating multiple values should result in the values as accumulated by the backing set."));
    }

    @Test
    @DisplayName("Collection accumulator should reset to empty collection.")
    void drain() {
        fixture.accumulate("tenuous");
        fixture.drain(Drainers.noopDrainer());
        fixture.drain(col -> Assertions.assertTrue(col.isEmpty(), "Freshly drained accumulator should be empty."));
    }

    @Test
    @DisplayName("Cannot accumulate to a null consumer.")
    void drainNull() {
        fixture.accumulate("nifty");
        Assertions.assertThrows(NullPointerException.class, () -> fixture.drain(null), "Cannot drain to null.");
    }

    @Test
    @DisplayName("Test compatibility with Drainers.drainIterable.")
    void drainIterable() {
        List<String> strings = Arrays.asList("abounding", "refuse", "plough");
        List<String> stringsCopy = new ArrayList<>(strings);

        strings.forEach(fixture::accumulate);
        fixture.drain(Drainers.drainIterable(stringsCopy::remove));
        Assertions.assertEquals(Collections.emptyList(), stringsCopy, "Each item should have been removed from stringsCopy");
    }
}