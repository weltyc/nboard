package com.welty.nboard.thor;

import com.welty.othello.c.CBinaryReader;
import com.welty.othello.core.Utils;
import com.welty.othello.gdk.*;

import java.util.List;

/**
 * Memory representation of a thor game
 */
class ThorGameInternal extends ThorGame8 {
    final char year;
    final char openingCode;

    public ThorGameInternal(CBinaryReader is, char year) {
        super(is);
        this.year = year;
        openingCode = (char) ThorOpeningMap.OpeningCode(moves);
    }

    ThorGameInternal(int nBlackSquares, byte... moves) {
        super(nBlackSquares, moves);
        year = 0;
        openingCode = (char) ThorOpeningMap.OpeningCode(moves);
    }

    ThorGameInternal(int nBlackSquares, byte[] moves, int openingCode, int year) {
        super(nBlackSquares, moves);
        this.year = (char) year;
        this.openingCode = (char) openingCode;
    }

    public ThorGameInternal(int iTournament, int iBlackPlayer, int iWhitePlayer, int nBlackDiscs, int nPerfectDiscs, byte[] moves, int year, int openingCode) {
        super(iTournament, iBlackPlayer, iWhitePlayer, nBlackDiscs, nPerfectDiscs, moves);
        this.year = (char) year;
        this.openingCode = (char) openingCode;
    }

    public COsGame toOsGame(List<String> tournaments, List<String> players) {
        final COsGame game = new COsGame();
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);
        game.getBlackPlayer().name = players.get(iBlackPlayer);
        game.getWhitePlayer().name = players.get(iWhitePlayer);
        game.sPlace = tournaments.get(iTournament);
        game.SetResult(new OsResult(nBlackDiscs - (64 - nBlackDiscs)));
        game.SetTimeYear(year);
        for (byte move : moves) {
            final OsMove osMove = new OsMove(Utils.Row(move), Utils.Col(move));
            game.append(new OsMoveListItem(osMove));
        }
        return game;
    }
}
