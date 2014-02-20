package com.welty.nboard.nboard;

import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JSwingBuilder;
import com.welty.novello.core.Position;
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.COsBoard;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A window that allows the user to set up the initial board of a reversi game.
 */
public class SetUpWindow {
    private final JFrame window;
    private final Listener listener;
    private final JCheckBox moverSelector;

    SetUpWindow(final SetUpWindow.Listener listener) {
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

        final Grid<Component> buttonBar = JSwingBuilder.buttonBar(true, ok, cancel);

        final BoardSelectionPanel setUpPanel = new SetUpPanel(data, this);
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
        listener.setUpBoard(new Position(new String(data.pieces), moverSelector.isSelected()));
        window.setVisible(false);
    }

    /**
     * Display this window to the user.
     */
    void show() {
        window.setVisible(true);
    }

    interface Listener {
        /**
         * Notify the listener that the user would like to set up the board
         *
         * @param position board to set to
         */
        void setUpBoard(Position position);
    }
}

class SetUpData {
    final char[] pieces = new char[64];

    SetUpData() {
        final String text = Position.START_POSITION.boardString("");
        final char[] chars = text.toCharArray();
        System.arraycopy(chars, 0, pieces, 0, chars.length);
    }
}

class SetUpPanel extends BoardSelectionPanel {

    private final SetUpData data;

    public SetUpPanel(final SetUpData data, final SetUpWindow window) {
        this.data = data;
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        selectedRow = (selectedRow - 1) & 7;
                        selectionLocationChanged();
                        break;
                    case KeyEvent.VK_DOWN:
                        selectedRow = (selectedRow + 1) & 7;
                        selectionLocationChanged();
                        break;
                    case KeyEvent.VK_LEFT:
                        selectedCol = (selectedCol - 1) & 7;
                        selectionLocationChanged();
                        break;
                    case KeyEvent.VK_RIGHT:
                        selectedCol = (selectedCol + 1) & 7;
                        selectionLocationChanged();
                        break;
                    case KeyEvent.VK_DELETE:
                        data.pieces[selectedSquare()] = COsBoard.EMPTY;
                        repaint();
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        if (selectedRow > 0 || selectedCol > 0) {
                            selectedCol--;
                            if (selectedCol < 0) {
                                selectedCol = n - 1;
                                selectedRow--;
                            }
                            repaint();
                            data.pieces[selectedSquare()] = COsBoard.EMPTY;
                        }
                        break;

                    // need to handle ok/cancel here because we have focus
                    case KeyEvent.VK_ENTER:
                        window.ok(data);
                        break;
                    case KeyEvent.VK_ESCAPE:
                        window.cancel();
                        break;
                }
            }

            @Override public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'X':
                    case 'x':
                    case '*':
                        setPiece(COsBoard.BLACK);
                        break;
                    case 'O':
                    case 'o':
                    case '0':
                        setPiece(COsBoard.WHITE);
                        repaint();
                        break;
                    case ' ':
                    case '_':
                    case '-':
                        setPiece(COsBoard.EMPTY);
                        repaint();
                        break;
                }
            }
        });
    }

    private void setPiece(char color) {
        data.pieces[selectedSquare()] = color;
        incrementSquare();
    }

    protected void incrementSquare() {
        if (selectedCol < n - 1 || selectedRow < n - 1) {
            selectedCol++;
            if (selectedCol >= n) {
                selectedCol = 0;
                selectedRow++;
                if (selectedRow >= n) {
                    selectedRow = 0;
                }
            }
            selectionLocationChanged();
        }
    }

    @Override protected void paintSquare(Graphics g, int col, int row, @NotNull COsBoard board) {
        final char piece = board.getPiece(row, col);
        final Color bgColor = isSelected(col, row) ? highlightColor : boardColor;
        NBoard.paintSquare(g, col, row, piece, bgColor);
    }

    @NotNull @Override protected COsBoard getBoard() {
        return new COsBoard(new CReader("8 " + new String(data.pieces) + " *"));
    }
}