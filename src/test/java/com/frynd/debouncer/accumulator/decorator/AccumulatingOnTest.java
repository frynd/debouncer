package com.frynd.debouncer.accumulator.decorator;

import com.frynd.debouncer.accumulator.Accumulator;
import com.frynd.debouncer.accumulator.impl.LatestValueAccumulator;
import com.frynd.debouncer.accumulator.impl.ListAccumulator;
import com.frynd.debouncer.test.CountingExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class AccumulatingOnTest {

    @Test
    @DisplayName("AccumulatingOn requires a non-null accumulator and executor.")
    void constructor() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new AccumulatingOn<>(new ListAccumulator<String>(new ArrayList<>()), null),
                "Cannot create accumulator that accumulates on null.");
    }

    @Test
    @DisplayName("AccumulatingOn should accumulate through the executor.")
    void accumulate() {
        CountingExecutor countingExecutor = new CountingExecutor();

        Accumulator<String, String> accumulator = new LatestValueAccumulator<>();
        accumulator = new AccumulatingOn<>(accumulator, countingExecutor);
        accumulator.accumulate("a");
        accumulator.accumulate("b");
        accumulator.accumulate("c");
        accumulator.accumulate("d");

        Assertions.assertEquals(4, countingExecutor.getCount(), "Supplied 4 values through the executor.");

        accumulator.drain(str -> Assertions.assertEquals("d", str,
                "Latest value should still be final value supplied."));

        Assertions.assertEquals(4, countingExecutor.getCount(), "Only supplied 4 values through the executor.");
    }
}