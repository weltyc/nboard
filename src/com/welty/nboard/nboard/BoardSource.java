package com.welty.nboard.nboard;

import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMove;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gdk.COsPosition;
import com.welty.nboard.gui.SignalListener;
import org.jetbrains.annotations.NotNull;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 14, 2009
 * Time: 10:54:09 AM
 * </PRE>
 */
public interface BoardSource {
    /**
     * @return the position as it should be displayed currently
     */
    @NotNull COsPosition DisplayedPosition();

    /**
     * @return the move number of the current position
     */
    int IMove();

    void AddListener(SignalListener<COsMoveListItem> signalListener);

    /**
     * @return the number of moves played so far in the game.
     */
    int NMoves();

    /**
     * Change the displayed position of the game to the one before the given move. Raise the update method
     * <p/>
     * Raises a single-move update if the game moved forward by 1, otherwise a full-game update
     *
     * @param iMove move number, starting at 0. Move to the end of the game if iMove>NMoves().
     */
    void SetIMove(int iMove);

    /**
     * @return the current game
     */
    COsGame Game();

    /**
     * @return true if the user is reviewing the game, i.e. is not at the last move
     */
    boolean Reviewing();

    /**
     * Update the board with a move list item; update the engine; tell the engine what to do
     * todo engine talker should be a listener and tell the engine itself
     */
    void Update(COsMoveListItem mli, boolean fUserMove);

    /**
     * In study mode, move back one move and review. In game mode, remove moves from the game back to the last user move.
     * <p/>
     * In game mode:
     * - if the engine is moving , interrupt the engine and remove 1 move from the game
     * - if the user is moving, remove 2 moves from the game.
     * <p/>
     * If there are not enough moves to remove, does nothing.
     */
    void Undo();

    /**
     * @return the next move made, if the user is reviewing the game.
     *         return pass otherwise
     */
    COsMove NextMove();
}
