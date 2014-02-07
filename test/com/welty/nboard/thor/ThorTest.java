package com.welty.nboard.thor;

import com.welty.othello.core.CBitBoard;
import com.welty.othello.core.CMinimalReflection;
import com.welty.othello.core.CMove;
import com.welty.othello.core.CQPosition;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;
import gnu.trove.map.hash.TIntObjectHashMap;
import junit.framework.TestCase;

import java.util.ArrayList;

import static com.welty.othello.core.Utils.*;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 20, 2009
 * Time: 10:19:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class ThorTest extends TestCase {

    private static final byte STOP = (byte) -2;
    private static final ThorGameInternal passGame = getPassGame();

    /**
     * Set the game to be a game with a pass in it, and pos to be the position right before the pass
     */
    private static ThorGameInternal getPassGame() {
        // Marc Tastet vs Dorsimont Guilain, 2002, Result +64
        // white first passes on move 38.
        final byte passGameThorMoves[] = {56, 64, 53, 46, 35, 63, 34, 36, 65, 76, 47, 43, 75, 57, 66, 37, 68, 85, 86, 87, 74, 48, 26, 16, 67, 24, 28, 25, 84,
                58, 38, 83, 77, 27, 88, 73, 82, 13, 14, 78, 12, 15, 17, 18, 72, 62, 52, 51, 61, 71, 11, 33, 42, 41, 0};
        byte[] moves = new byte[60];
        for (int i = 0; i < passGameThorMoves.length; i++) {
            moves[i] = Thor.SquareFromThorMove(passGameThorMoves[i]);
        }
        return new ThorGameInternal(64, moves);
    }

    /**
     * This is a helper function for TestGameMatches()
     *
     * @return iReflection if any position in the gameMoves matches the position in posMatch, -1 if not
     *         <p/>
     *         iReflection is the index such that position.BitBoard().Symmetry(iReflection) is a position in the game described by gameMoves
     */
    private static int GameMatches(CQPosition position, final byte... gameMoves) {
        CBitBoard reflections[] = Thor.GetReflections(position.BitBoard());
        return Thor.GameMatches(gameMoves, reflections);
    }

    public static void testGameMatches() {
        CQPosition pos = new CQPosition();

        assertEquals(0, GameMatches(pos, STOP));

        assertEquals(0, GameMatches(pos, F5, STOP));

        pos.MakeMove(new CMove(F5));
        assertEquals(0, GameMatches(pos, F5, STOP));

        assertEquals(-1, GameMatches(pos, STOP));

        // reflection
        pos.Initialize();
        pos.MakeMove(new CMove(C4));
        assertEquals(3, GameMatches(pos, F5, STOP));

        // passes
        pos.Initialize();
        for (int i = 0; i < 37; i++) {
            pos.MakeMove(new CMove(passGame.moves[i]));
        }
        assertTrue(GameMatches(pos, passGame.moves) >= 0);
        assertEquals(pos.Mobility(true), 0);
        pos.Pass();
        assertTrue(GameMatches(pos, passGame.moves) >= 0);
        pos.MakeMove(new CMove(passGame.moves[37]));
        assertTrue(GameMatches(pos, passGame.moves) >= 0);

        // check we're ok if pos has fewer empties than the game end position
        pos.Initialize("****************************************************************", true);
        assertEquals(-1, GameMatches(pos, passGame.moves));

        // Illegal moves, I've found at least one thor game with illegal moves
        // (Dagnino Roberto vs Barnaba Donato, 2002)
        // although this is not that game
        pos.Initialize();
        assertTrue(GameMatches(pos, D4, STOP) >= 0);
        pos.MakeMove(new CMove(F5));
        assertEquals(-1, GameMatches(pos, D4, STOP));

    }

    public void testGameMatchesReflections() {
        // check game matching symmetry
        CQPosition pos = new CQPosition();
        pos.MakeMove(new CMove(F5));
        for (int i = 0; i < 8; i++) {
            CQPosition reflection = pos.Symmetry(i);
            int iReflection = GameMatches(reflection, F5, STOP);
            CMove reflectedMove = new CMove((byte) Thor.MoveFromIReflection(F5, iReflection));
            CQPosition posCheck = new CQPosition().Symmetry(i);
            posCheck.MakeMove(reflectedMove);
            assertEquals(reflectedMove + ": " + i, pos.BitBoard().MinimalReflection(), reflection.BitBoard().MinimalReflection());

        }
    }

    /**
     * @return the name of the last opening that this game passes through
     */
    private static String OpeningName(byte... moves) {
        return ThorOpeningMap.OpeningName(ThorOpeningMap.OpeningCode(moves));
    }


    public static void testOpeningCode() {
        int openingCode = ThorOpeningMap.OpeningCode(F5, STOP);
        assertEquals(0, openingCode);
        assertEquals("", OpeningName(F5, STOP));

        assertEquals("Perpendicular", OpeningName(F5, D6, STOP));
    }

    public static void testThorFindMatchingPositions() {
        ArrayList<ThorGameInternal> games = new ArrayList<ThorGameInternal>();
        COsGame match = new COsGame();
        match.SetDefaultStartPos();

        // no positions in thor game list
        Thor.MatchingPositions matchingPositions = Thor.ThorFindMatchingPositions(games, match.pos.board);
        assertEquals(0, matchingPositions.index.size());

        // add a game 1.F5 to the thor game array
        games.add(new ThorGameInternal(32, F5, STOP));

        matchingPositions = Thor.ThorFindMatchingPositions(games, match.pos.board);
        assertEquals(1, matchingPositions.index.size());
        assertEquals(1, matchingPositions.iReflections.size());
        assertEquals(0, matchingPositions.index.get(0));

        // Now update the match position to be the position after 1.F5
        match.Update(mli("F5"));

        matchingPositions = Thor.ThorFindMatchingPositions(games, match.pos.board);
        assertEquals(1, matchingPositions.index.size());
        assertEquals(1, matchingPositions.iReflections.size());
        assertEquals(0, matchingPositions.index.get(0));

        games.add(new ThorGameInternal(32, STOP));

        matchingPositions = Thor.ThorFindMatchingPositions(games, match.pos.board);
        assertEquals(1, matchingPositions.index.size());
        assertEquals(1, matchingPositions.iReflections.size());
        assertEquals(0, matchingPositions.index.get(0));
    }

    public static void testThorFindNextMoves() {

        for (int i = 0; i < 8; i++) {
            CQPosition f5 = new CQPosition();
            f5.MakeMove(new CMove(F5));

            CQPosition f5Reflection = new CQPosition(f5.BitBoard().Symmetry(i), f5.BlackMove());
            final int iReflection = GameMatches(f5Reflection, F5, D6, STOP);
            assertTrue(iReflection >= 0);
            int mv = Thor.MoveFromIReflection(D6, iReflection);

            f5Reflection.MakeMove(new CMove((byte) mv));
            f5.MakeMove(new CMove(D6));
            assertEquals("" + i, new CMinimalReflection(f5Reflection.BitBoard()), new CMinimalReflection(f5.BitBoard()));
        }
    }


    public static void testThorSummarize() {
        ArrayList<ThorGameInternal> games = new ArrayList<ThorGameInternal>();

        // no games, summary should be empty
        COsGame osGame = new COsGame();
        osGame.SetDefaultStartPos();
        Thor.MatchingPositions matchingPositions = Thor.ThorFindMatchingPositions(games, osGame.pos.board);
        TIntObjectHashMap<ThorSummaryData> summary = Thor.ThorSummarize(games, osGame.pos.board, matchingPositions.index, matchingPositions.iReflections);
        assertEquals(0, summary.size());

        // 1 game, summary should contain correct information
        games.add(new ThorGameInternal(64, F5, D6, C3, STOP));
        matchingPositions = Thor.ThorFindMatchingPositions(games, osGame.pos.board);
        summary = Thor.ThorSummarize(games, osGame.pos.board, matchingPositions.index, matchingPositions.iReflections);
        assertEquals(1, summary.size());
        checkMove(summary, F5, 1, 1, 0);


        // one white win, one black win, one draw.
        // position is after F5, all games should be there.
        osGame.Update(mli("F5"));
        games.add(new ThorGameInternal(32, F5, D6, C3, STOP));
        games.add(new ThorGameInternal(0, F5, D6, C3, STOP));
        matchingPositions = Thor.ThorFindMatchingPositions(games, osGame.pos.board);
        summary = Thor.ThorSummarize(games, osGame.pos.board, matchingPositions.index, matchingPositions.iReflections);
        assertEquals(1, summary.size());
        checkMove(summary, D6, 3, 1, 1);

        // two different moves
        // osGame still at the position after 1.F5
        games.add(new ThorGameInternal(64, F5, F6, STOP));
        matchingPositions = Thor.ThorFindMatchingPositions(games, osGame.pos.board);
        summary = Thor.ThorSummarize(games, osGame.pos.board, matchingPositions.index, matchingPositions.iReflections);
        assertEquals(2, summary.size());
        checkMove(summary, D6, 3, 1, 1);
        checkMove(summary, F6, 1, 1, 0);

        // No summary information for games which have terminated.
        osGame.Update(mli("D6"));
        osGame.Update(mli("C3"));
        matchingPositions = Thor.ThorFindMatchingPositions(games, osGame.pos.board);
        assertEquals(3, matchingPositions.index.size());
        summary = Thor.ThorSummarize(games, osGame.pos.board, matchingPositions.index, matchingPositions.iReflections);
        assertEquals(0, summary.size());

        // summary should contain a Pass when the mover must pass
        games.add(passGame);
        osGame.SetDefaultStartPos();
        for (int i = 0; i < 37; i++) {
            CMove mv = new CMove(passGame.moves[i]);
            osGame.Update(new OsMoveListItem(mv.toOsMove()));
        }
        matchingPositions = Thor.ThorFindMatchingPositions(games, osGame.pos.board);
        assertEquals(1, matchingPositions.index.size());
        summary = Thor.ThorSummarize(games, osGame.pos.board, matchingPositions.index, matchingPositions.iReflections);
        assertEquals(1, summary.size());
        assertNotNull(summary.get(-1));
    }

    public static OsMoveListItem mli(String moveText) {
        return new OsMoveListItem(new OsMove(moveText));
    }

    private static void checkMove(TIntObjectHashMap<ThorSummaryData> summary, byte move, int nPlayed, int nBlackWins, int nWhiteWins) {
        ThorSummaryData data;
        assertNotNull(summary.get(move));
        data = summary.get(move);
        assertEquals(nPlayed, data.nPlayed);
        assertEquals(nBlackWins, data.nBlackWins);
        assertEquals(nWhiteWins, data.nWhiteWins);
    }
}
