package com.welty.nboard.nboard.setup;

import com.welty.nboard.nboard.BoardSelectionPanel;
import com.welty.nboard.nboard.NBoard;
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.COsBoard;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
