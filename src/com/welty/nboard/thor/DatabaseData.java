package com.welty.nboard.thor;

import com.orbanova.common.misc.Require;
import com.welty.nboard.gui.Align;
import com.welty.nboard.gui.GridColumn;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.nboard.BoardSource;
import com.welty.nboard.nboard.GgfGameText;
import com.welty.nboard.nboard.NBoard;
import com.welty.nboard.nboard.OptionSource;
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gdk.COsPosition;
import com.welty.othello.gdk.OsBoard;
import gnu.trove.list.array.TIntArrayList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.welty.nboard.thor.Thor.*;
import static com.welty.nboard.thor.ThorOpeningMap.NOpenings;
import static com.welty.nboard.thor.ThorOpeningMap.OpeningName;

/**
 * Encapsulate all data needed by the database window
 * <p/>
 * <PRE>
 * User: HP_Administrator
 * Date: Jun 26, 2009
 * Time: 11:14:48 PM
 * </PRE>
 */
public class DatabaseData extends GridTableModel {
    private final TextFileChooser textFileChooser = new TextFileChooser();
    private final ThorFileChooser thorFileChooser = new ThorFileChooser();

    private final DatabaseDataModel ddm = new DatabaseDataModel();

    private static final GridColumn[] m_columns = {
            new GridColumn(120, "Black", Align.LEFT),
            new GridColumn(120, "White", Align.LEFT),
            new GridColumn(50, "Year", Align.RIGHT),
            new GridColumn(150, "Place", Align.LEFT),
            new GridColumn(50, "Result", Align.RIGHT),
            new GridColumn(110, "Opening", Align.LEFT),
    };
    private final @NotNull OptionSource optionSource;
    private final @NotNull BoardSource boardSource;

    public DatabaseData(@NotNull OptionSource optionSource, @NotNull BoardSource boardSource) {
        super(m_columns);
        this.optionSource = optionSource;
        this.boardSource = boardSource;
        for (int i = 0; i < m_filters.length; i++) {
            m_filters[i] = "";
        }
        boardSource.AddListener(new SignalListener<COsMoveListItem>() {
            public void handleSignal(COsMoveListItem data) {
                OnBoardChanged();
            }
        });
    }

    int NPlayers() {
        return ddm.NPlayers();
    }

    int NTournaments() {
        return ddm.NTournaments();
    }

    void OnBoardChanged() {
        if (optionSource.ThorLookUpAll()) {
            LookUpPosition();
        } else {
            m_summary.clear();
            fireTableDataChanged();
        }
    }

    // Filtering
    public TThorSummary m_summary = new TThorSummary();

    private String m_fnThorTournaments;    //*< Currently loaded Thor tournaments file
    private String m_fnThorPlayers;        //*< Currently loaded Thor players file
    private final HashSet<String> m_fnThorGames = new HashSet<>();        //*< Currently loaded Thor games files

    /**
     * List of games that match the displayed position.
     * <p/>
     * The int is an index into m_tgis.
     */
    private TIntArrayList m_index = new TIntArrayList();
    private final static int nFields = 6;
    private final String[] m_filters = new String[nFields];                //*< Text that must match the given field in order to display the position


    /**
     * @return true if the database window should be displayed.
     */
    public boolean IsReady() {
        return ddm.NGames() != 0;
    }

    /**
     * @return the total number of games loaded (both Thor and GGF)
     */
    public int NGames() {
        return ddm.NGames();
    }

    /**
     * @param iFiltered model row, i.e. index into the list of displayed games
     * @return a game in GGS/os format.
     */
    public COsGame GameFromFilteredRow(int iFiltered) {
        return ddm.GameFromIndex(m_index.get(iFiltered));
    }

    /**
     * Save the current thor player/tournament/games files to the registry
     */
    public void SaveConfig() {
        StringBuilder sb = new StringBuilder();

        sb.append(m_fnThorPlayers).append("\n").append(m_fnThorTournaments).append("\n");
        for (String it : m_fnThorGames) {
            sb.append(it).append("\n");
        }
        NBoard.RegistryWriteString("Thor/Config", sb.toString());
    }

