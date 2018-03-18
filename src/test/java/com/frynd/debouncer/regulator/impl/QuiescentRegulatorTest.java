package com.frynd.debouncer.regulator.impl;

import com.frynd.debouncer.regulator.Regulator;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class QuiescentRegulatorTest {

    private static ScheduledExecutorService scheduler;

    @BeforeAll
    static void init() {
        scheduler = Executors.newScheduledThreadPool(1);
    }

    @Test
    @DisplayName("QuiescentRegulator requires a non-null regulator, a positive delay, and a non-null action.")
    void constructor() {
        Runnable noop = () -> { /*no-op*/ };

        Assertions.assertThrows(NullPointerException.class,
                () -> new QuiescentRegulator(null, 100, noop),
                "Cannot create a scheduled regulator from a null scheduler.");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QuiescentRegulator(scheduler, -1, noop),
                "Cannot create a scheduled regulator from a negative delay.");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new QuiescentRegulator(scheduler, 0, noop),
                "Cannot create a scheduled regulator from a zero delay.");

        Assertions.assertThrows(NullPointerException.class,
                () -> new QuiescentRegulator(scheduler, 10, null),
                "Cannot create a scheduled regulator from a null action.");

        new QuiescentRegulator(scheduler, 10, noop);
    }

    @Test
    @DisplayName("QuiescentRegulator should delay action by the delay amount and should ignore additional calls in between.")
    void requestAction() {
        AtomicInteger nextNumber = new AtomicInteger(0);
        long delayMillis = 100;
        Duration delay = new Duration(delayMillis, TimeUnit.MILLISECONDS);

        Regulator regulator = new QuiescentRegulator(scheduler, delayMillis, nextNumber::incrementAndGet);
        Assertions.assertEquals(0, nextNumber.get(), "Nothing should have been run yet.");

        regulator.requestAction();
        Awaitility.await().between(delay.divide(2), delay.multiply(2)).untilAtomic(nextNumber, IsEqual.equalTo(1));

        long start = System.currentTimeMillis();
        long end;
        do {
            regulator.requestAction();
            end = System.currentTimeMillis();
            Assertions.assertEquals(1, nextNumber.get(), "No new actions should be run while new events keep coming in.");
        } while (end - start < delayMillis * 3);

        //Once actions stop coming in, should fire after delay.
        Awaitility.await().between(delay.divide(2), delay.multiply(2)).untilAtomic(nextNumber, IsEqual.equalTo(2));
    }

    @AfterAll
    static void dispose() {
        scheduler.shutdown();
    }
}