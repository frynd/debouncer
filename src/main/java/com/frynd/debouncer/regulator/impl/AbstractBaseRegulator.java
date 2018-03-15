package com.frynd.debouncer.regulator.impl;

import com.frynd.debouncer.regulator.Regulator;

import java.util.Objects;

/**
 * Abstract convenience class for containing a Runnable as the action for a regulator.
 * Regulators need not extend this class, just a convenient base class.
 */
public abstract class AbstractBaseRegulator implements Regulator {
    private final Runnable action;

    /**
     * Create a regulator that stores a runnable action as its action.
     *
     * @param action the action to be stored
     * @throws NullPointerException if {@code action} is null.
     */
    protected AbstractBaseRegulator(Runnable action) {
        Objects.requireNonNull(action);
        this.action = action;
    }

    /**
     * Can be invoked from child classes to invoke the stored runnable.
     */
    protected void invokeAction() {
        action.run();
    }

}
