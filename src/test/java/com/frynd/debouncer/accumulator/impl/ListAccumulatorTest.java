package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.accumulator.Drainers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ListAccumulatorTest {

    private ListAccumulator<String> fixture;

    @BeforeEach
    void setUp()
    {
        fixture = new ListAccumulator<>();
    }

    @Test
    @DisplayName("New accumulator should have empty result.")
    void testInitialization() {
        fixture.drain(list -> Assertions.assertTrue(list.isEmpty(), "Newly created should be empty."));
    }

    @Test
    @DisplayName("List accumulator should accumulate each value in order.")
    void accumulate() {
        fixture.accumulate("umbrella");
        fixture.drain(list -> Assertions.assertIterableEquals(Collections.singletonList("umbrella"), list,
                "Accumulating a single value should result in a single value."));

        fixture.accumulate("inconclusive");
        fixture.accumulate("questionable");
        fixture.drain(list -> Assertions.assertEquals(Arrays.asList("inconclusive", "questionable"), list,
                "Accumulating multiple values should result in the last provided value."));
    }

    @Test
    @DisplayName("List accumulator should reset to empty list.")
    void drain() {
        fixture.accumulate("tenuous");
        fixture.drain(Drainers.noopDrainer());
        fixture.drain(list -> Assertions.assertTrue(list.isEmpty(), "Freshly drained accumulator should be empty."));
    }

    @Test
    @DisplayName("Cannot accumulate to a null consumer.")
    void drainNull() {
        fixture.accumulate("nifty");
        Assertions.assertThrows(NullPointerException.class, () -> fixture.drain(null), "Cannot drain to null.");
    }

    @Test
    @DisplayName("Test compatibility with Drainers.drainForEach.")
    void drainForEach() {
        List<String> strings = Arrays.asList("abounding", "refuse", "plough");
        List<String> stringsCopy = new ArrayList<>(strings);

        strings.forEach(fixture::accumulate);
        fixture.drain(Drainers.drainForEach(stringsCopy::remove));
        Assertions.assertEquals(Collections.emptyList(), stringsCopy, "Each item should have been removed from stringsCopy");
    }
}