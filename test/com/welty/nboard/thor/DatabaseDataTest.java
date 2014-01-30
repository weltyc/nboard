package com.welty.nboard.thor;

import com.orbanova.common.misc.ArrayTestCase;
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMove;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gdk.COsPosition;
import com.welty.nboard.nboard.BoardSource;
import com.welty.nboard.nboard.OptionSource;
import org.easymock.EasyMock;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 7, 2009
 * Time: 7:50:25 PM
 * </PRE>
 */
public class DatabaseDataTest extends ArrayTestCase {
    private static final String diagonalGame = "(;GM[Othello]PC[NBoard]DT[2004-11-24 13:47:34 GMT]PB[Chris]PW[Ntest2]RE[-12]TI[0]TY[8]BO[8 ---------------------------O*------*O--------------------------- *]B[F5]W[F6];)";

    public static void testIsWtbFilename() {
        assertTrue(DatabaseData.IsWtbFilename("temp.wtb"));
        assertTrue(!DatabaseData.IsWtbFilename("temp"));
        assertTrue(!DatabaseData.IsWtbFilename("bla.wtba"));
        assertTrue(DatabaseData.IsWtbFilename("foo.WTB"));
        assertTrue(DatabaseData.IsWtbFilename("c:/devl/othello/foo.wtB"));
    }

    //! test DatabaseData.GameItemText()
    private static void testGameItemText(final DatabaseData dd) {
        // Thor game
        assertTrue(dd.GameItemText(0, 0).equals("???"));
        assertTrue(dd.GameItemText(0, 1).equals("???"));
        assertTrue(dd.GameItemText(0, 2).equals("1980"));
        assertTrue(dd.GameItemText(0, 3).equals("???"));
        assertTrue(dd.GameItemText(0, 4).equals("-2"));
        assertTrue(dd.GameItemText(0, 5).equals("Parallel"));

        // GGF game
        assertTrue(dd.GameItemText(1, 0).equals("Saio1200"));
        assertTrue(dd.GameItemText(1, 1).equals("Saio3000"));
        assertTrue(dd.GameItemText(1, 2).equals("2003"));
        assertTrue(dd.GameItemText(1, 3).equals("GGS/os"));
        assertTrue(dd.GameItemText(1, 4).equals("0"));
        assertTrue(dd.GameItemText(1, 5).equals("No-Kung"));
    }

    public void testDatabaseData() throws IOException {
        final String ggfFile = createTempFile(".ggf", "test.ggf");
        final String wtbFile = createTempFile(".WTB", "test.WTB");
        final List<String> fns = Arrays.asList(ggfFile, wtbFile);

        DatabaseData dd = createSampleDatabase(fns);

        assertEquals(dd.NGames(), 2);
        assertEquals(dd.NPlayers(), 0);
        assertEquals(dd.NTournaments(), 0);

        testGameItemText(dd);

        COsGame osg = new COsGame();
        osg.SetDefaultStartPos();

        dd.LookUpPosition(osg.pos.board);
        assertEquals(dd.m_summary.size(), 2);

        osg.Update(new COsMoveListItem(new COsMove("F5"), 0, 0));
        dd.LookUpPosition(osg.pos.board);
        assertEquals(dd.m_summary.size(), 2);

        osg.Update(new COsMoveListItem(new COsMove("D6"), 0, 0));
        dd.LookUpPosition(osg.pos.board);
        assertEquals(dd.m_summary.size(), 1);
    }

    public void testReadingIrregularGames() throws IOException {
        // in this file, there are two irregular games: a 4x4 game and an 8x8 random-start game.
        // both of these should be ignored by the reader.
        // After this there is a standard game, which should be read in by the reader.
        final String ggfFile = createTempFile(".ggf", "test2.ggf");
        final List<String> fns = Arrays.asList(ggfFile);

        DatabaseData dd = createSampleDatabase(fns);

        assertEquals(1, dd.NGames());
        assertEquals(0, dd.NPlayers());
        assertEquals(0, dd.NTournaments());

        // The DT field (date/time) normally contains a text string starting with the year, e.g. "2008-07-06".
        // the only game that works exhibits a bug found in early GGS games: the time is given in seconds since
        // 1970-01-01. Make sure that we can handle this anomaly
        assertEquals(1999, dd.GameYear(0));
    }

