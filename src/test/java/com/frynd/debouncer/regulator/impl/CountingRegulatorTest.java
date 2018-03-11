package com.frynd.debouncer.regulator.impl;

import com.frynd.debouncer.regulator.Regulator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

class CountingRegulatorTest {

    @Test
    @DisplayName("Counting regulator requires a non-null action and a positive maxCount.")
    void constructor() {
        Runnable noop = () -> {/*no-op*/};
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CountingRegulator(-5, noop),
                "Cannot create counting regulator from negative count.");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CountingRegulator(0, noop),
                "Cannot create counting regulator from count of zero.");
        Assertions.assertThrows(NullPointerException.class, () -> new CountingRegulator(10, null),
                "Cannot create regulator from null action.");

        new CountingRegulator(10, noop);
    }

    @Test
    @DisplayName("Counting regulator should run action after each maxCount requests.")
    void requestAction() {
        AtomicBoolean value = new AtomicBoolean(false);
        int count = 10;

        Regulator regulator = new CountingRegulator(count, () -> value.set(true));

        for (int runCount = 0; runCount < 5; runCount++) { //Make sure regulator works after initial run
            value.set(false);

            Assertions.assertFalse(value.get(), "Before action requested, value should not have changed.");

            for (int i = 1; i < count; ++i) {
                regulator.requestAction();
                final int requestNum = i;
                Assertions.assertFalse(value.get(), () -> "Before " + count +
                        " actions requested, value should not have changed. Value changed at: " + requestNum);
            }

            regulator.requestAction();
            Assertions.assertTrue(value.get(), "After " + count + " actions requested. Value should have changed.");
        }
    }
}