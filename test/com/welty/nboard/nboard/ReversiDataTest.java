package com.welty.nboard.nboard;

import com.orbanova.common.clock.MockClock;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.nboard.startpos.StartPosition;
import com.welty.nboard.thor.ThorTest;
import com.welty.novello.core.Position;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

/**
 * Test accessing the ReversiGame
 */
public class ReversiDataTest extends TestCase {
    private @NotNull MockClock clock;

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

    private void testReflectGame(int iReflection, String expected) {
        final ReversiData data = createRd();
        final COsGame game = data.getGame();
        game.setToDefaultStartPosition(OsClock.DEFAULT, OsClock.DEFAULT);
        game.append(ThorTest.mli("F5"));
        data.ReflectGame(iReflection);
        assertEquals(1, game.nMoves());
        assertEquals(expected, game.getMli(0).move.toString());
        data.ReflectGame(iReflection);
    }

    private static OptionSource mockOptionSource() {
        final OptionSource optionSource = Mockito.mock(OptionSource.class);
        Mockito.stub(optionSource.getStartPosition()).toReturn(new StartPosition(Position.START_POSITION));
        return optionSource;
    }

    public void testUpdate() {
        final ReversiData data = createRd();

        data.update(ThorTest.mli("F5"), true);
        data.update(ThorTest.mli("D6"), true);

        // move matches game; game should not be broken
        data.SetIMove(0);
        data.update(ThorTest.mli("F5"), true);
        assertEquals(2, data.nMoves());

        // move does not match game; game should be broken
        data.SetIMove(0);
        data.update(ThorTest.mli("D3"), true);
        assertEquals(1, data.nMoves());
    }

    private ReversiData createRd() {
        final OptionSource optionSource = mockOptionSource();
        final EngineTalker engineTalker = Mockito.mock(EngineTalker.class);
        clock = new MockClock();
        clock.setMillis(37);
        return new ReversiData(optionSource, engineTalker, clock);
    }

    public void testTiming() {
        final BoardSource data = createRd();

        assertEquals(0., data.secondsSinceLastMove());

        clock.setMillis(1037);
        assertEquals(1., data.secondsSinceLastMove());

        data.update(new OsMoveListItem("F5"), true);
        assertEquals(0., data.secondsSinceLastMove());
        clock.setMillis(2037);
        assertEquals(1., data.secondsSinceLastMove());
    }

    public void testPasteGame() {
        final ReversiData data = createRd();
        final COsGame game = new COsGame();
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);
        game.append(new OsMoveListItem("F5/1/2"));

        data.paste(game.toString());
        assertEquals(game.toString(), data.getGame().toString());
    }

    public void testPasteMoveList() {
        final ReversiData data = createRd();
        data.paste("F5 d6");
        assertEquals("F5D6", data.getGame().getMoveList().toMoveListString());
    }

    public void testPasteBoard() {
        final ReversiData data = createRd();
        data.paste("F5 D6");


        final String text = Position.ALTERNATE_START_POSITION.play("F6").positionString();
        System.out.println(text);
        data.paste(text);

        assertEquals(0, data.getGame().nMoves());
        final String expected = "8" + text.replaceAll(" ", "");
        assertEquals(expected, data.getGame().getPos().board.toString().replaceAll(" ", ""));
        assertEquals(expected, data.getGame().getStartPosition().board.toString().replaceAll(" ", ""));
    }

    public void testPasteFails() {
        final ReversiData data = createRd();
        testPasteFails(data, "foo bar", "Can't interpret as a move list, board, or game: \"foo bar\"");
        testPasteFails(data, "D7", "Invalid move list: Move flips no disks: D7");
        testPasteFails(data, "(;GM[Othello]PC[here]PB[me]PW[you]RE[?]TI[24:00:00]TY[8r]BO[8 -------- -------- ----*O-- --*OO--- --*OO--- --****-- ----*--- -------- O];)"
                , "Random match type is missing # of random disks");
    }

    private static void testPasteFails(ReversiData data, String pasteText, String message) {
        try {
            data.paste(pasteText);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            assertEquals(message, e.getMessage());
        }
    }

    /**
     * When we set a position, the listener should receive a signal that the board has changed.
     */
    public void testSetStartPosition() {
        final ReversiData data = createRd();
        //noinspection unchecked
        final SignalListener<OsMoveListItem> listener = Mockito.mock(SignalListener.class);
        data.addListener(listener);

        data.StartNewGame(new StartPosition(Position.START_POSITION, new OsMove("F5")));
        assertEquals(1, data.IMove());
        assertEquals(59, data.DisplayedPosition().board.nEmpty());

        Mockito.verify(listener).handleSignal(null);
        Mockito.verifyNoMoreInteractions(listener);
    }
}
