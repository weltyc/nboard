package com.welty.nboard.nboard;

import com.orbanova.common.misc.Require;
import com.welty.nboard.gui.SignalEvent;
import com.welty.nboard.gui.SignalListener;
import com.welty.novello.core.Position;
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data displayed by ReversiWindow and its helper windows
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 17, 2009
 * Time: 2:34:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReversiData implements BoardSource {
    private final OptionSource optionSource;    // The app's main window, so we can tell if View menu items are checked
    private final EngineTalker engineTalker;

    //    SignalEvent m_seClearHints;            //*< Event that is fired when the hints are cleared
    //    SignalEvent m_seAddHint;            //*< Event that is fired when a new hint is added
    //
    /**
     * Event that is fired when the displayed board position changes
     * Event data = OsMoveListItem* if the change is an update of the position by 1 move, or NULL otherwise
     * don't raise this directly, raise via BoardChanged()
     */
    private final SignalEvent<OsMoveListItem> m_seBoardChanged = new SignalEvent<>();


    /**
     * Initialize fields and create a new DatabaseData
     */
    ReversiData(@NotNull OptionSource optionSource, @NotNull EngineTalker engineTalker) {
        m_iMove = 0;
        this.optionSource = optionSource;
        this.engineTalker = engineTalker;
        StartNewGame(optionSource.getStartPosition());
    }

    public int nMoves() {
        return game.nMoves();
    }

    @NotNull public COsPosition DisplayedPosition() {
        return game.PosAtMove(m_iMove);
    }

    public void AddListener(SignalListener<OsMoveListItem> signalListener) {
        m_seBoardChanged.Add(signalListener);
    }

    public COsGame Game() {
        return game;
    }

    public int IMove() {
        return m_iMove;
    }

    public boolean Reviewing() {
        return m_iMove != game.nMoves();
    }

    private void BoardChanged() {
        BoardChanged(null);
    }

    private COsGame game = new COsGame();    // game data
    private int m_iMove;    // currently displayed move


    private static final int n = 8;

    public OsMove NextMove() {
        return Reviewing() ? game.getMli(m_iMove).move : OsMove.PASS;
    }

    /**
     * Move back one move.
     * <p/>
     * On success this means the program enters review mode. The game is left unchanged.
     *
     * @return false on failure (we were already at the beginning of the game) and true otherwise.
     */
    boolean Back() {
        if (m_iMove > 0) {
            SetIMove(m_iMove - 1);
            m_seBoardChanged.Raise();
            return true;
        } else
            return false;
    }

    /**
     * Move forward one move, unless at the end of the game
     * <p/>
     * If the new position is at the end of the game the program will leave review mode. The game is left unchanged.
     * <p/>
     * Calls Update(mli version) so that the engine gets a single-move update instead of a full-game update
     *
     * @return false on failure (we were already at the end of the game) and true otherwise.
     */
    boolean Fore() {
        if (m_iMove < game.nMoves()) {
            SetIMove(m_iMove + 1);
            return true;
        } else
            return false;
    }

    /**
     * Move to the beginning of the game
     */
    void First() {
        SetIMove(0);
    }

    /**
     * Move to the end of the game
     */
    void Last() {
        SetIMove(game.nMoves());
    }

    /**
     * Undo the given number of moves.
     * <p/>
     * This does not enter review mode. The moves are physically removed from the game.
     * If nUndo is greater than the number of moves in the game, the game is not changed and the function returns false.
     *
     * @param nUndo number of moves to remove from the game.
     * @return false on failure
     */
    boolean Undo(int nUndo) {
        if (nUndo <= game.nMoves()) {
            game.Undo(nUndo);
            final int nMoves = game.nMoves();
            if (m_iMove > nMoves)
                SetIMove(nMoves);
            m_seBoardChanged.Raise();
            return true;
        }
        return false;
    }

    public void Undo() {
        if (optionSource.IsStudying()) {
            Back();
        } else {
            int nUndo = optionSource.UserPlays(!Game().pos.board.fBlackMove) ? 1 : 2;
            Undo(nUndo);
        }
    }

    /**
     * Reflect the game (start pos, moves, current pos) using the given reflection
     */
    void ReflectGame(int iReflection) {
        game.reflect(iReflection);
        BoardChanged();
    }


    /**
     * The displayed position changed. Update the displays. Clear the hints. Raise the event.
     */
    void BoardChanged(@Nullable OsMoveListItem data) {
        m_seBoardChanged.Raise(data);
    }

    public void SetIMove(int iMove) {
        Require.geq(iMove, "iMove", 0);
        final int nMoves = nMoves();
        if (iMove > nMoves) {
            iMove = nMoves;
        }
        if (iMove != m_iMove) {
            if (iMove == m_iMove + 1) {
                m_iMove = iMove;
                OsMoveListItem mli = game.getMli(m_iMove - 1);
                BoardChanged(mli);
            } else {
                m_iMove = iMove;
                BoardChanged();
            }
        }
    }

    public void Update(final OsMoveListItem mli, boolean fUserMove) {
        final int nMoves = nMoves();
        final int iMove = IMove();

        if (iMove >= nMoves || !(mli.move.equals(game.getMli(iMove).move))) {
            // if the user played a different move while reviewing, break the game
            // (eliminate subsequent moves which now make no sense)
            if (iMove < nMoves)
                game.Undo(nMoves - iMove);

            // update the player name
            game.pis[game.pos.board.fBlackMove ? 1 : 0].sName = fUserMove ? System.getProperty("user.name") : engineTalker.getEngineName();


            game.append(mli);
            if (game.pos.board.isGameOver()) {
                if (optionSource.EngineLearnAll()) {
                    engineTalker.TellEngineToLearn();
                }
            }
        } else {
            // do nothing, we will move forward down below
        }

        m_iMove++;
        BoardChanged(mli);
    }

    /**
     * Update the game and windows when the game is being set based on the given string
     * <p/>
     * If the string does not contain a valid game, nothing happens.
     */
    void setGameText(final String sGame, boolean fResetMove) {
        CReader is = new CReader(sGame);
        Update(is, fResetMove);
    }

    /**
     * Update the game and windows by reading the game from the istream.
     * <p/>
     * If the istream does not contain a valid game, nothing happens
     */
    void Update(CReader is, boolean fResetMove) {
        COsGame game = new COsGame();
        game.In(is);
        setGame(game, fResetMove);
        // todo if is doesn't contain a valid game, make sure nothing happens
    }

    /**
     * Update the game and windows when the game is set based on a game.
     *
     * @param fResetMove true if we should reset the position to move 0.
     *                   The user experience is best if this is true unless you can guarantee that the position on
     *                   the board will not change. (e.g. In Thor lookups, the position on the board shouldn't change).
     */
    public void setGame(final COsGame game, boolean fResetMove) {
        this.game = game;
        if (fResetMove || IMove() >= game.nMoves())
            m_iMove = 0;
        BoardChanged();
        engineTalker.MayLearn();
    }

    /**
     * Clear the game and switch back to the standard start position
     */
    void StartNewGame(@NotNull Position startPosition) {
        game.Clear();
        game.Initialize("8");
        final String sBoardText = startPosition.boardString("");
        game.SetToPosition(sBoardText, startPosition.blackToMove);
        game.SetTime(System.currentTimeMillis());
        game.SetPlace("NBoard");
        m_iMove = 0;
        BoardChanged();
    }

    public void Redo() {
        if (m_iMove < game.nMoves()) {
            SetIMove(m_iMove + 1);
        }
    }

    public void SetNames(String name0, String name1, boolean updateUsers) {
        Game().pis[0].sName = name0;
        Game().pis[1].sName = name1;
        if (updateUsers) {
            m_seBoardChanged.Raise();
        }
    }
}