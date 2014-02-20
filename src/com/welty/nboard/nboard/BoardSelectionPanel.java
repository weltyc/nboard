package com.welty.nboard.nboard;

import com.welty.novello.core.BitBoardUtils;
import com.welty.othello.gdk.COsBoard;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public abstract class BoardSelectionPanel extends BoardPanel {
    public static final Color highlightColor = new Color(0x48, 0x60, 0xFF);
    public static final Color boardColor = new Color(0x38, 0x50, 0xA8);
    protected int selectedRow = 0;
    protected int selectedCol = 0;

    public BoardSelectionPanel() {
        setFocusable(true);
    }

    protected void selectionLocationChanged() {
        repaint();
    }

    protected boolean isSelected(int col, int row) {
        return row == selectedRow && col == selectedCol;
    }

    protected int selectedSquare() {
        return BitBoardUtils.square(selectedRow, selectedCol);
    }

    @Override protected void paintComponent(Graphics g) {
        paintBoard(g, true, getBoard(), true);
    }

    protected abstract @NotNull COsBoard getBoard();

    @Override protected void onButtonDownInSquare(int col, int row) {
        selectedRow = row;
        selectedCol = col;
        selectionLocationChanged();
    }

    @Override void onRightButtonDown() {
    }
}
