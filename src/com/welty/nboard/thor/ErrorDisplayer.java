package com.welty.nboard.thor;

/**
 * Interface to notify user of errors
 */
public interface ErrorDisplayer {
    /**
     * Notify the user that an error occurred
     *
     * @param operation operation in progress when error occurred
     * @param error     text of error notification
     */
    void notify(String operation, String error);
}
