package com.welty.nboard.nboard;

import com.welty.novello.core.Position;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Manages everything related to choosing the start position for a game.
 * <p/>
 * All operations are expected to take place on the EDT.
 */
interface StartPositionManager {
    /**
     * Get the start position for a new game.
     *
     * @return A start position.
     */
    @NotNull Position getStartPosition();

    /**
     * Add required menu items so the start position can be selected
     *
     * @param menu menu to add to
     */
    void addChoicesToMenu(JMenu menu);
}
