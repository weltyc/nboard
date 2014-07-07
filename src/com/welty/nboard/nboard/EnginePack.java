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

package com.welty.nboard.nboard;

import com.welty.nboard.nboard.engine.EngineSynchronizer;
import com.welty.nboard.nboard.engine.ReversiWindowEngine;
import com.welty.nboard.nboard.selector.GuiOpponentSelector;
import com.welty.othello.api.PingPong;

public class EnginePack {
    final ReversiWindowEngine engine;
    final GuiOpponentSelector selector;

    public EnginePack(String windowTitle, boolean includeWeakEngines, String preferencePrefix, String type, PingPong pingPong, ReversiWindowEngine.Listener listener) {
        selector = new GuiOpponentSelector(windowTitle, includeWeakEngines, preferencePrefix, type);
        engine = new EngineSynchronizer(preferencePrefix, pingPong, selector, listener);
    }

    public String getName() {
        return engine.getName();
    }
}