    /**
     * Load database files. Filenames are stored in the Registry by SaveConfig()
     *
     * @return true if there was a saved config
     */
    public boolean LoadConfig() {
        String sConfig = NBoard.RegistryReadString("Thor/Config", "");
        if (sConfig.isEmpty()) {
            return false;
        } else {
            try {
                CReader config = new CReader(sConfig);
                LoadPlayers(config.readLine());
                LoadTournaments(config.readLine());
                ArrayList<String> fns = new ArrayList<>();
                String fn;
                while (null != (fn = config.readLine())) {
                    fns.add(fn);
                }
                LoadGames(fns);
                return true;
            } catch (IllegalArgumentException e) {
                // probably an EOF exception.
                // todo should we stop translating EOF exceptions?
            }
            return false;
        }
    }

    public void LookUpPosition() {
        final COsPosition position = boardSource.DisplayedPosition();
        LookUpPosition(position.board);
    }

    /**
     * Look up a position in the database, filter, set summary, and signal that this has been done.
     */
    public void LookUpPosition(final OsBoard pos) {
        if (pos.NEmpty() > 3) {
            // look up position
            final MatchingPositions matchingPositions = ddm.findMatchingPositions(pos);
            m_index = matchingPositions.index;
            final TIntArrayList iReflections = matchingPositions.iReflections;

            // filter
            final int n = m_index.size();
            TIntArrayList fi = new TIntArrayList();
            TIntArrayList fir = new TIntArrayList();
            for (int i = 0; i < n; i++) {
                final int j = m_index.get(i);
                if (FilterOk(j)) {
                    fi.add(j);
                    fir.add(iReflections.get(i));
                }
            }

            // set summary
            m_summary = ddm.summarize(pos, fi, fir);
        } else {
            m_index.clear();
            m_summary.clear();
        }
        fireTableDataChanged();
    }

    /**
     * Alter the data's filter (e.g. filter by player name) and signal that this has been done.
     * <p/>
     * \pre field<6
     */
    void SetFilter(String text, int field, final OsBoard pos) {
        assert (field < 6);
        if (field < 6) {
            m_filters[field] = text;
            LookUpPosition(pos);
        }
    }

    /**
     * @return the index, filtered by the filter
     */
    TIntArrayList FilteredIndex() {
        TIntArrayList fi = new TIntArrayList();
        int n = m_index.size();
        for (int i = 0; i < n; i++) {
            final int j = m_index.get(i);
            if (FilterOk(j))
                fi.add(j);
        }
        return fi;
    }

    /**
     * @return true if the item matches all filters set in the filter window
     */
    boolean FilterOk(int item) {
        for (int field = 0; field < nFields; field++) {
            String sFilter = m_filters[field];
            if (!sFilter.isEmpty() && !sFilter.equals(ddm.GameItemText(item, field)))
                return false;
        }
        return true;
    }


    /**
     * Get the user's choice of thor games files and load them
     */
    public boolean LoadGames() {
        String fn = NBoard.RegistryReadString("Thor/GamesFiles", "");
        if (!fn.isEmpty()) {
            thorFileChooser.setSelectedFile(fn);
        }
        File[] files = thorFileChooser.opens("Thor and GGF games databases", ".wtb;.ggf");
        final boolean fOK = files != null;
        if (fOK) {
            if (files.length != 0) {
                NBoard.RegistryWriteString("Thor/GamesFiles", files[0].getAbsolutePath());
            }
            final ArrayList<String> filenames = new ArrayList<>();
            for (File file : files) {
                filenames.add(file.getAbsolutePath());
            }
            LoadGames(filenames);
        }
        return fOK;
    }

    /**
     * @return true if the file ends with ".wtb", with any capitalization accepted
     */
    static boolean IsWtbFilename(String fn) {
        return fn.toUpperCase().endsWith(".WTB");
    }

    /**
     * Unload games database to restore memory
     */
    public void UnloadGames() {
        m_fnThorGames.clear();
        ddm.clearGames();
    }

    /**
     * Unload existing games files and load a new set
     *
     * Does nothing if fns is empty.
     */
    void LoadGames(final List<String> fns) {
        if (!fns.isEmpty()) {
            ArrayList<ThorGameInternal> games = loadGames(fns);
            setGames(games);
        }
    }

