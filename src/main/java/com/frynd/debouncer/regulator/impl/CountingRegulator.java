package com.frynd.debouncer.regulator.impl;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

/**
 * Counts the number of requests for action.
 * Each time a certain number of requests is met, runs the stored action immediately.
 */
public class CountingRegulator extends AbstractBaseRegulator {
    private final AtomicLong requestCount = new AtomicLong(0);
    private final LongUnaryOperator updateRequestNumber;

    /**
     * Create a new regulator that runs for each {@code count} times that
     * {@code requestAction} is invoked.
     *
     * @param count  the count of action requests that must be met before running the {@code action}
     * @param action the action to be run each time {@code count} action requests are made
     * @throws NullPointerException     if {@code action} is null.
     * @throws IllegalArgumentException if {@code count} is less than or equal to 0
     * @see #requestAction()
     */
    public CountingRegulator(long count, Runnable action) {
        super(action);
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0. Received [" + count + "]");
        }
        updateRequestNumber = value -> (value + 1) % count;
    }

    /**
     * Request that the action be run.
     * Will only actually run the action on the {@code count}-th time this is called.
     */
    @Override
    public void requestAction() {
        long requestNum = requestCount.updateAndGet(updateRequestNumber);
        if (requestNum == 0) {
            invokeAction();
        }
    }
}
