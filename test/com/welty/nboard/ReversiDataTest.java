package com.welty.nboard;

import com.welty.othello.gdk.COsGame;
import com.welty.nboard.thor.ThorTest;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 21, 2009
 * Time: 2:57:36 PM
 * To change this template use File | Settings | File Templates.
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
        final OptionSource optionSource = EasyMock.createMock(OptionSource.class);
        final EngineTalker engineTalker = EasyMock.createMock(EngineTalker.class);
        final ReversiData data = new ReversiData(optionSource, engineTalker);
        final COsGame game = data.Game();
        game.SetDefaultStartPos();
        game.Update(ThorTest.mli("F5"));
        data.ReflectGame(iReflection);
        assertEquals(1, game.ml.size());
        assertEquals(expected, game.ml.get(0).mv.toString());
        data.ReflectGame(iReflection);
    }

    public void testUpdate() {
        final OptionSource optionSource = EasyMock.createNiceMock(OptionSource.class);
        final EngineTalker engineTalker = EasyMock.createMock(EngineTalker.class);

        final ReversiData data = new ReversiData(optionSource, engineTalker);

        data.Update(ThorTest.mli("F5"), true);
        data.Update(ThorTest.mli("D6"), true);

        // move matches game; game should not be broken
        data.SetIMove(0);
        data.Update(ThorTest.mli("F5"), true);
        assertEquals(2, data.NMoves());

        // move does not match game; game should be broken
        data.SetIMove(0);
        data.Update(ThorTest.mli("D3"), true);
        assertEquals(1, data.NMoves());
    }
}
