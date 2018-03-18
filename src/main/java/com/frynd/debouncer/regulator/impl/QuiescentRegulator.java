package com.frynd.debouncer.regulator.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Regulator that delays requests for action by a given amount, and waits for requests to calm down.
 * Requests that happen after the first request, but before the next run will delay the next run.
 * e.g. for a QuiescentRegulator with delay of 5.
 * <pre>
 *     t00 - requestAction
 *     t01 - requestAction
 *     t02 -
 *     t03 - requestAction
 *     t04 -
 *     t05 -
 *     t06 -
 *     t07 -
 *     t08 - requestAction //action could be run depending on ordering of checking time vs action request
 *     t09 - requestAction
 *     t10 -
 *     t11 -
 *     t12 -
 *     t13 -
 *     t14 - **action is run**
 * </pre>
 * <p>
 * Usage: <pre>{@code
 *   ScheduledExecutorService scheduler = ...;
 *   AtomicInteger value = new AtomicInteger(0);
 *   Regulator regulator = new QuiescentRegulator(scheduler, 100, () -> value.incrementAndGet());
 *   regulator.requestAction(); //value is still 0
 *   regulator.requestAction(); //value is still 0
 *   // ~100 ms later, value is 1
 * }</pre>
 *
 * @see java.util.concurrent.Executors
 */
public class QuiescentRegulator extends AbstractBaseRegulator {

    private final ScheduledExecutorService scheduler;
    private final long delayMillis;
    private final AtomicLong lastRequestTimestamp = new AtomicLong();
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    /**
     * Create a delayed regulator that uses the {@code scheduler} to schedule a delay of {@code delayMillis}
     * after action requests before running {@code action}. Subsequent requests reset the delay.
     *
     * @param scheduler   the scheduler service to be used for scheduling the delay
     * @param delayMillis the delay amount in milliseconds
     * @param action      the action to be performed after request action is called.
     * @throws NullPointerException     if {@code action} or {@code scheduler} is null.
     * @throws IllegalArgumentException if {@code delayMillis} is less than or equal to 0
     */
    public QuiescentRegulator(ScheduledExecutorService scheduler, long delayMillis, Runnable action) {
        super(action);
        Objects.requireNonNull(scheduler);
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("delayMillis must be greater than 0. Received [" + delayMillis + "]");
        }
        this.scheduler = scheduler;
        this.delayMillis = delayMillis;
    }

    @Override
    public void requestAction() {
        long now = getCurrentMillis();
        long result = lastRequestTimestamp.updateAndGet(prev -> prev < now ? now : prev);

        if (result == now &&
                scheduled.compareAndSet(false, true)) {
            scheduler.schedule(this::tryAction, delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    private void tryAction() {
        long last = lastRequestTimestamp.get();
        long now = getCurrentMillis();

        if (now >= last + delayMillis) {
            scheduled.set(false);
            invokeAction();
        } else {
            long next = delayMillis - (now - last);
            scheduler.schedule(this::tryAction, next, TimeUnit.MILLISECONDS);
        }

    }

    //extracted for easy replacement
    private long getCurrentMillis() {
        return Instant.now().toEpochMilli();
    }
}
