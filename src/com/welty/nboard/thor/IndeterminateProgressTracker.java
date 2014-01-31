package com.welty.nboard.thor;

public interface IndeterminateProgressTracker extends AutoCloseable {
    /**
     * Notify the progress tracker that the progress amount has increased by 1.
     */
    public void increment();

    /**
     * Notify the progress tracker that it should display progress immediately.
     * <p/>
     * If this is not called, the progress tracker may choose to display progress only every second or so.
     */
    public void update();

    /**
     * Close the window.
     * <p/>
     * This is an override because we promise not to throw a checked exception when we close.
     */
    @Override public void close();
}
