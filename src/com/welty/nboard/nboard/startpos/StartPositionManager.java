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

package com.welty.nboard.nboard.startpos;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Manages everything related to choosing the start position for a game.
 * <p/>
 * All operations are expected to take place on the EDT.
 */
public interface StartPositionManager {
    /**
     * Get the start position for a new game.
     *
     * @return A start position.
     */
    @NotNull StartPosition getStartPosition();

    /**
     * Add required menu items so the start position can be selected
     *
     * @param menu menu to add to
     */
    void addChoicesToMenu(JMenu menu);
}
