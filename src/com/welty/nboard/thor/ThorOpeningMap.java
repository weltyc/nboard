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

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.feed.Predicate;
import com.welty.othello.c.CReader;
import com.welty.othello.core.CBitBoard;
import com.welty.othello.core.CMove;
import com.welty.othello.core.CMoves;
import com.welty.othello.core.CQPosition;
import com.welty.othello.gdk.COsBoard;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Opening lookup table
 */
public class ThorOpeningMap {
    private final TObjectIntHashMap<CBitBoard> openingMap = new TObjectIntHashMap<>();
    private final ArrayList<String> names = new ArrayList<>();

    /**
     * Create the opening map from the "openings" array
     */
    private ThorOpeningMap() {
        final List<String> openingTexts = Feeds.ofLines(ThorOpeningMap.class.getResourceAsStream("openings.txt"))
                .filter(new Predicate<String>() {
                    @Override public boolean y(@NotNull String s) {
                        return !(s.startsWith("//"));
                    }
                })
                .asList();

        for (int i = 0; i < openingTexts.size(); i++) {
            String[] parts = openingTexts.get(i).split("\\s+", 2);
            String sMoves = parts[0];
            String name = parts.length > 1 ? parts[1].split(",")[0] : "";
            names.add(name);

            // calculate the position at the end of the move list
            CQPosition pos = new CQPosition();
            for (int moveLoc = 0; moveLoc < sMoves.length(); moveLoc += 2) {
                CMove move = new CMove(sMoves.substring(moveLoc, moveLoc + 2));
                pos.MakeMove(move);
            }
            for (int reflection = 0; reflection < 8; reflection++) {
                CBitBoard bb = pos.BitBoard().Symmetry(reflection);
                openingMap.put(bb, i);
            }
        }
    }


    private static final ThorOpeningMap tom = new ThorOpeningMap();

    /**
     * @return the opening name given the opening code
     */
    public static String OpeningName(int openingCode) {
        return tom.names.get(openingCode);
    }


    /**
     * Find the opening code for a thor game
     *
     * @param moves [in/out] In: ist of moves in Ntest square format. -2 denotes end of game. Out: Illegal moves are replaced by -2
     * @return 0 if the opening does not exist, otherwise the last named opening for the game
     */
    public static int OpeningCode(@NotNull byte... moves) {
        CQPosition pos = new CQPosition();
        return OpeningCode(pos, moves);
    }

    /**
     * Find the opening code for a thor game
     *
     * @param moves [in/out] In: ist of moves in Ntest square format. -2 denotes end of game. Out: Illegal moves are replaced by -2
     * @return 0 if the opening does not exist, otherwise the last named opening for the game
     */
    private static int OpeningCode(@NotNull CQPosition pos, @NotNull byte... moves) {
        int openingCode = 0;
        for (int i = 0; i < 60; i++) {
            int mv = moves[i];
            if (mv < 0)
                break;
            CMoves legalMoves = new CMoves();
            pos.CalcMovesAndPass(legalMoves);
            if (!Thor.GetBit(legalMoves.All(), mv)) {
                moves[i] = -2;
                break;
            }
            pos.MakeMove(new CMove((byte) mv));
            final CBitBoard bb = pos.BitBoard();
            if (tom.openingMap.contains(bb)) {
                openingCode = tom.openingMap.get(bb);
            }
        }
        return openingCode;
    }


    /**
     * @return the total number of named openings
     */
    static int NOpenings() {
        return tom.names.size();
    }

    /**
     * @return 0 if opening does not exist, otherwise the last named opening for the game.
     */
    public static int OpeningCodeFromGgf(String sGgfGame) {
        // Check to see if the game is standard-start. In the process check that a BO tag exists and is properly formed
        int bo = sGgfGame.indexOf("]BO[");
        if (bo == -1)
            throw new IllegalArgumentException("Corrupt GGF game");
        bo += 4;
        int boEnd = sGgfGame.indexOf(']', bo);
        if (boEnd == -1)
            throw new IllegalArgumentException("Corrupt GGF game");
        String s = sGgfGame.substring(bo, boEnd);
        final CReader boardIn = new CReader(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 66; i++) {
            sb.append(boardIn.readChar());
        }
        String boText = sb.toString();
        if (!(boText.equals("8---------------------------O*------*O---------------------------*"))) {
            // not 8x8 standard start, so either a rand game or not 8x8.
            return 0;
        }

        final byte[] moves = moveBytesFromGgf(sGgfGame);

        final COsBoard board = new COsBoard(new CReader(boText));
        return OpeningCode(new CQPosition(board), moves);
    }

    public static byte[] moveBytesFromGgf(String sGgfGame) {
        final byte[] moves = new byte[60];

        int loc = 0;
        for (int i = 0; i < 60; ) {
            loc = sGgfGame.indexOf(']', loc) + 1;
            if (loc + 4 >= sGgfGame.length()) {
                moves[i] = -2;
                break;
            }
            if ((sGgfGame.charAt(loc) == 'B' || sGgfGame.charAt(loc) == 'W') && sGgfGame.charAt(loc + 1) == '[') {
                CMove mv = new CMove(sGgfGame.substring(loc + 2, loc + 4));
                if (!mv.IsPass()) {
                    moves[i++] = (byte) mv.Square();
                }
            }
        }
        return moves;
    }
}
