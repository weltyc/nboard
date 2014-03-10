package com.welty.nboard.nboard;

import com.orbanova.common.clock.Clock;
import com.orbanova.common.clock.SystemClock;
import com.orbanova.common.misc.Require;
import com.welty.nboard.gui.SignalEvent;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.nboard.startpos.StartPosition;
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
    private final @NotNull Clock clock;

    //    SignalEvent m_seClearHints;            //*< Event that is fired when the hints are cleared
    //    SignalEvent m_seAddHint;            //*< Event that is fired when a new hint is added
    //
    /**
     * Event that is fired when the displayed board position changes
     * Event data = OsMoveListItem* if the change is an update of the position by 1 move, or NULL otherwise
     * don't raise this directly, raise via fireBoardChanged()
     */
    private final SignalEvent<OsMoveListItem> m_seBoardChanged = new SignalEvent<>();
    private @NotNull OsClock gameStartClock = new OsClock(2 * 60);

    /**
     * Time of the last move, or if no moves have been made, time since the game started.
     */
    private long lastMoveMillis;

    /**
     * Initialize fields and create a new DatabaseTableModel
     */
    ReversiData(@NotNull OptionSource optionSource, @NotNull EngineTalker engineTalker) {
        this(optionSource, engineTalker, new SystemClock());
    }

    ReversiData(@NotNull OptionSource optionSource, @NotNull EngineTalker engineTalker, @NotNull Clock clock) {
        this.clock = clock;
        m_iMove = 0;
        this.optionSource = optionSource;
        this.engineTalker = engineTalker;
        StartNewGame(optionSource.getStartPosition());
        this.lastMoveMillis = clock.getMillis();
    }

    public int nMoves() {
        return game.nMoves();
    }

    @NotNull public COsPosition DisplayedPosition() {
        return game.PosAtMove(m_iMove);
    }

    public void addListener(SignalListener<OsMoveListItem> signalListener) {
        m_seBoardChanged.Add(signalListener);
    }

    public COsGame getGame() {
        return game;
    }

    public int IMove() {
        return m_iMove;
    }

    public boolean isReviewing() {
        return m_iMove != game.nMoves();
    }

    /**
     * The board changed in some way that can't be represented by a MoveListItem
     */
    private void fireBoardChanged() {
        fireBoardChanged(null);
    }

    private COsGame game = new COsGame();    // game data
    private int m_iMove;    // currently displayed move

    public OsMove NextMove() {
        return isReviewing() ? game.getMli(m_iMove).move : OsMove.PASS;
    }

    @Override public double secondsSinceLastMove() {
        return 0.001 * (clock.getMillis() - lastMoveMillis);
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
            int nUndo = optionSource.UserPlays(!getGame().pos.board.fBlackMove) ? 1 : 2;
            Undo(nUndo);
        }
    }

    /**
     * Reflect the game (start pos, moves, current pos) using the given reflection
     */
    void ReflectGame(int iReflection) {
        game.reflect(iReflection);
        fireBoardChanged();
    }


    /**
     * The displayed position changed. Update the displays. Clear the hints. Raise the event.
     */
    void fireBoardChanged(@Nullable OsMoveListItem data) {
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
                fireBoardChanged(mli);
            } else {
                m_iMove = iMove;
                fireBoardChanged();
            }
        }
    }

    public void update(final OsMoveListItem mli, boolean fUserMove) {
        final int nMoves = nMoves();
        final int iMove = IMove();

        //noinspection StatementWithEmptyBody
        if (iMove >= nMoves || !(mli.move.equals(game.getMli(iMove).move))) {
            // if the user played a different move while reviewing, break the game
            // (eliminate subsequent moves which now make no sense)
            if (iMove < nMoves)
                game.Undo(nMoves - iMove);

            // update the player name
            final String name = fUserMove ? System.getProperty("user.name") : engineTalker.getEngineName();
            game.setPlayerName(game.pos.board.fBlackMove, name);


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
        resetTimer();
        fireBoardChanged(mli);
    }

    private void resetTimer() {
        lastMoveMillis = clock.getMillis();
    }

    /**
     * Update the game and windows when the game is being set based on the given string
     * <p/>
     * If the sGame does not contain a valid game, this data is not modified and the method throws an exception.
     *
     * @param sGame GGF format text of the game.
     * @throws IllegalArgumentException if this can't be parsed as a game.
     */
    void setGameText(final String sGame) {
        CReader is = new CReader(sGame);
        setGame(is, true);
    }

    /**
     * Update the game and windows by reading the game from the istream.
     * <p/>
     * If the istream does not contain a valid game, this data is not modified and the method throws an exception.
     *
     * @param fResetMove true if we should reset the position to move 0.
     *                   The user experience is best if this is true unless you can guarantee that the position on
     *                   the board will not change. (e.g. In Thor lookups, the position on the board shouldn't change).
     * @throws IllegalArgumentException if this can't be parsed as a game.
     */
    void setGame(CReader is, boolean fResetMove) {
        COsGame game = new COsGame(is);
        setGame(game, fResetMove);
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
        if (fResetMove || IMove() >= game.nMoves()) {
            m_iMove = 0;
        }
        resetTimer();
        fireBoardChanged();
        engineTalker.MayLearn();
    }

    /**
     * Clear the game and switch to a new game's start position
     */
    void StartNewGame(@NotNull StartPosition startPosition) {
        game.Clear();
        game.Initialize("8", getGameStartClock(), getGameStartClock());
        final String sBoardText = startPosition.initialPosition.boardString("");
        game.SetToPosition(sBoardText, startPosition.initialPosition.blackToMove);
        for (OsMove move : startPosition.moves) {
            game.append(new OsMoveListItem(move));
        }
        game.SetTime(System.currentTimeMillis());
        game.SetPlace("NBoard");
        m_iMove = startPosition.moves.length;

        resetTimer();
        fireBoardChanged();
    }

    public void Redo() {
        if (m_iMove < game.nMoves()) {
            SetIMove(m_iMove + 1);
        }
    }

    public void SetNames(String whiteName, String blackName, boolean updateUsers) {
        getGame().setPlayerName(true, blackName);
        getGame().setPlayerName(false, whiteName);
        if (updateUsers) {
            m_seBoardChanged.Raise();
        }
    }

    public @NotNull OsClock getGameStartClock() {
        return gameStartClock;
    }

    /**
     * Handles a Paste command.
     * <p/>
     * This tries to interpret s as a move list (from the current start position),
     * as a board (filled squares and player-to-move only), or as a GGF game. If any of these
     * succeed, it updates the current game. If none succeed, it throws an IllegalArgumentException
     * containing a helpful error message.
     *
     * @param s text to paste
     * @throws IllegalArgumentException if s can't be interpreted.
     */
    public void paste(String s) {
        if (s.startsWith("(;GM[Othello]")) {
            try {
                setGameText(s);
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        } else {
            String compressed = s.replaceAll("[ \t\r\n]", "");
            if (looksLikeMoveList(compressed)) {
                // looks like a move list
                COsGame game = new COsGame();
                game.setToDefaultStartPosition(getGameStartClock(), getGameStartClock());
                try {
                    game.SetMoveList(s);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid move list: " + e.getMessage(), e);
                }
                setGame(game, true);
            } else if (looksLikeBoard(compressed)) {
                // try it as a board
                COsGame game = new COsGame();
                game.Initialize("8", getGameStartClock(), getGameStartClock());
                game.getStartPosition().board.in(new CReader("8 " + s));
                game.CalcCurrentPos();
                setGame(game, true);
            } else {
                throw new IllegalArgumentException("Can't interpret as a move list, board, or game: \"" + s + "\"");
            }
        }
    }

    static boolean looksLikeBoard(String compressed) {
        return !compressed.isEmpty() && "OX*0-._".contains(compressed.substring(0, 1));
    }

    private static boolean looksLikeMoveList(String compressed) {
        if (compressed.length() < 2) {
            return false;
        }
        compressed = compressed.toUpperCase();
        final char c = compressed.charAt(0);
        if (c >= 'A' && c <= 'H') {
            final char c1 = compressed.charAt(1);
            return c1 <= '8' && c1 >= '1';
        } else return compressed.startsWith("PA");

    }
}