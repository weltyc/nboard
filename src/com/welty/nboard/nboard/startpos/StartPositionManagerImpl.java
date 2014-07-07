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

import com.welty.nboard.nboard.ReversiWindow;
import com.welty.othello.gui.MenuButtonGroup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class StartPositionManagerImpl implements StartPositionManager {
    private final MenuButtonGroup startPosition;

    public StartPositionManagerImpl() {
        startPosition = new MenuButtonGroup("StartPosition", ReversiWindow.class, "Standard", "Alternate", "XOT", "F5");
    }

    @NotNull @Override public StartPosition getStartPosition() {
        final String startPositionType = startPosition.getSelectedString();
        return StartPositionChooser.next(startPositionType);
    }

    @Override public void addChoicesToMenu(JMenu menu) {
        startPosition.addTo(menu);
    }
}
