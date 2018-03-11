package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.drainer.Drainers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LatestValueAccumulatorTest {

    private LatestValueAccumulator<String> fixture;

    @BeforeEach
    void setUp() {
        fixture = new LatestValueAccumulator<>();
    }

    @Test
    @DisplayName("New accumulator should have null result.")
    void testInitialization() {
        fixture.drain(str -> Assertions.assertNull(str, "Newly created should be null."));
    }

    @Test
    @DisplayName("Latest value accumulator should accumulate the latest value.")
    void accumulate() {
        fixture.accumulate("umbrella");
        fixture.drain(str -> Assertions.assertEquals("umbrella", str, "Accumulating a single value should result in the same value."));

        fixture.accumulate("inconclusive");
        fixture.accumulate("questionable");
        fixture.drain(str -> Assertions.assertEquals("questionable", str, "Accumulating multiple values should result in the last provided value."));
    }

    @Test
    @DisplayName("Latest value accumulator should reset to null.")
    void drain() {
        fixture.accumulate("tenuous");
        fixture.drain(Drainers.noopDrainer());
        fixture.drain(str -> Assertions.assertNull(str, "Freshy drained accumulator should be null."));
    }

    @Test
    @DisplayName("Cannot accumulate to a null consumer.")
    void drainNull() {
        fixture.accumulate("nifty");
        Assertions.assertThrows(NullPointerException.class, () -> fixture.drain(null), "Cannot drain to null.");
    }
}