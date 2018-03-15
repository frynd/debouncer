package com.frynd.debouncer.regulator.impl;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Regulator that delays requests for action by a given amount.
 * Requests that happen after the first request, but before the next run are ignored.
 * e.g. for a DelayedRegulator with delay of 5.
 * <pre>
 *     t00 - requestAction
 *     t01 - requestAction
 *     t02 -
 *     t03 - requestAction
 *     t04 -
 *     t05 - **action is run**
 *     t06 -
 *     t07 -
 *     t08 - requestAction
 *     t09 - requestAction
 *     t10 -
 *     t11 -
 *     t12 -
 *     t13 - **action is run**
 * </pre>
 */
public class DelayedRegulator extends AbstractBaseRegulator {
    private final ScheduledExecutorService scheduler;
    private final long delayMillis;
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    /**
     * Create a delayed regulator that uses the {@code scheduler} to schedule a delay of {@code delayMillis}
     * after action requests before running {@code action}.
     *
     * @param scheduler   the scheduler service to be used for scheduling the delay
     * @param delayMillis the delay amount in milliseconds
     * @param action      the action to be performed after request action is called.
     * @throws NullPointerException     if {@code action} or {@code scheduler} is null.
     * @throws IllegalArgumentException if {@code delayMillis} is less than or equal to 0
     * @see DelayedRegulator
     */
    public DelayedRegulator(ScheduledExecutorService scheduler, long delayMillis, Runnable action) {
        super(action);
        Objects.requireNonNull(scheduler);
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("delayMillis must be greater than 0. Received [" + delayMillis + "]");
        }
        this.scheduler = scheduler;
        this.delayMillis = delayMillis;
    }

    /**
     * Request that the action be run.
     * This may occur up to {@code delay} milliseconds after this call.
     * The first call to this (or the first after a run) will schedule a new run.
     * Calls after the scheduling call, but before the delay occurs are ignored.
     */
    @Override
    public void requestAction() {
        //CAS returns true if the operation was successful
        boolean isSetter = scheduled.compareAndSet(false, true);

        if (isSetter) {
            scheduler.schedule(() -> {
                scheduled.set(false);
                invokeAction();
            }, delayMillis, TimeUnit.MILLISECONDS);
        }
    }

}
