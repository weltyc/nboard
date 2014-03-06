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
