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
