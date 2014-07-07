/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

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
