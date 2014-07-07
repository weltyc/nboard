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
import com.welty.othello.gdk.OsMove;
import junit.framework.TestCase;

public class StartPositionChooserTest extends TestCase {
    public void testAlternate() {
        assertEquals(Board.ALTERNATE_START_BOARD, StartPositionChooser.next("Alternate").initialBoard);
    }

    public void testXot() throws Exception {
        final StartPosition xot1 = StartPositionChooser.next("XOT");
        assertEquals(52, endPosition(xot1).nEmpty());
        final StartPosition xot2 = StartPositionChooser.next("XOT");
        assertFalse(xot1.equals(xot2));
    }

    private Board endPosition(StartPosition sp) {
        Board p = sp.initialBoard;
        for (OsMove move : sp.moves) {
            p = p.play(move.toString());
        }
        return p;
    }
}