    private void setGames(ArrayList<ThorGameInternal> games) {
        m_index.clear();
        for (int i = 0; i < NGames(); i++)
            m_index.add(i);

        ddm.setGames(games);

        LookUpPosition();
        fireTableDataChanged();
    }

    /**
     * Unload existing games and load new ones from the file
     *
     * This function creates and displays a progress bar, thus it must run on the EDT. It will block the EDT while running.
     *
     * @param fns filenames to load
     * @return list of games.
     */
    private ArrayList<ThorGameInternal> loadGames(List<String> fns) {
        Require.isTrue(SwingUtilities.isEventDispatchThread(), "must run on EDT");
        ArrayList<ThorGameInternal> games = new ArrayList<>();
        try (final IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(" games loaded")) {
            UnloadGames();
            for (String it : fns) {
                try {
                    if (IsWtbFilename(it)) {
                        games.addAll(Thor.ThorLoadGames(it, monitor));
                    } else {
                        final ArrayList<GgfGameText> ggfGameTexts = GgfGameText.Load(new File(it), monitor);
                        ddm.addGgfGames(ggfGameTexts);
                    }
                    m_fnThorGames.add(it);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error loading games file", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return games;
    }

    /**
     * Get the user's choice of thor player file and load it
     *
     * @return true if a file was selected by the user (even if it didn't load)
     */
    public boolean LoadPlayers() {
        File file = thorFileChooser.open("Thor/PlayersFile", "Thor players database", ".jou");
        if (file != null) {
            LoadPlayers(file.getAbsolutePath());
        }
        return file != null;
    }

    /**
     * Load a Thor players file
     */
    void LoadPlayers(final String fn) {
        try {
            ddm.setPlayers(ThorLoadPlayers(fn));
            m_fnThorPlayers = fn;
            fireTableDataChanged();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error loading players file", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get the user's choice of thor tournament file and load it
     */
    public boolean LoadTournaments() {
        final String regKey = "Thor/TournamentsFile";
        File file = thorFileChooser.open(regKey, "Thor Tournaments database", ".trn");
        if (file != null) {
            LoadTournaments(file.getAbsolutePath());
        }
        return file != null;
    }

    /**
     * Load a tournament file
     */
    void LoadTournaments(final String fn) {
        try {
            ddm.setTournaments(ThorLoadTournaments(fn));
            m_fnThorTournaments = fn;
            NBoard.RegistryWriteString("Thor/TournamentsFile", fn);
            fireTableDataChanged();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error loading tournament file", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Save opening frequencies to a file
     */
    public boolean SaveOpeningFrequencies() {
        final File file = textFileChooser.save();
        if (file != null) {
            // count opening frequencies
            final int nOpenings = NOpenings();
            final int[] counts = ddm.getOpeningCounts(nOpenings);

            // write to file
            double nGames = ddm.NGames();
            StringBuilder os = new StringBuilder();
            os.append("freq.\tOpening Name\n");
            for (char openingCode = 0; openingCode < nOpenings; openingCode++) {
                if (openingCode != 0 || counts[openingCode] != 0) {
                    final double freq = counts[openingCode] / nGames;
                    os.append(String.format("%5.2f", freq * 100)).append("%\t").append(OpeningName(openingCode)).append("\n");
                }
            }
            try (final BufferedWriter out = Files.newBufferedWriter(file.toPath(), Charset.defaultCharset())) {
                out.write(os.toString());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Can't write to file " + file + ": " + e, "Error writing to file", JOptionPane.ERROR_MESSAGE);
            }
        }
        return file != null;
    }

    /**
     * @return result of the game, #black discs - #white discs, for Thor games only
     */
    int GameResult(int iGame) {
        return ddm.GameResult(iGame);
    }

    /**
     * @return year in which the game was played, for Thor Games only
     */
    int GameYear(int iGame) {
        return ddm.getGameYear(iGame);
    }

    public int getRowCount() {
        return m_index.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return ddm.GameItemText(m_index.get(rowIndex), columnIndex);
    }

    public String getStatusString() {
        return getRowCount() + "/" + NGames() + " games selected";
    }

    public String GameItemText(int item, int field) {
        return ddm.GameItemText(item, field);
    }
}
