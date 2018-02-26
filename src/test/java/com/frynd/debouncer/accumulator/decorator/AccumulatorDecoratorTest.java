package com.frynd.debouncer.accumulator.decorator;

import com.frynd.debouncer.accumulator.Accumulator;
import com.frynd.debouncer.accumulator.Drainers;
import com.frynd.debouncer.accumulator.impl.LatestValueAccumulator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccumulatorDecoratorTest {

    private Accumulator<String, String> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AccumulatorDecorator<>(
                new LatestValueAccumulator<>()
        );
    }

    @Test
    @DisplayName("Decorated value should be same as the supplied accumulator.")
    void getDecorated() {
        LatestValueAccumulator<Object> decorated = new LatestValueAccumulator<>();
        AccumulatorDecorator<Object, Object> decorating = new AccumulatorDecorator<>(decorated);
        Assertions.assertSame(decorated, decorating.getDecorated(), "Supplied accumulator should be the same as the decorated.");
    }

    @Test
    @DisplayName("Should not be able to make a decorated accumulator from null")
    void testConstructorNull() {
        Assertions.assertThrows(NullPointerException.class, () -> new AccumulatorDecorator<>(null));
    }

    //from here basically just copy LatestValueAccumulatorTest :)

    @Test
    @DisplayName("New accumulator should have null result.")
    void testInitialization() {
        fixture.drain(str -> Assertions.assertNull(str, "Newly created should be null."));
    }

    @Test
    @DisplayName("Latest value accumulator should accumulate the latest value.")
    void accumulate() {
        fixture.accumulate("umbrella");
        fixture.drain(str -> Assertions.assertEquals("umbrella", str, "Accumulating a single value should result in the same value."));

        fixture.accumulate("inconclusive");
        fixture.accumulate("questionable");
        fixture.drain(str -> Assertions.assertEquals("questionable", str, "Accumulating multiple values should result in the last provided value."));
    }

    @Test
    @DisplayName("Latest value accumulator should reset to null.")
    void drain() {
        fixture.accumulate("tenuous");
        fixture.drain(Drainers.noopDrainer());
        fixture.drain(str -> Assertions.assertNull(str, "Freshy drained accumulator should be null."));
    }

    @Test
    @DisplayName("Cannot accumulate to a null consumer.")
    void drainNull() {
        fixture.accumulate("nifty");
        Assertions.assertThrows(NullPointerException.class, () -> fixture.drain(null), "Cannot drain to null.");
    }

}