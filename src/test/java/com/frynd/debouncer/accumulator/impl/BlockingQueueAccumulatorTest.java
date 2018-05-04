package com.frynd.debouncer.accumulator.impl;

import com.frynd.debouncer.accumulator.Accumulator;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

class BlockingQueueAccumulatorTest {

    private static final int CONCURRENCY = 100;
    private static final Comparator<Integer> COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());

    private Accumulator<Integer, List<Integer>> fixture;
    private ExecutorService service;

    @BeforeEach
    void setUp() {
        fixture = new BlockingQueueAccumulator<>(new LinkedBlockingQueue<>());
        service = Executors.newFixedThreadPool(CONCURRENCY);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }


    @RepeatedTest(10)//Due to non-deterministic nature, even non-concurrent version can frequently pass
    @DisplayName("Concurrent accumulator should handle multiple threads attempting access simultaneously.")
    void accumulate() throws Exception {
        CyclicBarrier startBarrier = new CyclicBarrier(CONCURRENCY + 1); //+1 for this thread

        List<Integer> expected = new ArrayList<>(CONCURRENCY);
        List<Future<?>> futures = new ArrayList<>(CONCURRENCY);

        for (int i = 0; i < CONCURRENCY; ++i) {
            final int submission = i;
            futures.add(service.submit(() -> {
                startBarrier.await();
                fixture.accumulate(submission);
                return null;
            }));
            expected.add(submission);
        }

        startBarrier.await();
        //Not strictly needed to sort the expected list, but wanted to make sure the same operation on expected as values
        expected.sort(COMPARATOR);

        for (Future<?> future : futures) {
            future.get();
        }

        fixture.drain(values -> {
            values.sort(COMPARATOR);
            Assertions.assertIterableEquals(expected, values, "Should contain all values.");
        });
    }

    @SuppressWarnings("squid:S2925")//Sleeping in submits to space out accumulates while main thread reads
    @RepeatedTest(10)//Due to non-deterministic nature, even non-concurrent version can frequently pass
    @DisplayName("Concurrent accumulator should be able to drain mid-accumulate storm.")
    void drain() throws Exception {
        CyclicBarrier startBarrier = new CyclicBarrier(CONCURRENCY + 1); //+1 for this thread
        Comparator<Integer> comparator = Comparator.nullsFirst(Comparator.naturalOrder());

        List<Integer> expected = new ArrayList<>(CONCURRENCY);
        List<Future<?>> futures = new ArrayList<>(CONCURRENCY);

        for (int i = 0; i < CONCURRENCY; ++i) {
            final int submission = i;
            futures.add(
                    service.submit(() -> {
                        startBarrier.await();
                        Thread.sleep((submission / 10) * 2);
                        fixture.accumulate(submission);
                        return null;
                    })
            );
            expected.add(submission);
        }

        startBarrier.await();

        CopyOnWriteArrayList<Integer> actualList = new CopyOnWriteArrayList<>();
        Thread.sleep(10);
        fixture.drain(actualList::addAll);
        Thread.sleep(10);
        fixture.drain(actualList::addAll);

        //Not strictly needed to sort the expected list, but wanted to make sure the same operation on expected as values
        expected.sort(comparator);

        for (Future<?> future : futures) {
            future.get();
        }

        fixture.drain(actualList::addAll);
        actualList.sort(COMPARATOR);
        Assertions.assertIterableEquals(expected, actualList, "Draining multiple times should result in same.");
    }
}