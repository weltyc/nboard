package com.welty.nboard.nboard;

import com.orbanova.common.misc.Require;
import com.welty.nboard.gui.SignalEvent;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.thor.Thor;
import com.welty.novello.core.Position;
import com.welty.othello.c.CReader;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.welty.othello.core.Utils.*;

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
     * Event data = COsMoveListItem* if the change is an update of the position by 1 move, or NULL otherwise
     * don't raise this directly, raise via BoardChanged()
     */
    private final SignalEvent<COsMoveListItem> m_seBoardChanged = new SignalEvent<>();


    /**
     * Initialize fields and create a new DatabaseData
     */
    ReversiData(@NotNull OptionSource optionSource, @NotNull EngineTalker engineTalker) {
        m_iMove = 0;
        this.optionSource = optionSource;
        this.engineTalker = engineTalker;
        StartNewGame(optionSource.getStartPosition());
    }

    public int NMoves() {
        return m_game.ml.size();
    }

    @NotNull public COsPosition DisplayedPosition() {
        return m_game.PosAtMove(m_iMove);
    }

    public void AddListener(SignalListener<COsMoveListItem> signalListener) {
        m_seBoardChanged.Add(signalListener);
    }

    public COsGame Game() {
        return m_game;
    }

    public int IMove() {
        return m_iMove;
    }

    public boolean Reviewing() {
        return m_iMove != m_game.ml.size();
    }

    private void BoardChanged() {
        BoardChanged(null);
    }

    private COsGame m_game = new COsGame();    // game data
    private int m_iMove;    // currently displayed move


    private static final int n = 8;

    public OsMove NextMove() {
        return Reviewing() ? m_game.ml.get(m_iMove).mv : OsMove.PASS;
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
        if (m_iMove < m_game.ml.size()) {
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
        SetIMove(m_game.ml.size());
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
        if (nUndo <= m_game.ml.size()) {
            m_game.Undo(nUndo);
            final int nMoves = m_game.ml.size();
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
        // reflect start pos
        OsBoard newStart = new OsBoard(m_game.posStart.board);
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n; row++) {
                int oldSquare = Square(row, col);
                int newSquare = Thor.MoveFromIReflection(oldSquare, iReflection);
                int newCol = Col(newSquare);
                int newRow = Row(newSquare);
                newStart.SetPiece(newRow, newCol, m_game.posStart.board.Piece(row, col));
            }
        }
        m_game.posStart.board.Set(newStart);

        for (int i = 0; i < m_game.ml.size(); i++) {
            COsMoveListItem mli = m_game.ml.get(i);
            CMove mv = new CMove(mli.mv);
            int sq = mv.Square();
            final OsMove rMv = new CMove((byte) Thor.MoveFromIReflection(sq, iReflection)).toOsMove();
            m_game.ml.set(i, new COsMoveListItem(rMv, mli.getEval(), mli.getElapsedTime()));
        }

        m_game.pos = m_game.PosAtMove(10000);

        BoardChanged();
    }


    /**
     * The displayed position changed. Update the displays. Clear the hints. Raise the event.
     */
    void BoardChanged(@Nullable COsMoveListItem data) {
        m_seBoardChanged.Raise(data);
    }

    public void SetIMove(int iMove) {
        Require.geq(iMove, "iMove", 0);
        final int nMoves = NMoves();
        if (iMove > nMoves) {
            iMove = nMoves;
        }
        if (iMove != m_iMove) {
            if (iMove == m_iMove + 1) {
                m_iMove = iMove;
                COsMoveListItem mli = new COsMoveListItem(m_game.ml.get(m_iMove - 1));
                BoardChanged(mli);
            } else {
                m_iMove = iMove;
                BoardChanged();
            }
        }
    }

    public void Update(final COsMoveListItem mli, boolean fUserMove) {
        final int nMoves = NMoves();
        final int iMove = IMove();

        if (iMove >= nMoves || !(mli.mv.equals(m_game.ml.get(iMove).mv))) {
            // if the user played a different move while reviewing, break the game
            // (eliminate subsequent moves which now make no sense)
            if (iMove < nMoves)
                m_game.Undo(nMoves - iMove);

            // update the player name
            m_game.pis[m_game.pos.board.fBlackMove ? 1 : 0].sName = fUserMove ? System.getProperty("user.name") : engineTalker.getEngineName();


            m_game.Update(mli);
            if (m_game.pos.board.GameOver()) {
                if (optionSource.EngineLearnAll()) {
                    engineTalker.TellEngineToLearn();
                }
            }
        } else {
            // do nothing, we will move forward down below
        }

        m_iMove++;
        COsMoveListItem mli2 = new COsMoveListItem(mli);
        BoardChanged(mli2);
    }

    /**
     * Update the game and windows when the game is being set based on the given string
     * <p/>
     * If the string does not contain a valid game, nothing happens.
     */
    void Update(final String sGame, boolean fResetMove) {
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
        Update(game, fResetMove);
        // todo if is doesn't contain a valid game, make sure nothing happens
    }

    /**
     * Update the game and windows when the game is set based on a game.
     *
     * @param fResetMove true if we should reset the position to move 0.
     *                   The user experience is best if this is true unless you can guarantee that the position on
     *                   the board will not change. (e.g. In Thor lookups, the position on the board shouldn't change).
     */
    public void Update(final COsGame game, boolean fResetMove) {
        m_game = game;
        if (fResetMove || IMove() >= game.ml.size())
            m_iMove = 0;
        BoardChanged();
        engineTalker.MayLearn();
    }

    /**
     * Clear the game and switch back to the standard start position
     */
    void StartNewGame(@NotNull Position startPosition) {
        m_game.Clear();
        m_game.Initialize("8");
        final String sBoardText = startPosition.boardString("");
        m_game.SetToPosition(sBoardText, startPosition.blackToMove);
        m_game.SetTime(System.currentTimeMillis());
        m_game.SetPlace("NBoard");
        m_iMove = 0;
        BoardChanged();
    }

    public void Redo() {
        if (m_iMove < m_game.ml.size()) {
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