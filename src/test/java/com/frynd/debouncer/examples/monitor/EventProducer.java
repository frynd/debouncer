package com.frynd.debouncer.examples.monitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Produces events with either a given frequency or in a cpu-bound loop.
 * In context here, an event is a pair of (String, Integer) that consists
 * of the name of this producer and an integer that either stays the same or
 * increases until it loops back to 0.
 * Values will be between 0 and 100, inclusive
 */
public class EventProducer {
    //Maximum iterations before looping back
    //Higher values cause more repeated values
    private static final int MAX_ITER = 5_000;

    //Name of the event producer
    private final String name;
    //Callback to feed events to
    private final BiConsumer<String, Integer> callback;

    //Executor service that handles production of events
    private final ExecutorService service;

    //Which iteration the producer is on
    private int iteration = 0;

    //Flag to tell the producer to stop producing events
    private volatile boolean shutdown = false;


    /**
     * Construct a producer that produces events in a cpu-bound loop.
     *
     * @param name     the name the producer will provide with every call to {@code callback}.
     * @param callback the callback to invoke for each event.
     */
    EventProducer(String name, BiConsumer<String, Integer> callback) {
        this.name = name;
        this.callback = callback;
        service = Executors.newSingleThreadScheduledExecutor(this::newThread);
        service.submit(() -> {
            while (!Thread.currentThread().isInterrupted() && !shutdown) {
                createEvent();
            }
        });
    }

    /**
     * Construct a producer that rate limits the number of events per second to {@code eventsPerSecond}.
     *
     * @param name            the name the producer will provide with every call to {@code callback}.
     * @param eventsPerSecond the number of events to be produced each second.
     * @param callback        the callback to invoke for each event.
     */
    EventProducer(String name, int eventsPerSecond, BiConsumer<String, Integer> callback) {
        this.name = name;
        this.callback = callback;

        float period = 1.0f / eventsPerSecond;
        long periodMillis = Math.round(period * 1000);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(this::newThread);
        scheduler.scheduleAtFixedRate(this::createEvent, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
        service = scheduler;
    }

    /**
     * The name of this producer.
     * Passed to every call of {@code callback}
     *
     * @return the name of this producer.
     */
    public String getName() {
        return name;
    }

    /**
     * Shutdown this event producer.
     * Invokes {@code callback} one more time with -1.
     */
    public void shutdown() {
        shutdown = true;
        service.shutdownNow();
        callback.accept(name, -1);
    }

    private void createEvent() {
        if (!shutdown) {
            iteration++;

            Integer eventValue = computeEventValue(iteration);
            callback.accept(name, eventValue);

            if (iteration > MAX_ITER) {
                iteration = 0;
            }
        }
    }

    private Integer computeEventValue(int iteration) {
        return (int) (iteration * (100.0 / MAX_ITER));
    }

    private Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, name + "-scheduler");
        thread.setDaemon(true);
        return thread;
    }
}
