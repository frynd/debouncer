package com.frynd.debouncer.examples.hello;

import com.frynd.debouncer.Debouncer;
import com.frynd.debouncer.accumulator.impl.ListAccumulator;
import com.frynd.debouncer.regulator.impl.DelayedRegulator;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloWorldDemo {

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        long delay = Duration.ofSeconds(1).toMillis();

        Debouncer<Integer> debouncer = Debouncer.accumulating(new ListAccumulator<Integer>())
                .regulating(runnable -> new DelayedRegulator(scheduler, delay, runnable))
                .draining(numbers -> System.out.println("Accumulated: " + numbers))
                .build();

        AtomicInteger count = new AtomicInteger(0);

        scheduler.scheduleAtFixedRate(() -> debouncer.accumulate(count.getAndIncrement()), delay / 9,
                delay / 9, TimeUnit.MILLISECONDS);
    }
}