    private static DatabaseData createSampleDatabase(List<String> fns) {
        // set up a sample database
        final OptionSource optionSource = EasyMock.createNiceMock(OptionSource.class);
        final BoardSource boardSource = EasyMock.createNiceMock(BoardSource.class);
        final COsGame game = new COsGame(new CReader(diagonalGame));
        EasyMock.expect(boardSource.DisplayedPosition()).andReturn(game.GetPosStart());
        EasyMock.replay(boardSource);

        DatabaseData dd = new DatabaseData(optionSource, boardSource);
        assertEquals(0, dd.NPlayers());
        assertEquals(0, dd.NTournaments());
        assertEquals(0, dd.NGames());

        dd.LoadGames(fns);
        return dd;
    }

    public void testInitialLookup() throws IOException {
        // set up a sample database
        final OptionSource optionSource = EasyMock.createNiceMock(OptionSource.class);
        final BoardSource boardSource = EasyMock.createNiceMock(BoardSource.class);
        final COsGame game = new COsGame(new CReader(diagonalGame));
        EasyMock.expect(boardSource.DisplayedPosition()).andReturn(game.GetPosStart());
        EasyMock.replay(boardSource);
        EasyMock.expect(optionSource.ThorLookUpAll()).andReturn(true).times(2);
        EasyMock.replay(optionSource);

        DatabaseData dd = new DatabaseData(optionSource, boardSource);

        dd.LoadGames(Arrays.asList(createTempFile(".ggf", "test.ggf")));

        // we should have exactly one game, the game at index 0
        assertEquals(new int[]{0}, dd.FilteredIndex().toArray());

        //after playing one move we should still have exactly one game
        EasyMock.reset(boardSource);
        final COsPosition pos1 = game.PosAtMove(1);
        EasyMock.expect(boardSource.DisplayedPosition()).andReturn(pos1);
        EasyMock.replay(boardSource);
        dd.OnBoardChanged();
        assertEquals(new int[]{0}, dd.FilteredIndex().toArray());

        //after two moves we should have no games, since the displayed position is the diagonal
        // but the database game is perpendicular.
        EasyMock.reset(boardSource);
        EasyMock.expect(boardSource.DisplayedPosition()).andReturn(game.PosAtMove(2));
        EasyMock.replay(boardSource);
        dd.OnBoardChanged();
        assertEquals(new int[0], dd.FilteredIndex().toArray());
    }

    public void testFoo() {
        final Foo foo = EasyMock.createNiceMock(Foo.class);
        EasyMock.expect(foo.bar()).andReturn(1);
        EasyMock.replay(foo);
        assertEquals(1, foo.bar());
        EasyMock.reset(foo);
        EasyMock.expect(foo.bar()).andReturn(2);
        EasyMock.replay(foo);
        assertEquals(2, foo.bar());
    }

    private static interface Foo {
        int bar();
    }

    /**
     * Create a temp file on disk
     *
     * @param extension file extension to use for the file, e.g. ".ggf"
     * @param resource  resource location for getResourceAsStream()
     * @return absolute path to the file
     * @throws IOException if file can't be created
     */
    private String createTempFile(String extension, String resource) throws IOException {
        final File file = File.createTempFile("abc", extension);
        final InputStream ggfStream = getClass().getResourceAsStream(resource);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        int c;
        while (0 <= (c = ggfStream.read())) {
            out.write(c);
        }
        ggfStream.close();
        out.close();
        file.deleteOnExit();
        return file.getAbsolutePath();
    }
}
