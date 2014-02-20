package com.welty.nboard.nboard.startpos;

import com.welty.novello.core.Position;
import com.welty.othello.gdk.OsMove;
import junit.framework.TestCase;

public class StartPositionChooserTest extends TestCase {
    public void testAlternate() {
        assertEquals(Position.ALTERNATE_START_POSITION, StartPositionChooser.next("Alternate").initialPosition);
    }

    public void testXot() throws Exception {
        final StartPosition xot1 = StartPositionChooser.next("XOT");
        assertEquals(52, endPosition(xot1).nEmpty());
        final StartPosition xot2 = StartPositionChooser.next("XOT");
        assertFalse(xot1.equals(xot2));
    }

    private Position endPosition(StartPosition sp) {
        Position p = sp.initialPosition;
        for (OsMove move : sp.moves) {
            p = p.play(move.toString());
        }
        return p;
    }
}
