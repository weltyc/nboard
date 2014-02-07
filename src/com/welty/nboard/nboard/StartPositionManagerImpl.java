package com.welty.nboard.nboard;

import com.welty.novello.core.Position;
import com.welty.othello.gui.MenuButtonGroup;
import com.welty.othello.gui.StartPositionChooser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class StartPositionManagerImpl implements StartPositionManager {
    private final MenuButtonGroup startPosition;

    public StartPositionManagerImpl() {
        startPosition = new MenuButtonGroup("StartPosition", ReversiWindow.class, "Standard", "Alternate", "XOT", "F5");
    }

    @NotNull @Override public Position getStartPosition() {
        final String startPositionType = startPosition.getSelectedString();
        return StartPositionChooser.next(startPositionType);
    }

    @Override public void addChoicesToMenu(JMenu menu) {
        startPosition.addTo(menu);
    }
}
