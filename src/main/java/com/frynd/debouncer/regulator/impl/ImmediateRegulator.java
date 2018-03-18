package com.frynd.debouncer.regulator.impl;

/**
 * Regulator that immediately invokes its {@code action}
 * when {@code requestAction} is invoked. Exceptions
 * thrown during the {@code action} will be passed to
 * the invoker of {@code requestAction}
 * Usage: <pre>{@code
 *   AtomicInteger value = new AtomicInteger(0);
 *   Regulator regulator = new ImmediateRegulator(() -> value.incrementAndGet());
 *   regulator.requestAction(); //value is now 1
 * }</pre>
 *
 * @see #requestAction()
 */
public class ImmediateRegulator extends AbstractBaseRegulator {

    /**
     * Create a new {@link ImmediateRegulator} that uses
     * the provided {@code action}.
     *
     * @param action the action to be performed each time {@code requestAction} is invoked
     * @throws NullPointerException if {@code action} is null.
     * @see #requestAction()
     */
    public ImmediateRegulator(Runnable action) {
        super(action);
    }

    /**
     * Immediately invoke the stored action.
     */
    @Override
    public void requestAction() {
        invokeAction();
    }
}
