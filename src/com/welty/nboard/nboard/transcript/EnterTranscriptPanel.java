package com.welty.nboard.nboard.transcript;

import com.welty.nboard.gui.Align;
import com.welty.nboard.gui.VAlign;
import com.welty.nboard.nboard.BoardSelectionPanel;
import com.welty.nboard.nboard.GraphicsUtils;
import com.welty.nboard.nboard.NBoard;
import com.welty.othello.gdk.COsBoard;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class EnterTranscriptPanel extends BoardSelectionPanel implements TranscriptData.Listener {
    private final TranscriptData data;
    private boolean hasFirstDigit = false;
    public static final Color errorColor = new Color(0x9F, 0x00, 0x00);

    /**
     * Construct a Panel that allows the user to edit transcript data.
     *
     * @param data            Transcript Data
     * @param chainedListener KeyListener to call when this Panel does not handle an event.
     */
    EnterTranscriptPanel(final TranscriptData data, final KeyListener chainedListener) {
        this.data = data;
        data.addListener(this);

        addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (selectedRow > 0) {
                            selectedRow--;
                            while (isStartSquare(selectedRow, selectedCol)) {
                                selectedRow--;
                            }
                            selectionLocationChanged();
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (selectedRow < 7) {
                            selectedRow++;
                            while (isStartSquare(selectedRow, selectedCol)) {
                                selectedRow++;
                            }
                            selectionLocationChanged();
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        decrementSquare();
                        break;
                    case KeyEvent.VK_RIGHT:
                        incrementSquare();
                        break;
                    case KeyEvent.VK_DELETE:
                        clearSquare(data);
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        if (hasFirstDigit) {
                            hasFirstDigit = false;
                            data.setMoveNumber(selectedSquare(), 0);
                            repaint();
                        } else {
                            clearSquare(data);
                        }
                        break;
                    default:
                        chainedListener.keyPressed(e);
                }
            }

            @Override public void keyTyped(KeyEvent e) {
                final char c = e.getKeyChar();
                final int sq = selectedSquare();
                if (c >= '0' && c <= '9') {
                    if (hasFirstDigit) {
                        incrementSquare();
                        data.shiftMoveNumber(sq, c - '0');
                        data.recalculate();
                    } else {
                        data.setMoveNumber(sq, c - '0');
                        hasFirstDigit = true;
                        repaint();
                    }
                } else if (c == ' ') {
                    if (hasFirstDigit) {
                        incrementSquare();
                    } else {
                        data.setMoveNumber(sq, 0);
                        hasFirstDigit = false;
                        data.recalculate();
                    }
                } else {
                    chainedListener.keyTyped(e);
                }
            }

            @Override public void keyReleased(KeyEvent e) {
                chainedListener.keyReleased(e);
            }
        });
    }

    private void clearSquare(TranscriptData data) {
        data.setMoveNumber(selectedSquare(), 0);
        hasFirstDigit = false;
        data.recalculate();
    }

    private void decrementSquare() {
        if (selectedCol > 0 || selectedRow > 0) {
            selectedCol--;
            while (isStartSquare(selectedRow, selectedCol)) {
                selectedCol--;
            }
            if (selectedCol < 0) {
                selectedCol = 7;
                selectedRow--;
            }
        }
        selectionLocationChanged();
    }

    void incrementSquare() {
        if (selectedCol < n - 1 || selectedRow < n - 1) {
            selectedCol++;
            while (isStartSquare(selectedRow, selectedCol)) {
                selectedCol++;
            }
            if (selectedCol >= n) {
                selectedCol = 0;
                selectedRow++;
                if (selectedRow >= n) {
                    selectedRow = 0;
                }
            }
            hasFirstDigit = false;
            selectionLocationChanged();
        }
    }

    private boolean isStartSquare(int row, int col) {
        return (row == 3 || row == 4) && (col == 3 || col == 4);
    }

    @Override protected void selectionLocationChanged() {
        hasFirstDigit = false;
        data.recalculate();
        super.selectionLocationChanged();
    }

    @Override protected void onButtonDownInSquare(int col, int row) {
        if (isStartSquare(row, col)) {
            return;
        }
        if (col != selectedCol || row != selectedRow) {
            hasFirstDigit = false;
            data.recalculate();
        }
        super.onButtonDownInSquare(col, row);
    }

    @Override protected @NotNull COsBoard getBoard() {
        return data.getGame().getPos().board;
    }

    @Override protected void paintSquare(Graphics g, int col, int row, @NotNull COsBoard board) {
        final char piece = board.getPiece(row, col);
        final int moveNumber = data.getMoveNumber(row, col);
        final int errorMoveNumber = data.getErrorMoveNumber();

        final Color bgColor;
        if (errorMoveNumber != 0 && moveNumber == errorMoveNumber) {
            bgColor = errorColor;
        } else if (isSelected(col, row)) {
            bgColor = highlightColor;
        } else {
            bgColor = boardColor;
        }
        NBoard.paintSquare(g, col, row, piece, bgColor);

        if (moveNumber > 0) {
            Rectangle rect = rectFromSquare(col, row, n, boardArea);
            g.setColor(piece == COsBoard.WHITE ? Color.BLACK : Color.WHITE);
            final String text = Integer.toString(moveNumber);
            GraphicsUtils.drawString(g, text, rect, Align.CENTER, VAlign.MIDDLE);
        }
    }

    @Override public void transcriptDataUpdated() {
        repaint();
    }
}
