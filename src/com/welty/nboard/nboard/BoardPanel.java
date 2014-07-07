/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.nboard.nboard;

import com.welty.othello.gdk.COsBoard;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class BoardPanel extends JPanel {
    public static final int n = 8;
    protected static final int boardSize = 400;
    protected static final int boardFrameWidth = 15;
    private static final int boardFrameSize = boardSize + 2 * boardFrameWidth;
    @SuppressWarnings("SuspiciousNameCombination")
    protected static final Rectangle boardArea = new Rectangle(boardFrameWidth, boardFrameWidth, boardSize, boardSize);

    public BoardPanel() {
        setPreferredSize(new Dimension(boardFrameSize, boardFrameSize));

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                final Point point = e.getPoint();
                if (e.isMetaDown()) {
                    onRightButtonDown();
                } else {
                    onButtonDown(point);
                }
            }
        });
    }

    /**
     * @return the point at the left,top of the board square given by x,y
     */
    private static Point leftTop(int x, int y, int n, final Rectangle board) {
        return new Point(board.x + x * board.width / n, board.y + y * board.height / n);
    }

    /**
     * @return a rect containing the given square
     */
    protected static Rectangle rectFromSquare(int x, int y, int n, final Rectangle board) {
        final Point leftTop = leftTop(x, y, n, board);
        final Point bottomRight = leftTop(x + 1, y + 1, n, board);
        return new Rectangle(leftTop, new Dimension(bottomRight.x - leftTop.x, bottomRight.y - leftTop.y));
    }

    private static void paintBoardDot(Graphics gd, int x, int y) {
        final Rectangle rect = rectFromSquare(x, y, 8, boardArea);
        rect.x -= 2;
        rect.y -= 2;
        rect.height = 5;
        rect.width = 5;
        GraphicsUtils.fillRect(gd, rect);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    protected void paintBoard(Graphics gd, boolean paintCoordinates, @NotNull COsBoard board, boolean paintGridlines) {
        gd.setColor(new Color(0xFF, 0xCC, 0x88));
        final Dimension size = getSize();
        gd.fillRect(0, 0, size.width, size.height);

        // draw squares
        // hints in bold
        gd.setFont(gd.getFont().deriveFont(Font.BOLD));
        for (int ix = 0; ix < 8; ix++) {
            for (int iy = 0; iy < 8; iy++) {
                paintSquare(gd, ix, iy, board);
            }
        }

        // draw the gridlines
        gd.setColor(Color.BLACK);
        if (paintGridlines) {
            for (int i = 0; i <= 8; i++) {
                int nPixels = i * boardArea.width / 8;
                gd.drawLine(boardArea.x + nPixels, boardArea.y, boardArea.x + nPixels, boardArea.y + boardArea.height);
                gd.drawLine(boardArea.x, boardArea.y + nPixels, boardArea.y + boardArea.width, boardArea.y + nPixels);
            }
        }
        paintBoardDot(gd, 2, 2);
        paintBoardDot(gd, 2, 6);
        paintBoardDot(gd, 6, 2);
        paintBoardDot(gd, 6, 6);


        if (paintCoordinates) {
            // draw board coordinates
            gd.setColor(Color.black);
            // coordinates are not bold
            gd.setFont(gd.getFont().deriveFont(Font.PLAIN));

            for (int i = 0; i < 8; i++) {
                String col = "" + (char) (i + 'a');
                int left = i * boardArea.width / 8 + boardFrameWidth;
                int right = (i + 1) * boardArea.width / 8 + boardFrameWidth;
                GraphicsUtils.drawString(gd, col, new Rectangle(left, 0, right - left, boardFrameWidth));
                GraphicsUtils.drawString(gd, col, new Rectangle(left, boardFrameWidth + boardSize, right - left, boardFrameWidth));

                String row = "" + (char) (i + '1');
                GraphicsUtils.drawString(gd, row, new Rectangle(0, left, boardFrameWidth, right - left));
                GraphicsUtils.drawString(gd, row, new Rectangle(boardFrameWidth + boardSize, left, boardFrameWidth, right - left));
            }
        }
    }

    protected abstract void paintSquare(Graphics gd, int col, int row, @NotNull COsBoard board);

    /**
     * ButtonDown makes a move, send the move to the engine
     * <p/>
     * Can't move if it's the engine's move and we're not reviewing.
     */
    void onButtonDown(Point loc) {
        final int col = (loc.x - boardArea.x) * n / boardArea.width;
        final int row = (loc.y - boardArea.y) * n / boardArea.height;

        if (col >= 0 && col < n && row >= 0 && row < n) {
            onButtonDownInSquare(col, row);
        }
    }

    abstract void onButtonDownInSquare(int col, int row);

    abstract void onRightButtonDown();
}
