package com.welty.nboard.thor;

public interface IndeterminateProgressTracker extends AutoCloseable {
    /**
     * Notify the progress tracker that the progress amount has increased by 1.
     */
    public void increment();

    /**
     * Promise not to throw a checked exception when we close.
     */
    @Override public void close();
}
