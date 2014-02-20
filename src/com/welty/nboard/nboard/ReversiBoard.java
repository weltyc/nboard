package com.welty.nboard.nboard;

import com.welty.nboard.gui.Align;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.gui.VAlign;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsBoard;
import com.welty.othello.gdk.COsPosition;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that displays the board (with pieces, evals, etc)
 */
class ReversiBoard extends BoardPanel {
    private final BoardSource boardSource;
    private final OptionSource optionSource;


    private final Hints hints;

    ReversiBoard(@NotNull ReversiData pd, @NotNull OptionSource optionSource, @NotNull Hints hints) {
        boardSource = pd;
        this.optionSource = optionSource;
        // add an event handler so when the hints change we draw them
        this.hints = hints;
        SignalListener repainter = new SignalListener() {
            public void handleSignal(Object data) {
                repaint();
            }
        };
        this.hints.m_seUpdate.Add(repainter);
        boardSource.addListener(repainter);
    }


    /**
     * Output a value in the lovely format used on screen
     * <p/>
     * normally this is just the value, but if |v|>=100
     * the extra 100 points is removed and the precision is capped at 1
     *
     * @return the stringBuilder that was sent in, having appended the value to it
     */
    StringBuilder outputValue(StringBuilder sb, float v, String precision) {
        if (Math.abs(v) >= 100.0) {
            if (v < 0)
                v += 100;
            else
                v -= 100;
        }
        sb.append(String.format("%" + precision + "f", v));
        return sb;
    }

    /**
     * @return the piece code to display on the screen based on location and legal-moves flag
     */
    private static char pieceToPaint(int ix, int iy, COsBoard board, char pcBackground) {
        char piece = board.getPiece(iy, ix);
        if (piece == COsBoard.EMPTY && pcBackground != 0)
            piece = pcBackground;
        return piece;
    }

    // Bitmaps
    private static final ImageIcon hbmBlack = NBoard.getImage("black.PNG");
    private static final ImageIcon hbmWhite = NBoard.getImage("white.PNG");
    private static final ImageIcon hbmEmpty = NBoard.getImage("empty.PNG");
    private static final ImageIcon hbmLegal = NBoard.getImage("legal.PNG");
    private static final ImageIcon hbmBadMove = NBoard.getImage("badmove.PNG");

    /**
     * @return highlight char if the given move should be highlighted, else return 0
     */
    char highlightChar(CMove mv, int iHighlight, final COsBoard board, OsMove mvNext) {
        switch (iHighlight) {
            case 0:
                return 0;
            case 1:
                return board.isMoveLegal(mv.toOsMove()) ? 'L' : 0;
            case 2:
                if (optionSource.ShowEvals()) {
                    final Hint hint = hints.get((byte) mv.Square());
                    if (hint == null)
                        return 0;
                    else if (hint.VNeutral() == hints.VBest())
                        return 'L';
                    else if (new CMove(mvNext).equals(mv))
                        return 'R';
                    else
                        return 0;
                } else
                    return 0;
            default:
                return 0;
        }
    }

    /**
     * Display the evaluation in the given square
     * <p/>
     * \post text background is set to transparent.
     */
    void paintEval(Graphics gd, CMove mv) {
        final Hint hint = hints.Map().get((byte) mv.Square());
        if (hint == null)
            return;

        Color foregroundColor;

        if (hint.fBook) {
            foregroundColor = Color.yellow;
        } else if (hint.depth.equals(hints.lastDepth)) {
            foregroundColor = Color.white;
        } else {
            foregroundColor = Color.lightGray;
        }

        Rectangle rectLast = rectFromSquare(mv.Col(), mv.Row(), n, boardArea);
        gd.setColor(foregroundColor);

        StringBuilder sb = new StringBuilder();
        String format;
        if (hint.isExact()) {
            format = ".0";
        } else if (hint.depth.isWldProven()) {
            format = ".1";
        } else {
            format = ".2";
        }

        // show a '+' for positive values, only if the values are under 100. If the values are 100 or more
        // then "+100.00" doesn't fit into the square on the board; just show "100.00" instead.
        if (hint.vWhite < 100 && hint.vBlack < 100) {
            format = "+" + format;
        }

        // eval
        if (optionSource.ViewD2()) {
            outputValue(sb, hint.vBlack, format);
            if (hint.vWhite != hint.vBlack) {
                sb.append("\n");
                outputValue(sb, hint.vWhite, format);
            }
        } else {
            outputValue(sb, hint.VNeutral(), format);
        }

        // number of games, or ply.
        if (hint.nGames != 0) {
            sb.append("\n").append(hint.nGames);
        }
        // if we're not in book, display ply (unless it's 0)
        // ply is 0 when displaying mli.dEval from the movelist, since there's no ply field in mli.
        else if (!hints.HasBookHint() && hint.depth.depth != 0 && !hint.isExact()) {
            sb.append("\n");
            if (hint.depth.isWldProven()) {
                float vn = hint.VNeutral();
                if (vn < 0) {
                    sb.append("Loss");
                } else {
                    sb.append("Win");
                }
            } else if (hint.depth.isProbableSolve()) {
                sb.append(hint.depth);
            } else {
                sb.append(hint.depth.depth).append(" ply");
            }
        }

        GraphicsUtils.drawString(gd, sb.toString(), rectLast, Align.CENTER, VAlign.MIDDLE);
    }

