package com.welty.nboard.thor;

import com.orbanova.common.misc.Require;
import com.welty.othello.c.CBinaryReader;
import com.welty.othello.gdk.COsBoard;
import com.welty.othello.core.CBitBoard;
import com.welty.othello.core.CMove;
import com.welty.othello.core.CMoves;
import com.welty.othello.core.CQPosition;
import static com.welty.othello.core.Utils.*;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TObjectProcedure;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 20, 2009
 * Time: 9:40:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class Thor {


    /**
     * @return the Ntest move representation from Thor's move representation
     *         <p/>
     *         returns -2 if the tmv is not a valid move.
     */
    static byte SquareFromThorMove(byte tmv) {
        if (tmv != 0) {

            int col = tmv % 10;
            int row = (tmv - col) / 10;
            col--;
            row--;

            Require.eq(col & 7, "true col", col);
            Require.eq(row & 7, "true row", row);
            return (byte) Square(row, col);
        } else
            return -2;
    }

    //static class ThorString {
//	char name[nStringSize];
//};

//typedef ThorString<20> ThorPlayer;
//typedef ThorString<26> ThorTournament;


    /**
     * The thor progress window
     */
    private static ProgressWindow pwProgress = new ProgressWindow();

    /**
     * Hide the thor game loading progress window
     */
    static void ThorHideProgressWindow() {
        pwProgress.setVisible(false);
    }


    /**
     * Load a thor 8x8 games database
     *
     * @param fn     Filename
     * @param nSoFar number of games loaded so far (for progress window)
     * @return list of games from the file
     * @throws IllegalArgumentException if file doesn't exist
     */
    static ArrayList<ThorGameInternal> ThorLoadGames(final String fn, int nSoFar) {
        long t0 = System.currentTimeMillis();

        final CBinaryReader is = new CBinaryReader(fn);

        final ArrayList<ThorGameInternal> tgs = new ArrayList<ThorGameInternal>();
        // read header section
        final ThorHeader header = new ThorHeader(is);

        // check header for consistency
        if ((header.boardSize != 0 && header.boardSize != 8) ||
                header.n2 != 0 || header.fSolitaire || header.nPerfectPlay > 60
                || header.crDay > 31 || header.crMonth > 12)
            throw new IllegalArgumentException("This is not a thor games file : " + fn);

        // read games
        while (is.available() != 0) {
            ThorGameInternal tg = new ThorGameInternal(is, header.year);
            tgs.add(tg);
            int nGamesTotal = tgs.size() + nSoFar;

            // display the progress window if it's been more than 1 second
            if ((nGamesTotal & 1023) == 0 && System.currentTimeMillis() - t0 > 1000) {
                t0 = System.currentTimeMillis();
                pwProgress.SetText((nGamesTotal >> 10) + "k games loaded");
                pwProgress.setVisible(true);
                pwProgress.repaint();
            }
        }

        // check number of games vs header
        int nGames = tgs.size();
        if (nGames != header.n1) {
            throw new IllegalStateException("Wrong number of games in Thor games database " + fn);
        }

        return tgs;
    }

    /**
     * Load a thor players database or tournament database
     *
     * @param fn          filename
     * @param nStringSize 20 for players database, 26 for tournaments database
     * @return list of all players (or tournaments)
     * @throws IllegalArgumentException if there's an error reading the file
     */
    private static ArrayList<String> ThorLoadStrings(final String fn, int nStringSize) {
        final CBinaryReader is = new CBinaryReader(fn);

        ThorHeader header = new ThorHeader(is);

        // check header for consistency
        // board size should be 0 but Kostas's GGFToWthor converter sets it to 124, so ignore it.
        //if (header.boardSize!=0 || header.nPerfectPlay!=0 ||
        if (header.n1 != 0 || header.fSolitaire
                || header.crDay > 31 || header.crMonth > 12)
            throw new IllegalArgumentException("This is not a thor file : " + fn);

        // read players
        final ArrayList<String> ss = new ArrayList<String>();
        while (is.available() != 0) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nStringSize; i++) {
                sb.append((char) is.readByte());
            }
            ss.add(sb.toString());
        }

        // check number of games vs header
        int nss = ss.size();
        if (nss != header.n2)
            throw new IllegalArgumentException("Wrong number of strings in Thor database " + fn);


        return ss;
    }

    static ArrayList<String> ThorLoadPlayers(final String fn) {
        return ThorLoadStrings(fn, 20);
    }

    static ArrayList<String> ThorLoadTournaments(final String fn) {
        return ThorLoadStrings(fn, 26);
    }

    /**
     * Calc all reflections of the given bitboard
     *
     * @param bb Bitboard
     * @return all 8 reflections of the bitboard
     */
    static CBitBoard[] GetReflections(final CBitBoard bb) {
        CBitBoard[] reflections = new CBitBoard[8];
        for (int i = 0; i < 8; i++) {
            reflections[i] = bb.Symmetry(i);
        }
        return reflections;
    }

    /**
     * @return if a position matches, index of the matching reflection. Otherwise -1.
     * @param[in] bb Bitboard of the position
     * @param[in] reflections all 8 reflections of the position we may want to match
     */
    private static int MatchesReflections(final CBitBoard bb, final CBitBoard reflections[]) {
        for (int reflection = 0; reflection < 8; reflection++) {
            if (bb.equals(reflections[reflection])) {
                return reflection;
            }
        }
        return -1;
    }

    /**
     * @return true if the given empties matches the empties of any of the 8 reflections
     */
    private static boolean EmptiesMatch(final long empty, final CBitBoard reflections[]) {
        for (int reflection = 0; reflection < 8; reflection++) {
            if (empty == reflections[reflection].empty)
                return true;
        }
        return false;
    }

    /**
     * @return true if the bitboard block has the given square set
     */
    static boolean GetBit(long bbBlock, int square) {
        return ((bbBlock >> square) & 1) != 0;
    }

    /**
     * @param gameMoves   Game moves, in ntest movelist format
     * @param reflections all 8 reflections of the position we may want to match
     * @return iReflection if a position matches, index of the matching reflection. Otherwise -1.
     */
    static int GameMatches(final byte[] gameMoves, CBitBoard reflections[]) {
        CQPosition posGame = new CQPosition();
        final int nEmpty = reflections[0].NEmpty();

        // Quick-and-dirty check:
        // If the empty squares don't match, the position doesn't match.
        // It's very fast to calculate the empty squares.
        long empty;
        empty = posGame.BitBoard().empty;
        for (int i = 0; i < 60 - nEmpty; i++) {
            final int sq = gameMoves[i];
            if (sq < 0)
                return -1;
            else
                empty &= ~(1L << sq);
        }
        if (!EmptiesMatch(empty, reflections))
            return -1;

        // full check, that pos actually is equal
        for (int i = 0; i < 60 - nEmpty; i++) {
            CMoves moves = new CMoves();
            posGame.CalcMovesAndPass(moves);
            final int sq = gameMoves[i];
            if (sq < 0)
                return -1;

            // don't continue if the move is illegal
            if (!GetBit(moves.All(), sq))
                return -1;

            posGame.MakeMove(new CMove((byte) sq));
        }
        int iReflection = MatchesReflections(posGame.BitBoard(), reflections);
        if (iReflection < 0 && posGame.Mobility(true) == 0) {
            posGame.Pass();
            iReflection = MatchesReflections(posGame.BitBoard(), reflections);
        }
        return iReflection;
    }

    /**
     * Find games that have a position matching posMatch, and output the list of indices as index.
     *
     * @param games    vector of thor games to check
     * @param posMatch position to match
     * @return vector of indices of matching games, and vector of reflection indices that make the games match
     */
    static MatchingPositions ThorFindMatchingPositions(final ArrayList<ThorGameInternal> games, final COsBoard posMatch) {
        CBitBoard reflections[] = GetReflections(new CQPosition(posMatch).BitBoard());
        final MatchingPositions result = new MatchingPositions();

        for (int i = 0; i < games.size(); i++) {
            final int iReflection = GameMatches(games.get(i).moves, reflections);
            if (iReflection >= 0) {
                result.index.add(i);
                result.iReflections.add(iReflection);
            }
        }
        return result;
    }

    static class MatchingPositions {
        // index vector of indices of matching games
        final TIntArrayList index = new TIntArrayList();
        // iReflections vector of reflection indices that make the games match. Useful for ThorFindNextMoves().
        final TIntArrayList iReflections = new TIntArrayList();
    }

    /**
     * @param move        move from the reflected position
     * @param iReflection reflection index that takes the original position to the reflected position
     *                    <p/>
     *                    \note This function really INVERTS the reflection, to go from the reflected move to the original move
     *                    passes (and any move code<0) are returned unchanged
     * @return the move from the original position.
     */
    public static int MoveFromIReflection(int move, int iReflection) {
        if (move < 0)
            return move;

        if ((iReflection & 4) != 0)
            move = Square(Col(move), Row(move));
        if ((iReflection & 2) != 0)
            move ^= 7;
        if ((iReflection & 1) != 0)
            move ^= 070;
        return move;
    }

    /**
     * @return summary data for the various moves from a position
     */
    static TThorSummary ThorSummarize(final ArrayList<ThorGameInternal> games, final COsBoard pos, final TIntArrayList index, final TIntArrayList iReflections) {
        TThorSummary summary = new TThorSummary();

        final boolean fMustPass = !pos.HasLegalMove() && !pos.GameOver();

        final int iMove = 60 - pos.NEmpty();
        for (int i = 0; i < index.size(); i++) {
            final ThorGameInternal game = games.get(index.get(i));

            // find move for pos
            int mvReflected = game.moves[iMove];
            if (mvReflected != -2) {
                int mv = fMustPass ? -1 : MoveFromIReflection(mvReflected, iReflections.get(i));
                ThorSummaryData data = summary.get(mv);
                if (data == null) {
                    data = new ThorSummaryData();
                    summary.put(mv, data);
                }

                // game result: +1 for win, 0 for draw, -1 for loss
                if (game.nBlackDiscs != 32) {
                    if (game.nBlackDiscs > 32)
                        data.nBlackWins++;
                    else
                        data.nWhiteWins++;
                }
                data.nPlayed++;
            }
        }

        /// calc average score
        summary.forEachValue(new TObjectProcedure<ThorSummaryData>() {

            public boolean execute(ThorSummaryData tsd) {
                tsd.CalcScore(pos.fBlackMove);
                tsd.CalcFrequency(index.size());
                return true;
            }
        });

        return summary;
    }
}
