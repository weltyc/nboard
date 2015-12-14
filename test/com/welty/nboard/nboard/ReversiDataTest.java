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

package com.welty.nboard.nboard;

import com.orbanova.common.clock.MockClock;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.nboard.startpos.StartPosition;
import com.welty.novello.core.Board;
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


    public static OsMoveListItem mli(String moveText) {
        return new OsMoveListItem(new OsMove(moveText));
    }

    private void testReflectGame(int iReflection, String expected) {
        final ReversiData data = createRd();
        final COsGame game = data.getGame();
        game.setToDefaultStartPosition(OsClock.DEFAULT, OsClock.DEFAULT);
        game.append(mli("F5"));
        data.ReflectGame(iReflection);
        assertEquals(1, game.nMoves());
        assertEquals(expected, game.getMli(0).move.toString());
        data.ReflectGame(iReflection);
    }

    private static OptionSource mockOptionSource() {
        final OptionSource optionSource = Mockito.mock(OptionSource.class);
        Mockito.stub(optionSource.getStartPosition()).toReturn(new StartPosition(Board.START_BOARD));
        return optionSource;
    }

    public void testUpdate() {
        final ReversiData data = createRd();

        data.update(mli("F5"), true);
        data.update(mli("D6"), true);

        // move matches game; game should not be broken
        data.SetIMove(0);
        data.update(mli("F5"), true);
        assertEquals(2, data.nMoves());

        // move does not match game; game should be broken
        data.SetIMove(0);
        data.update(mli("D3"), true);
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

    public void testPastePlayOk() {
        // Move list in playOK format
        String moveList = "1. f5 f4 2. e3 f6 3. c4 c5 4. g6 d2 5. d6 g5 6. e2 d3 7. f3 b4 8. e6 f1 9. g3\n" +
                "g4 10. b6 b5 11. f2 c3 12. h3 h4 13. c6 h2 14. e1 d1 15. h6 c7 16. d7 a6 17. c2\n" +
                "b3 18. a5 a4 19. c1 b1 20. h5 h7 21. g2 e8 22. d8 f7 23. e7 f8 24. g8 c8 25. b8\n" +
                "b7 26. a8 a7 27. a3 b2 28. a1 a2 29. g1 h1 30. g7 h8";
        moveList = ReversiData.compressMoveList(moveList);
        assertTrue(ReversiData.looksLikeMoveList(moveList));
        final ReversiData data = createRd();
        data.paste(moveList);
        String mls = data.getGame().getMoveList().toMoveListString();
        assertEquals("F5F4E3", mls.substring(0, 6));
        assertTrue(mls.endsWith("H8"));
    }

    public void testPastePlayOkWithPass() {
        // Move list in playOK format
        String moveList = "1. f5 f4 2. g3 e6 3. f3 g4 4. f7 c5 5. d6 f6 6. e3 e7 7. c3 d7 8. c4 d3 9. b6\n" +
                "e2 10. d2 h3 11. g5 f8 12. h4 g6 13. h2 b4 14. e1 f2 15. h5 f1 16. c2 d1 17. c1\n" +
                "b1 18. c8 d8 19. b5 c6 20. b3 a3 21. a4 a5 22. a6 a7 23. c7 h6 24. h7 g2 25. b7\n" +
                "g7 26. h8 b8 27. a8 g8 28. e8 -- 29. a2 b2 30. a1 -- 31. g1";
        moveList = ReversiData.compressMoveList(moveList);
        assertTrue(ReversiData.looksLikeMoveList(moveList));
        final ReversiData data = createRd();
        data.paste(moveList);
        String mls = data.getGame().getMoveList().toMoveListString();
        assertEquals("F5F4G3", mls.substring(0,6));
        assertTrue(mls.endsWith("G1"));
    }

    public void testReversiWarWithPass() {
        // reversi wars omits passes
        String moveList = "F5D6C3D3C4F4F6F3E3B4B5E6E7F2E2G5G6F7G3E1C5C6H4G4D2H6F8H5H7H3H2C1D1A4A6A5F1A7D7C7B6G1B3E8D8G2B7B8A8C8G8G7H8A2A3H1A1B2C2B1";
        moveList = ReversiData.compressMoveList(moveList);
        assertTrue(ReversiData.looksLikeMoveList(moveList));
        final ReversiData data = createRd();
        data.paste(moveList);
        String mls = data.getGame().getMoveList().toMoveListString();
        assertEquals("F5D6C3", mls.substring(0, 6));
        assertTrue(mls.endsWith("B1"));
    }

    public void testPasteBoard() {
        final ReversiData data = createRd();
        data.paste("F5 D6");


        final String text = Board.ALTERNATE_START_BOARD.play("F6").positionString();
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

        data.StartNewGame(new StartPosition(Board.START_BOARD, new OsMove("F5")));
        assertEquals(1, data.IMove());
        assertEquals(59, data.DisplayedPosition().board.nEmpty());

        Mockito.verify(listener).handleSignal(null);
        Mockito.verifyNoMoreInteractions(listener);
    }

    public void testGetOthelloReplayerText() {
        final ReversiData data = createRd();
        data.getGame().append(new OsMoveListItem("F5/1/2"));

        String or = data.getOthelloReplayerHtml("foo").split("-->")[1];
        assertEquals(
                "\n<head>\n" +
                        "    <script src=\"othello-replayer.js\"></script>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<div id=\"foo\" style=\"line-height: 0;\"></div>\n" +
                        "<br/><br/>\n" +
                        "<input type=\"button\" value=\"reset\" id=\"foo_reset\"/>\n" +
                        "<input type=\"button\" value=\"prev\" id=\"foo_prev\"/>\n" +
                        "<input type=\"button\" value=\"next\" id=\"foo_next\"/>\n" +
                        "\n" +
                        "<font class=\"score\">Black: <span id=\"foo_black\"></span>, White: <span id=\"foo_white\"></span></font>\n" +
                        "\n" +
                        "<script>\n" +
                        "    if (typeof Oth === 'undefined') {\n" +
                        "        var err = document.createElement(\"p\");\n" +
                        "        err.style.backgroundColor = '#FF8080';\n" +
                        "        err.innerHTML = \"To view the embedded othello game:<br/>\" +\n" +
                        "                \"download <a href=https://github.com/emmettnicholas/OthelloReplayer/archive/master.zip>OthelloReplayer</a>, <br/>\" +\n" +
                        "                \"then put <code>othello-replayer.js</code> and the <code>images/</code> folder in the same folder as this web page. \";\n" +
                        "        document.body.insertBefore(err, document.body.firstChild);\n" +
                        "    }\n" +
                        "    else {\n" +
                        "        new Oth.Grid('foo', '--------|--------|--------|---wb---|---bw---|--------|--------|--------', 'b', '', 'F5');\n" +
                        "    }\n" +
                        "</script>\n" +
                        "</body>\n" +
                        "</html>"
                , or);
    }
}
