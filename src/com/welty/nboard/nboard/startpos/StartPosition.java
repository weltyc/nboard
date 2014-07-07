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

package com.welty.nboard.nboard.startpos;

import com.welty.novello.core.Board;
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.OsMove;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * A start position for a game.
 * <p/>
 * Includes an initial board setup (to support the alternate start position)
 * and a move list from that setup (to support XOT).
 */
@EqualsAndHashCode
@ToString
public class StartPosition {
    public final Board initialBoard;
    public final OsMove[] moves;

    public StartPosition(Board initialBoard, OsMove... moves) {
        this.initialBoard = initialBoard;
        this.moves = moves;
    }

    public StartPosition(Board initialBoard, String moveList) {
        this.initialBoard = initialBoard;
        List<OsMove> moves = new ArrayList<>();
        final CReader in = new CReader(moveList);
        while (!in.wsEof()) {
            OsMove mv = new OsMove(in);
            moves.add(mv);
        }
        this.moves = moves.toArray(new OsMove[moves.size()]);
    }
}
