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
