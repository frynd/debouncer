package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.accumulator.Accumulator;
import com.frynd.debouncer.accumulator.Drainers;
import com.frynd.debouncer.accumulator.Record;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

class MapAccumulatorTest {

    private MapAccumulator<Record, String, Record> fixture;

    @BeforeEach
    void setUp() {
        fixture = new MapAccumulator<>(
                Record::getName,
                LatestValueAccumulator::new
        );
    }

    @Test
    @DisplayName("New accumulator should have empty result.")
    void testInitialization() {
        fixture.drain(map -> Assertions.assertTrue(map.isEmpty(), "Newly created should be empty."));
    }

    @Test
    @DisplayName("Map accumulator accumulates values by keys.")
    void accumulate() {
        fixture.accumulate(Record.Records.waimarie0);

        fixture.drain(map -> {
            Assertions.assertEquals(1, map.size(), "Only one record added.");
            Assertions.assertTrue(map.containsKey(Record.Users.WAIMARIE), "Record should be under name key.");
            map.get(Record.Users.WAIMARIE).drain(record -> Assertions.assertEquals(Record.Records.waimarie0, record, "Drained record should be only one provided."));
        });

        fixture.accumulate(Record.Records.waimarie0);
        fixture.accumulate(Record.Records.nikau0);
        fixture.accumulate(Record.Records.waimarie1);

        fixture.drain(map -> {
            Assertions.assertEquals(2, map.size(), "Both records should be added.");

            Assertions.assertTrue(map.containsKey(Record.Users.WAIMARIE), "Record should be under name key.");
            Assertions.assertTrue(map.containsKey(Record.Users.NIKAU), "Record should be under name key.");

            map.get(Record.Users.WAIMARIE).drain(record -> Assertions.assertEquals(Record.Records.waimarie1, record, "Drained record should latest accumulated under the same key."));

            map.get(Record.Users.NIKAU).drain(record -> Assertions.assertEquals(Record.Records.nikau0, record, "Drained value should be only one provided for key."));
        });
    }


    @Test
    @DisplayName("Map accumulator should retain keys after accumulation.")
    void drain() {
        fixture.accumulate(Record.Records.waimarie0);
        fixture.drain(Drainers.noopDrainer());
        fixture.drain(map -> Assertions.assertEquals(1, map.size(), "Freshly drained accumulator should be retain keys."));
        fixture.drain(Drainers.drainMap((user, record) -> {
                    Assertions.assertEquals(Record.Users.WAIMARIE, user, "Only one key should still be around.");
                    Assertions.assertNull(record, "Record was already drained.");
                }));
    }

    @Test
    @DisplayName("Cannot accumulate to a null consumer.")
    void drainNull() {
        fixture.accumulate(Record.Records.waimarie0);
        Assertions.assertThrows(NullPointerException.class, () -> fixture.drain(null), "Cannot drain to null.");
    }

    @Test
    @DisplayName("Map accumulator should not be constructed with null params.")
    void testNullConstructorParams() {
        Assertions.assertThrows(NullPointerException.class, () -> new MapAccumulator<>(null, (Supplier<Accumulator<Object, List<Object>>>) ListAccumulator::new), "Map accumulator should not be created if parameters are null.");
        Assertions.assertThrows(NullPointerException.class, () -> new MapAccumulator<>(Record::getName, null), "Map accumulator should not be created if parameters are null.");
        Assertions.assertThrows(NullPointerException.class, () -> new MapAccumulator<>(null, null), "Map accumulator should not be created if parameters are null.");
    }

    @Test
    @DisplayName("Testing compatibility with Drainers.drainMap")
    void drainMap() {
        fixture.accumulate(Record.Records.waimarie0);
        fixture.accumulate(Record.Records.nikau0);
        fixture.accumulate(Record.Records.waimarie1);

        HashSet<Record> expectedRecords = new HashSet<>(2);
        expectedRecords.add(Record.Records.waimarie1);
        expectedRecords.add(Record.Records.nikau0);

        fixture.drain(Drainers.drainMap((name, record) -> expectedRecords.remove(record)));
        Assertions.assertTrue(expectedRecords.isEmpty(), "All records should have been removed.");
    }

    @Test
    @DisplayName("Testing compatibility with Drainers.drainMap for non-unary")
    void drainMapList() {
        MapAccumulator<Record, String, List<Record>> fixture = new MapAccumulator<>(
                Record::getName,
                ListAccumulator::new
        );
        fixture.accumulate(Record.Records.waimarie0);
        fixture.accumulate(Record.Records.nikau0);
        fixture.accumulate(Record.Records.waimarie1);

        HashSet<Record> expectedRecords = new HashSet<>(2);
        expectedRecords.add(Record.Records.waimarie0);
        expectedRecords.add(Record.Records.waimarie1);
        expectedRecords.add(Record.Records.nikau0);

        BiConsumer<String, List<Record>> consumer = (user, records) -> {
            expectedRecords.removeAll(records);
            if (Record.Users.WAIMARIE.equals(user)) {
                Assertions.assertEquals(2, records.size());
            } else if (Record.Users.NIKAU.equals(user)) {
                Assertions.assertEquals(1, records.size());
            } else {
                Assertions.fail("user[" + user + "] not recognized.");
            }
        };

        fixture.drain(Drainers.drainMap(consumer));
        Assertions.assertTrue(expectedRecords.isEmpty(), "All records should have been removed.");
    }

}