    @Override void onButtonDownInSquare(int col, int row) {
        if (optionSource.UsersMove() || boardSource.isReviewing()) {
            final COsPosition displayedPosition = boardSource.DisplayedPosition();
            final OsMove mv;
            if (displayedPosition.board.hasLegalMove()) {
                mv = new OsMove(row, col);
            } else {
                mv = OsMove.PASS;
            }

            if (displayedPosition.board.isMoveLegal(mv)) {
                // legal move, make the move and send to the engine.
                final double tElapsed = boardSource.secondsSinceLastMove();
                boardSource.update(new OsMoveListItem(mv, Double.NaN, tElapsed), true);
            }
        }
    }

    /**
     * The right button down undoes a move.
     *
     * @see ReversiData#Undo
     */
    @Override void onRightButtonDown() {
        boardSource.Undo();
    }

    public void paintComponent(Graphics gd) {
        final boolean paintCoordinates = optionSource.ViewCoordinates();
        COsPosition pos = boardSource.DisplayedPosition();
        final boolean paintPhotoStyle = optionSource.ViewPhotoStyle();

        paintBoard(gd, paintCoordinates, pos.board, !paintPhotoStyle);
    }

    /**
     * Paint the symbol that tells where the next move of the game was made
     * <p/>
     * If there is no next move, or if the next move is a pass, do nothing
     */
    void paintNextMove(Graphics gd, final OsMove mv) {
        if (!mv.isPass()) {
            boolean fBlackMove = boardSource.DisplayedPosition().board.fBlackMove;
            Rectangle rect = rectFromSquare(mv.col(), mv.row(), n, boardArea);
            rect = GraphicsUtils.FractionalInflate(rect, -.1);
            final Color pieceColor = fBlackMove ? Color.black : Color.white;

            gd.setColor(pieceColor);
            gd.drawOval(rect.x, rect.y, rect.width, rect.height);
            gd.setColor(Color.BLACK);
        }
    }


    /**
     * Paint the square. Background + piece only (piece/empty/legal move), no hints or "next move" info displayed
     * <p/>
     * This draws both the background and the piece itself.
     */
    void paintPiece(Graphics gd, int ix, int iy, COsBoard board, int iHighlight, OsMove mvNext) {
        final char pcBackground = highlightChar(new CMove(iy, ix), iHighlight, board, mvNext);
        char pc = pieceToPaint(ix, iy, board, pcBackground);

        Rectangle rect = rectFromSquare(ix, iy, n, boardArea);

        // old-style non-photo background
        if (!optionSource.ViewPhotoStyle()) {
            final Color bgColor;
            if (pc == 'L') {
                bgColor = NBoard.highlightColor;
            } else if (pc == 'R') {
                bgColor = Color.red;
            } else {
                bgColor = NBoard.boardColor;
            }
            NBoard.drawPiece(gd, pc, rect, bgColor);
        }

        // new style
        else {
            // draw pieces
            final ImageIcon imageIcon;
            switch (pc) {
                case COsBoard.EMPTY:
                    imageIcon = hbmEmpty;
                    break;
                case COsBoard.BLACK:
                    imageIcon = hbmBlack;
                    break;
                case COsBoard.WHITE:
                    imageIcon = hbmWhite;
                    break;
                case 'L':
                    imageIcon = hbmLegal;
                    break;
                case 'R':
                    imageIcon = hbmBadMove;
                    break;
                default:
                    throw new IllegalArgumentException("unknown char : " + pc);
            }
            gd.drawImage(imageIcon.getImage(), rect.x, rect.y, 50, 50, null);
        }
    }

    /**
     * Paint a square including background, next move symbol, and eval
     */
    @Override protected void paintSquare(Graphics gd, int ix, int iy, @NotNull COsBoard board) {
        final int iHighlight = optionSource.IHighlight();
        final OsMove mv = boardSource.NextMove();

        paintPiece(gd, ix, iy, board, iHighlight, mv);

        if (!mv.isPass() && mv.row() == iy && mv.col() == ix) {
            paintNextMove(gd, mv);
        }

        if (optionSource.ShowEvals())
            paintEval(gd, new CMove(iy, ix));
    }

}
