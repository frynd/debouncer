package com.frynd.debouncer.accumulator;

import com.frynd.debouncer.accumulator.impl.ListAccumulator;
import com.frynd.debouncer.drainer.Drainers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class DrainersTest {

    @Test
    @DisplayName("noopDrainer should do nothing.")
    void noopDrainer() {
        Consumer<Object> drainer = Drainers.noopDrainer();
        drainer.accept("");
        drainer.accept(1L);
        drainer.accept(0.5d);
    }

    @Test
    @DisplayName("noopDrainer should be castable.")
    void noopDrainerGenerics() {
        Consumer<String> stringDrainer = Drainers.noopDrainer();
        stringDrainer.accept("nothing");
        Consumer<Integer> integerDrainer = Drainers.noopDrainer();
        integerDrainer.accept(0);
    }

    @Test
    @DisplayName("Iterable drainer should drain each item.")
    void drainIterable() {
        AtomicInteger count = new AtomicInteger(0);
        Consumer<Collection<String>> drainer = Drainers.drainIterable(str -> count.incrementAndGet());
        List<String> strings = Arrays.asList("a", "b", "c", "d");

        drainer.accept(strings);
        Assertions.assertEquals(strings.size(), count.get(), "count should be incremented for each value.");

        count.set(0);
        strings = Arrays.asList("a", "b");
        drainer.accept(strings);
        Assertions.assertEquals(strings.size(), count.get(), "count should be incremented for each value.");

        Assertions.assertThrows(NullPointerException.class, () -> {
            Consumer<Iterable<Object>> shouldNotBeMade = Drainers.drainIterable(null);
            Assertions.fail("Drainer should not be made with null, but did: " + shouldNotBeMade);
        }, "Should not be able to create a null Iterable drainer.");
    }

    @Test
    @DisplayName("drainMap should drain each accumulator in a map.")
    void drainMap() {
        Map<String, Accumulator<Record, List<Record>>> mapping = new HashMap<>();
        ListAccumulator<Record> waimarieRecords = new ListAccumulator<>();
        ListAccumulator<Record> nikauRecords = new ListAccumulator<>();

        waimarieRecords.accumulate(Record.Records.waimarie0);
        waimarieRecords.accumulate(Record.Records.waimarie1);
        nikauRecords.accumulate(Record.Records.nikau0);

        mapping.put(Record.Users.WAIMARIE, waimarieRecords);
        mapping.put(Record.Users.NIKAU, nikauRecords);

        List<String> users = new ArrayList<>(2);
        users.add(Record.Users.WAIMARIE);
        users.add(Record.Users.NIKAU);

        BiConsumer<String, List<Record>> consumer = (user, records) -> {
            users.remove(user);
            if (Record.Users.WAIMARIE.equals(user)) {
                Assertions.assertEquals(2, records.size());
            } else if (Record.Users.NIKAU.equals(user)) {
                Assertions.assertEquals(1, records.size());
            } else {
                Assertions.fail("user[" + user + "] not recognized.");
            }
        };

        Drainers.<String, Record, List<Record>>drainMap(consumer).accept(mapping);
        Assertions.assertTrue(users.isEmpty(), "Each key should be visited.");

        Assertions.assertThrows(NullPointerException.class, () -> {
            Consumer<Map<Object, Accumulator<Object, Object>>> shouldNotBeMade = Drainers.drainMap(null);
            Assertions.fail("Drainer should not be made with null, but did: " + shouldNotBeMade);

        }, "Should not be able to create a null map drainer.");
    }
}