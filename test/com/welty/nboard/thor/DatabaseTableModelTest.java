package com.welty.nboard.thor;

import com.orbanova.common.misc.ArrayTestCase;
import com.welty.nboard.nboard.BoardSource;
import com.welty.nboard.nboard.OptionSource;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;
import org.easymock.EasyMock;
import org.mockito.Mockito;

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
public class DatabaseTableModelTest extends ArrayTestCase {
    private static void testGameItemText(final DatabaseTableModel dd) {
        // Thor game
        assertTrue(dd.getValueAt(0, 0).equals("???"));
        assertTrue(dd.getValueAt(0, 1).equals("???"));
        assertTrue(dd.getValueAt(0, 2).equals("1980"));
        assertTrue(dd.getValueAt(0, 3).equals("???"));
        assertTrue(dd.getValueAt(0, 4).equals("-2"));
        assertTrue(dd.getValueAt(0, 5).equals("Parallel"));

        // GGF game
        assertTrue(dd.getValueAt(1, 0).equals("Saio1200"));
        assertTrue(dd.getValueAt(1, 1).equals("Saio3000"));
        assertTrue(dd.getValueAt(1, 2).equals("2003"));
        assertTrue(dd.getValueAt(1, 3).equals("GGS/os"));
        assertTrue(dd.getValueAt(1, 4).equals("0"));
        assertTrue(dd.getValueAt(1, 5).equals("No-Kung"));
    }

    public void testDatabaseData() throws IOException {
        final String ggfFile = createTempFile(".ggf", "test.ggf");
        final String wtbFile = createTempFile(".WTB", "test.WTB");
        final List<String> fns = Arrays.asList(ggfFile, wtbFile);

        DatabaseTableModel dtm = createDtm(fns);

        assertEquals(2, dtm.getRowCount());

        testGameItemText(dtm);

        COsGame osg = new COsGame();
        osg.setToDefaultStartPosition(OsClock.DEFAULT, OsClock.DEFAULT);

        dtm.lookUpPosition(osg.pos.board);
        assertEquals(dtm.summary.size(), 2);

        osg.append(new OsMoveListItem(new OsMove("F5")));
        dtm.lookUpPosition(osg.pos.board);
        assertEquals(dtm.summary.size(), 2);

        osg.append(new OsMoveListItem(new OsMove("D6")));
        dtm.lookUpPosition(osg.pos.board);
        assertEquals(dtm.summary.size(), 1);
    }

    public void testFiltering() throws IOException {
        final String ggfFile = createTempFile(".ggf", "test.ggf");
        final List<String> fns = Arrays.asList(ggfFile);

        DatabaseTableModel dtm = createDtm(fns);

        assertEquals(1, dtm.getRowCount());

        final int BLACK_PLAYER_NAME = 0;
        dtm.setFilter("q", BLACK_PLAYER_NAME);
        assertEquals(0, dtm.getRowCount());

        dtm.setFilter("Saio", BLACK_PLAYER_NAME);
        assertEquals("ok if user name starts with filter", 1, dtm.getRowCount());

        dtm.setFilter("Saio1200", BLACK_PLAYER_NAME);
        assertEquals(1, dtm.getRowCount());
    }

    public void testReadingIrregularGames() throws IOException {
        // in this file, there are two irregular games: a 4x4 game and an 8x8 random-start game.
        // both of these should be ignored by the reader.
        // After this there is a standard game, which should be read in by the reader.
        final String ggfFile = createTempFile(".ggf", "test2.ggf");
        final List<String> fns = Arrays.asList(ggfFile);

        DatabaseTableModel dtm = createDtm(fns);

        assertEquals(1, dtm.getRowCount());

        // The DT field (date/time) normally contains a text string starting with the year, e.g. "2008-07-06".
        // the only game that works exhibits a bug found in early GGS games: the time is given in seconds since
        // 1970-01-01. Make sure that we can handle this anomaly
        assertEquals("1999", dtm.getValueAt(0, DatabaseTableModel.YEAR));
    }

    private static DatabaseTableModel createDtm(List<String> fns) {
        // set up a sample database
        final OptionSource optionSource = EasyMock.createNiceMock(OptionSource.class);
        final BoardSource boardSource = new BoardSourceStub();

        final DatabaseData databaseData = new DatabaseData();
        DatabaseTableModel dtm = new DatabaseTableModel(optionSource, boardSource, databaseData);
        assertEquals(0, dtm.getRowCount());

        reloadGames(databaseData, fns);
        return dtm;
    }

    /**
     * Call databaseData.reloadGames() with mock progress tracker and error displayer
     *
     * @param databaseData database data to do the loading
     * @param fns          files to load
     */
    private static void reloadGames(DatabaseData databaseData, List<String> fns) {
        final IndeterminateProgressTracker tracker = Mockito.mock(IndeterminateProgressTracker.class);
        final ErrorDisplayer errorDisplayer = Mockito.mock(ErrorDisplayer.class);
        new DatabaseLoader(databaseData).reloadGames(fns, errorDisplayer, tracker);
    }

    public void testInitialLookup() throws IOException {
        // set up a sample database
        final OptionSource optionSource = EasyMock.createNiceMock(OptionSource.class);
        final BoardSourceStub boardSource = new BoardSourceStub();
        EasyMock.expect(optionSource.ThorLookUpAll()).andReturn(true).times(2);
        EasyMock.replay(optionSource);

        final DatabaseData databaseData = new DatabaseData();
        DatabaseTableModel dd = new DatabaseTableModel(optionSource, boardSource, databaseData);

        reloadGames(databaseData, Arrays.asList(createTempFile(".ggf", "test.ggf")));

        // we should have exactly one game, the game at index 0
        assertEquals(1, dd.getRowCount());

        //after playing one move we should still have exactly one game
        boardSource.append(new OsMoveListItem("F5"));
        assertEquals(1, dd.getRowCount());

        //after two moves we should have no matching games, since the displayed position is the diagonal
        // but the database game is perpendicular.
        boardSource.append(new OsMoveListItem("F6"));
        assertEquals(0, dd.getRowCount());
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
