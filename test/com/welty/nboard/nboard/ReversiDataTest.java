package com.welty.nboard.nboard;

import com.welty.nboard.thor.ThorTest;
import com.welty.novello.core.Position;
import com.welty.othello.gdk.COsGame;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.mockito.Mockito;

/**
 * Test accessing the ReversiGame
 */
public class ReversiDataTest extends TestCase {

    public void testReflectGame() {
        testReflectGame(0, "F5");
        testReflectGame(1, "F4");
        testReflectGame(2, "C5");
        testReflectGame(3, "C4");
        testReflectGame(4, "E6");
        testReflectGame(5, "E3");
        testReflectGame(6, "D6");
        testReflectGame(7, "D3");
    }

    private static void testReflectGame(int iReflection, String expected) {
        final OptionSource optionSource = mockOptionSource();
        final EngineTalker engineTalker = Mockito.mock(EngineTalker.class);
        final ReversiData data = new ReversiData(optionSource, engineTalker);
        final COsGame game = data.Game();
        game.SetDefaultStartPos();
        game.Update(ThorTest.mli("F5"));
        data.ReflectGame(iReflection);
        assertEquals(1, game.nMoves());
        assertEquals(expected, game.getMli(0).move.toString());
        data.ReflectGame(iReflection);
    }

    private static OptionSource mockOptionSource() {
        final OptionSource optionSource = Mockito.mock(OptionSource.class);
        Mockito.stub(optionSource.getStartPosition()).toReturn(Position.START_POSITION);
        return optionSource;
    }

    public void testUpdate() {
        final OptionSource optionSource = mockOptionSource();
        final EngineTalker engineTalker = EasyMock.createMock(EngineTalker.class);

        final ReversiData data = new ReversiData(optionSource, engineTalker);

        data.Update(ThorTest.mli("F5"), true);
        data.Update(ThorTest.mli("D6"), true);

        // move matches game; game should not be broken
        data.SetIMove(0);
        data.Update(ThorTest.mli("F5"), true);
        assertEquals(2, data.nMoves());

        // move does not match game; game should be broken
        data.SetIMove(0);
        data.Update(ThorTest.mli("D3"), true);
        assertEquals(1, data.nMoves());
    }
}
