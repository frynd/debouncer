package com.frynd.debouncer.regulator;

/**
 * Regulator interface.
 *
 * @apiNote The action may be run immediately when {@code requestAction}
 * is invoke or it may be run later, depending on implementation.
 */
public interface Regulator {
    /**
     * Request that the action be invoked.
     * When the action is performed is implementation specific.
     */
    void requestAction();
}
