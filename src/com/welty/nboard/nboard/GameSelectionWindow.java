package com.welty.nboard.nboard;

import javax.swing.*;
import java.io.File;

/**
 * Window that displays a bunch of games for the user to choose from.
 * When the user chooses one it is displayed in the main Reversi Window.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 23, 2009
 * Time: 9:44:22 PM
 * To change this template use File | Settings | File Templates.
 */
class GameSelectionWindow extends JFrame {

    private final GameSelectionGrid m_pgsg;

    GameSelectionWindow(ReversiWindow pwTarget) {
        super("Game selection window");
        m_pgsg = new GameSelectionGrid(pwTarget);
        add(m_pgsg);
        pack();
    }

    /**
     * Load all games from a file and display them in the grid
     */
    void LoadAndShow(final File file) {
        m_pgsg.Load(file);
        setTitle(file.getName() + " (" + m_pgsg.getTableModel().getRowCount() + " games)");
        // the unusual pack/repaint/pack combo seems to get it right even if we change from no scrollbars to scrollbars.
        pack();
        repaint();
        pack();
        setVisible(true);
    }
}
