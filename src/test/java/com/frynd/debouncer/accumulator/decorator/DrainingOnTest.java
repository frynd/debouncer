package com.frynd.debouncer.accumulator.decorator;

import com.frynd.debouncer.accumulator.Accumulator;
import com.frynd.debouncer.accumulator.impl.LatestValueAccumulator;
import com.frynd.debouncer.accumulator.impl.ListAccumulator;
import com.frynd.debouncer.test.CountingExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DrainingOnTest {

    @Test
    @DisplayName("DrainingOn requires a non-null accumulator and executor.")
    void constructor() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new DrainingOn<>(new ListAccumulator<String>(), null),
                "Cannot create accumulator that accumulates on null.");
    }

    @Test
    @DisplayName("DrainingOn should drain through the provided executor.")
    void accumulate() {
        CountingExecutor countingExecutor = new CountingExecutor();
        Accumulator<String, String> accumulator = new LatestValueAccumulator<>();
        accumulator = new DrainingOn<>(accumulator, countingExecutor);
        accumulator.accumulate("a");
        accumulator.accumulate("b");
        accumulator.accumulate("c");
        accumulator.accumulate("d");

        Assertions.assertEquals(0, countingExecutor.getCount(), "Nothing should have gone through the executor yet.");

        accumulator.drain(str -> Assertions.assertEquals("d", str,
                "Latest value should still be final value supplied."));

        Assertions.assertEquals(1, countingExecutor.getCount(), "Drained through executor once.");
    }
}