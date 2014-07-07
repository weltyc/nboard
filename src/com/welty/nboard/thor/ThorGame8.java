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

package com.welty.nboard.thor;

import com.welty.othello.c.CBinaryReader;

import java.util.Arrays;

/**
 * Information about a single game, played on an 8x8 board.
 */
class ThorGame8 {
    final char iTournament;    //*< Tournament id from tournament DB.
    final char iBlackPlayer;    //*< Black player id from players db
    final char iWhitePlayer;    //*< White player id from players db
    final byte nBlackDiscs;    //*< Actual score, number of black discs at end of game
    private final byte nPerfectDiscs; //*< Score with perfect play in the endgame from nPerfectPlay empties.
    final byte moves[];        //*< column (1-8) + 10* row (1-8), so a1=11, h1=18, a8=81, h8=88

    ThorGame8(CBinaryReader is) {
        iTournament = is.readChar();
        iBlackPlayer = is.readChar();
        iWhitePlayer = is.readChar();
        nBlackDiscs = is.readByte();
        nPerfectDiscs = is.readByte();
        final byte[] moves = new byte[60];
        for (int i = 0; i < moves.length; i++) {
            // change Thor square numbers to Ntest square numbers.
            // this is done now to increase the speed of the lookups.
            moves[i] = Thor.SquareFromThorMove(is.readByte());
        }
        this.moves = moves;
    }

    public ThorGame8(byte[] moves) {
        this(0, 0, 0, 0, 0, moves);
    }

    ThorGame8(int nBlackDiscs, byte[] moves) {
        this((char) 0, (char) 0, (char) 0, (byte) nBlackDiscs, 0, moves);
    }

    ThorGame8(int iTournament, int iBlackPlayer, int iWhitePlayer, int nBlackDiscs, int nPerfectDiscs, byte[] moves) {
        this.iTournament = (char) iTournament;
        this.iBlackPlayer = (char) iBlackPlayer;
        this.iWhitePlayer = (char) iWhitePlayer;
        this.nBlackDiscs = (byte) nBlackDiscs;
        this.nPerfectDiscs = (byte) nPerfectDiscs;
        this.moves = Arrays.copyOf(moves, moves.length);
    }
}
