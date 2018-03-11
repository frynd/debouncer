package com.frynd.debouncer.test;

import java.util.concurrent.Executor;

/**
 * Test executor class.
 * Not a great executor, but indicates how many commands were run.
 *
 * @see #getCount()
 */
public class CountingExecutor implements Executor {
    private int counter = 0;

    @Override
    public void execute(Runnable command) {
        ++counter;
        command.run();
    }

    public int getCount() {
        return counter;
    }
}
