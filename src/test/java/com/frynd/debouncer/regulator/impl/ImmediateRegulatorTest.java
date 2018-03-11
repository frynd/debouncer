package com.frynd.debouncer.regulator.impl;

import com.frynd.debouncer.regulator.Regulator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

class ImmediateRegulatorTest {

    @Test
    @DisplayName("New regulator should create a non-null regulator.")
    void newRegulator() {
        Regulator regulator = new ImmediateRegulator(() -> {/*no-op*/});

        Assertions.assertNotNull(regulator, "Result of a regulator with a non-null action should be non-null.");

        Assertions.assertThrows(NullPointerException.class, () -> new ImmediateRegulator(null),
                "Immediate regulator should not be able to be created from null.");
    }

    @Test
    @DisplayName("Requesting action should invoke the action immediately.")
    void requestAction() {
        AtomicBoolean value = new AtomicBoolean(false);
        Regulator regulator = new ImmediateRegulator(() -> value.set(true));

        Assertions.assertFalse(value.get(), "Before action is requested, value should not have changed.");

        regulator.requestAction();

        Assertions.assertTrue(value.get(), "After action is requested, value should have changed.");
    }

    @Test
    @DisplayName("Requesting an action that throws exceptions should relay the exception to the caller.")
    void requestThrowingAction() {
        ImmediateRegulator regulator = new ImmediateRegulator(() -> {
            throw new UnsupportedOperationException("Not supported.");
        });

        Assertions.assertThrows(UnsupportedOperationException.class, regulator::requestAction,
                "Thrown exception should occur on caller.");
    }
}