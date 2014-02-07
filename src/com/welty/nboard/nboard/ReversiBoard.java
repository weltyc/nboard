package com.welty.nboard.nboard;

import com.welty.nboard.gui.Align;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.gui.VAlign;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsMove;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gdk.COsPosition;
import com.welty.othello.gdk.OsBoard;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Display the board (with pieces, evals, etc)
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 18, 2009
 * Time: 10:12:16 PM
 * To change this template use File | Settings | File Templates.
 */
class ReversiBoard extends JPanel {
    private final BoardSource m_pd;
    private final OptionSource optionSource;

    private static final int n = 8;

    private static final Color boardColor = new Color(0x38, 0x78, 0x30);
    private static final int boardSize = 400;
    private static final int boardFrameWidth = 15;
    private static final int boardFrameSize = boardSize + 2 * boardFrameWidth;

    @SuppressWarnings("SuspiciousNameCombination")
    private static final Rectangle boardArea = new Rectangle(boardFrameWidth, boardFrameWidth, boardSize, boardSize);

    private final Hints m_hints;

    ReversiBoard(@NotNull ReversiData pd, @NotNull OptionSource optionSource, @NotNull Hints hints) {
        m_pd = pd;
        this.optionSource = optionSource;
        // add an event handler so when the hints change we draw them
        m_hints = hints;
        SignalListener repainter = new SignalListener() {
            public void handleSignal(Object data) {
                repaint();
            }
        };
        m_hints.m_seUpdate.Add(repainter);
        m_pd.AddListener(repainter);

        setPreferredSize(new Dimension(boardFrameSize, boardFrameSize));
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                final Point point = e.getPoint();
                if (e.isMetaDown()) {
                    OnRightButtonDown();
                } else {
                    OnButtonDown(point);
                }
            }
        });
    }


    /**
     * @return the point at the left,top of the board square given by x,y
     */
    private static Point LeftTop(int x, int y, int n, final Rectangle board) {
        return new Point(board.x + x * board.width / n, board.y + y * board.height / n);
    }

    /**
     * @return a rect containing the given square
     */
    private static Rectangle SquareRect(int x, int y, int n, final Rectangle board) {
        final Point leftTop = LeftTop(x, y, n, board);
        final Point bottomRight = LeftTop(x + 1, y + 1, n, board);
        return new Rectangle(leftTop, new Dimension(bottomRight.x - leftTop.x, bottomRight.y - leftTop.y));
    }

    /**
     * Output a value in the lovely format used on screen
     * <p/>
     * normally this is just the value, but if |v|>=100
     * the extra 100 points is removed and the precision is capped at 1
     *
     * @return the stringBuilder that was sent in, having appended the value to it
     */
    StringBuilder ValueOut(StringBuilder sb, float v, String precision) {
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
    private static char PieceToPaint(int ix, int iy, COsPosition pos, char pcBackground) {
        char piece = pos.board.Piece(iy, ix);
        if (piece == OsBoard.EMPTY && pcBackground != 0)
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
    char Highlight(CMove mv, int iHighlight, final OsBoard board, COsMove mvNext) {
        switch (iHighlight) {
            case 0:
                return 0;
            case 1:
                return board.IsMoveLegal(mv.toOsMove()) ? 'L' : 0;
            case 2:
                if (optionSource.ShowEvals()) {
                    final Hint hint = m_hints.get((byte) mv.Square());
                    if (hint == null)
                        return 0;
                    else if (hint.VNeutral() == m_hints.VBest())
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
    void PaintEval(Graphics gd, CMove mv) {
        final Hint hint = m_hints.Map().get((byte) mv.Square());
        if (hint == null)
            return;

        Color foregroundColor;

        if (hint.fBook)
            foregroundColor = Color.yellow;
        else if (hint.sPly.equals(m_hints.m_sPlyRecent))
            foregroundColor = Color.white;
        else
            foregroundColor = Color.lightGray;

        Rectangle rectLast = SquareRect(mv.Col(), mv.Row(), n, boardArea);
        gd.setColor(foregroundColor);

        StringBuilder sb = new StringBuilder();
        String format;
        if (hint.IsExact())
            format = ".0";
        else if (hint.sPly.equals("100%W"))
            format = ".1";
        else
            format = ".2";

        // show a '+' for positive values, only if the values are under 100. If the values are 100 or more
        // then "+100.00" doesn't fit into the square on the board; just show "100.00" instead.
        if (hint.vWhite < 100 && hint.vBlack < 100)
            format = "+" + format;

        // eval
        if (optionSource.ViewD2()) {
            ValueOut(sb, hint.vBlack, format);
            if (hint.vWhite != hint.vBlack) {
                sb.append("\n");
                ValueOut(sb, hint.vWhite, format);
            }
        } else {
            ValueOut(sb, hint.VNeutral(), format);
        }

        // number of games, or ply.
        if (hint.nGames != 0) {
            sb.append("\n").append(hint.nGames);
        }
        // if we're not in book, display ply (unless it's 0)
        // ply is 0 when displaying mli.dEval from the movelist, since there's no ply field in mli.
        else if (!m_hints.HasBookHint() && hint.nPly != 0 && !hint.IsExact()) {
            sb.append("\n");
            if (hint.sPly.equals("100%W")) {
                float vn = hint.VNeutral();
                if (vn < 0)
                    sb.append("Loss");
                else
                    sb.append("Win");
            } else if (hint.sPly.indexOf('%') >= 0) {
                sb.append(hint.sPly);
            } else {
                sb.append(hint.sPly).append(" ply");
            }
        }

        GraphicsUtils.drawString(gd, sb.toString(), rectLast, Align.CENTER, VAlign.MIDDLE);
    }

    /**
     * ButtonDown makes a move, send the move to the engine
     * <p/>
     * Can't move if it's the engine's move and we're not reviewing.
     */
    void OnButtonDown(Point loc) {
        if (optionSource.UsersMove() || m_pd.Reviewing()) {
            final int ix = (loc.x - boardArea.x) * n / boardArea.width;
            final int iy = (loc.y - boardArea.y) * n / boardArea.height;

            if (ix >= 0 && ix < n && iy >= 0 && iy < n) {
                COsMove mv = new COsMove();
                COsPosition displayedPosition = m_pd.DisplayedPosition();
                if (displayedPosition.board.HasLegalMove())
                    mv.Set(iy, ix);
                else
                    mv.SetPass();

                if (displayedPosition.board.IsMoveLegal(mv)) {
                    // legal move, make the move and send to the engine.
                    m_pd.Update(new COsMoveListItem(mv), true);
                }
            }
        }
    }

    /**
     * The right button down undoes a move.
     *
     * @see ReversiData#Undo
     */
    void OnRightButtonDown() {
        m_pd.Undo();
    }

    public void paint(Graphics gd) {
        COsPosition pos = m_pd.DisplayedPosition();
        //gd.FillRect(GetClientRect(), Z::Color::red);

        // draw squares
        // hints in bold
        gd.setFont(gd.getFont().deriveFont(Font.BOLD));
        final int iHighlight = optionSource.IHighlight();
        for (int ix = 0; ix < 8; ix++) {
            for (int iy = 0; iy < 8; iy++) {
                PaintSquare(gd, ix, iy, pos, iHighlight);
            }
        }

        // draw the gridlines
        gd.setColor(Color.BLACK);
        if (!optionSource.ViewPhotoStyle()) {
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

//        // clear board coordinate area
//        gd.setColor(Color.white);
//        gd.drawRect(0, 0, boardFrameSize, boardFrameWidth);
//        gd.drawRect(0, 0, boardFrameWidth, boardFrameSize);
//        gd.drawRect(0, boardFrameSize - boardFrameWidth, boardFrameSize, boardFrameSize);
//        gd.drawRect(boardFrameSize - boardFrameWidth, 0, boardFrameSize, boardFrameSize);
//
        // draw board coordinates
        gd.setColor(Color.black);
        // coordinates are not bold
        gd.setFont(gd.getFont().deriveFont(Font.PLAIN));

        if (optionSource.ViewCoordinates()) {
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

    private static void paintBoardDot(Graphics gd, int x, int y) {
        final Rectangle rect = SquareRect(x, y, 8, boardArea);
        rect.x -= 2;
        rect.y -= 2;
        rect.height = 5;
        rect.width = 5;
        GraphicsUtils.fillRect(gd, rect);
    }

    /**
     * Paint the symbol that tells where the next move of the game was made
     * <p/>
     * If there is no next move, or if the next move is a pass, do nothing
     */
    void PaintNextMove(Graphics gd, final COsMove mv) {
        if (!mv.Pass()) {
            boolean fBlackMove = m_pd.DisplayedPosition().board.fBlackMove;
            Rectangle rect = SquareRect(mv.Col(), mv.Row(), n, boardArea);
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
    void PaintPiece(Graphics gd, int ix, int iy, COsPosition pos, int iHighlight, COsMove mvNext) {
        char pc = PieceToPaint(ix, iy, pos, Highlight(new CMove(iy, ix), iHighlight, pos.board, mvNext));

        Rectangle rect = SquareRect(ix, iy, n, boardArea);

        // old-style non-photo background
        if (!optionSource.ViewPhotoStyle()) {
            // draw background
            rect.x++;
            rect.y++;
            if (pc == 'L') {
                GraphicsUtils.fillRect(gd, rect, new Color(0x28, 0x98, 0x30));
            } else if (pc == 'R') {
                GraphicsUtils.fillRect(gd, rect, Color.red);
            } else {
                GraphicsUtils.fillRect(gd, rect, boardColor);
            }
            if (pc == OsBoard.BLACK || pc == OsBoard.WHITE) {
                rect = GraphicsUtils.FractionalInflate(rect, -.2);
                rect.x -= 1;
                rect.y -= 1;
                GraphicsUtils.fillEllipse(gd, rect, pc == OsBoard.BLACK ? Color.black : Color.white);
                GraphicsUtils.outlineEllipse(gd, rect, Color.BLACK);
            }
        }

        // new style
        else {
            // draw pieces
            final ImageIcon imageIcon;
            switch (pc) {
                case OsBoard.EMPTY:
                    imageIcon = hbmEmpty;
                    break;
                case OsBoard.BLACK:
                    imageIcon = hbmBlack;
                    break;
                case OsBoard.WHITE:
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
    void PaintSquare(Graphics gd, int ix, int iy, COsPosition pos, int iHighlight) {
        final COsMove mv = m_pd.NextMove();

        PaintPiece(gd, ix, iy, pos, iHighlight, mv);

        if (!mv.Pass() && mv.Row() == iy && mv.Col() == ix) {
            PaintNextMove(gd, mv);
        }

        if (optionSource.ShowEvals())
            PaintEval(gd, new CMove(iy, ix));
    }

}
