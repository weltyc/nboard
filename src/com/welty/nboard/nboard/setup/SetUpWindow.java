package com.welty.nboard.nboard.setup;

import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JSwingBuilder;
import com.welty.nboard.nboard.BoardSelectionPanel;
import com.welty.novello.core.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A window that allows the user to set up the initial board of a reversi game.
 */
public class SetUpWindow {
    private final JFrame window;
    private final Listener listener;
    private final JCheckBox moverSelector;
    private final BoardSelectionPanel setUpPanel;

    public SetUpWindow(final SetUpWindow.Listener listener) {
        this.listener = listener;
        final SetUpData data = new SetUpData();
        final JLabel instructions = new JLabel("Arrow keys to move, 'X', 'O', or <space> to change the piece");
        final JButton ok = JSwingBuilder.button(new AbstractAction("OK") {
            @Override public void actionPerformed(ActionEvent e) {
                ok(data);
            }
        });
        final JButton cancel = JSwingBuilder.button(new AbstractAction("Cancel") {
            @Override public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        moverSelector = new JCheckBox("Black to move");
        moverSelector.setSelected(true);
        moverSelector.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                setUpPanel.requestFocusInWindow();
            }
        });

        final Grid<Component> buttonBar = JSwingBuilder.buttonBar(true, ok, cancel);

        setUpPanel = new SetUpPanel(data, this);
        window = JSwingBuilder.frame("Set Up Board", WindowConstants.HIDE_ON_CLOSE, false,
                JSwingBuilder.vBox(
                        instructions,
                        setUpPanel,
                        moverSelector,
                        buttonBar
                ));
        setUpPanel.requestFocusInWindow();
    }

    void cancel() {
        window.setVisible(false);
    }

    void ok(SetUpData data) {
        listener.setUpBoard(new Board(new String(data.pieces), moverSelector.isSelected()));
        window.setVisible(false);
    }

    /**
     * Display this window to the user.
     */
    public void show() {
        window.setVisible(true);
        setUpPanel.requestFocusInWindow();
    }

    public interface Listener {
        /**
         * Notify the listener that the user would like to set up the board
         *
         * @param board board to set to
         */
        void setUpBoard(Board board);
    }
}

