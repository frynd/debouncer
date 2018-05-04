package com.frynd.debouncer;

import com.frynd.debouncer.accumulator.impl.ListAccumulator;
import com.frynd.debouncer.drainer.Drainers;
import com.frynd.debouncer.regulator.impl.CountingRegulator;
import com.frynd.debouncer.regulator.impl.ImmediateRegulator;
import com.frynd.debouncer.test.CountingExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class DebouncerTest {

    @Test
    @DisplayName("Debouncer builder requires non-null accumulator, regulator, and drainer.")
    void accumulating() {
        Debouncer<Integer> debouncer = Debouncer.accumulating(new ListAccumulator<Integer>())
                .regulating(ImmediateRegulator::new).draining(Drainers.noopDrainer()).build();
        Assertions.assertNotNull(debouncer, "Debounce builder should return non-null.");

        Assertions.assertThrows(NullPointerException.class,
                () -> Debouncer.accumulating(null),
                "Shouldn't be able to start a debouncer with a null accumulator.");

        Assertions.assertThrows(NullPointerException.class,
                () -> Debouncer.accumulating(new ListAccumulator<Integer>()).accumulatingOn(null),
                "Shouldn't be able to build a debouncer with a null accumulation executor.");

        Assertions.assertThrows(NullPointerException.class,
                () -> Debouncer.accumulating(new ListAccumulator<Integer>()).drainingOn(null),
                "Shouldn't be able to build a debouncer with a null drain executor.");

        Assertions.assertThrows(NullPointerException.class,
                () -> Debouncer.accumulating(new ListAccumulator<Integer>()).regulating(null),
                "Shouldn't be able to build a debouncer with a null regulator.");

        Assertions.assertThrows(NullPointerException.class,
                () -> Debouncer.accumulating(new ListAccumulator<Integer>())
                        .regulating(ImmediateRegulator::new)
                        .draining(null),
                "Shouldn't be able to build a debouncer with a null drainer.");
    }

    @Test
    @DisplayName("Debouncer should accumulate events until the regulator runs.")
    void accumulate() {
        int count = 10;
        List<Integer> numbers = new ArrayList<>(count);
        Debouncer<Integer> debouncer = Debouncer.accumulating(new ListAccumulator<Integer>())
                .regulating(runnable -> new CountingRegulator(count, runnable))
                .draining(acc -> {
                    numbers.clear();
                    numbers.addAll(acc);
                })
                .build();

        Assertions.assertTrue(numbers.isEmpty(), "Numbers should still be empty.");

        List<Integer> expected = new ArrayList<>(count);
        for (int i = 0; i < count - 1; ++i) {
            debouncer.accumulate(i);
            Assertions.assertTrue(numbers.isEmpty(), "Numbers should still be empty on iteration [" + i + "]. Was: " + numbers);
            expected.add(i);
        }

        expected.add(count - 1);
        debouncer.accumulate(count - 1);
        Assertions.assertIterableEquals(expected, numbers, "Numbers should have been updated.");
    }

    @Test
    void accumulateExecutors() {
        CountingExecutor accumulationExecutor = new CountingExecutor();
        CountingExecutor drainerExecutor = new CountingExecutor();

        int count = 10;
        List<Integer> numbers = new ArrayList<>(count);
        Debouncer<Integer> debouncer = Debouncer.accumulating(new ListAccumulator<Integer>())
                .accumulatingOn(accumulationExecutor)
                .drainingOn(drainerExecutor)
                .regulating(runnable -> new CountingRegulator(count, runnable))
                .draining(acc -> {
                    numbers.clear();
                    numbers.addAll(acc);
                })
                .build();

        Assertions.assertTrue(numbers.isEmpty(), "Numbers should still be empty.");

        List<Integer> expected = new ArrayList<>(count);
        for (int i = 0; i < count - 1; ++i) {
            debouncer.accumulate(i);
            Assertions.assertTrue(numbers.isEmpty(), "Numbers should still be empty on iteration [" + i + "]. Was: " + numbers);
            expected.add(i);
        }

        expected.add(count - 1);
        debouncer.accumulate(count - 1);
        Assertions.assertIterableEquals(expected, numbers, "Numbers should have been updated.");

        Assertions.assertEquals(count, accumulationExecutor.getCount(), "[" + count + "] events were accumulated.");
        Assertions.assertEquals(1, drainerExecutor.getCount(), "Only one drain should have been performed.");
    }
}