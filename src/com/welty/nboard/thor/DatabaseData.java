package com.welty.nboard.thor;

import com.welty.othello.c.CReader;
import com.welty.othello.gdk.*;
import com.welty.nboard.BoardSource;
import com.welty.nboard.GgfGameText;
import com.welty.nboard.NBoard;
import com.welty.nboard.OptionSource;
import com.welty.nboard.gui.Align;
import com.welty.nboard.gui.GridColumn;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.gui.SignalListener;
import static com.welty.nboard.thor.Thor.*;
import static com.welty.nboard.thor.ThorOpeningMap.NOpenings;
import static com.welty.nboard.thor.ThorOpeningMap.OpeningName;
import static com.welty.othello.core.Utils.Col;
import static com.welty.othello.core.Utils.Row;

import gnu.trove.list.array.TIntArrayList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.*;

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
        return m_players.size();
    }

    int NTournaments() {
        return m_tournaments.size();
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
    private final HashSet<String> m_fnThorGames = new HashSet<String>();        //*< Currently loaded Thor games files

    private TIntArrayList m_index = new TIntArrayList();            //*< List of games that match the displayed position.
    private final static int nFields = 6;
    private final String[] m_filters = new String[nFields];                //*< Text that must match the given field in order to display the position

    private int m_nThorGames;                    //*< Number of games loaded from WTB files
    private final ArrayList<GgfGameText> m_ggfGames = new ArrayList<GgfGameText>();    //*< GGF text of games loaded from GGF files
    private ArrayList<ThorGameInternal> m_tgis = new ArrayList<ThorGameInternal>();    //*< WTB games followed by converted GGF games
    private ArrayList<String> m_players = new ArrayList<String>();
    private ArrayList<String> m_tournaments = new ArrayList<String>();


    /**
     * @return true if the database window should be displayed.
     */
    public boolean IsReady() {
        return NGames() != 0;
    }

    /**
     * @return the total number of games loaded (both Thor and GGF)
     */
    public int NGames() {
        return m_tgis.size();
    }

    /**
     * @return a game in GGS/os format.
     */
    public COsGame GetOsGame(int iGame) {
        COsGame game = new COsGame();

        if (iGame < m_nThorGames) {
            final ThorGameInternal tg = m_tgis.get(iGame);

            game.SetDefaultStartPos();
            game.pis[0].sName = PlayerName(tg.iWhitePlayer);
            game.pis[1].sName = PlayerName(tg.iBlackPlayer);
            game.pis[0].dRating = game.pis[1].dRating = 0;
            game.sPlace = TournamentName(tg.iTournament);
            for (int i = 0; i < 60 && tg.moves[i] >= 0; i++) {
                final int sq = tg.moves[i];
                COsMoveListItem mli = new COsMoveListItem(new COsMove(Row(sq), Col(sq)), 0, 0);

                // illegal moves end the game. Yes, the Thor database has some.
                if (!game.pos.board.IsMoveLegal(mli.mv))
                    break;
                game.Update(mli);

                if (!game.pos.board.HasLegalMove() && !game.pos.board.GameOver()) {
                    mli.mv.SetPass();
                    game.Update(mli);
                }
            }
            if (!game.pos.board.GameOver()) {
                COsResult osResult = new COsResult();
                osResult.status = COsResult.TStatus.kTimeout;
                osResult.dResult = tg.nBlackDiscs * 2 - 64;
                game.SetResult(osResult);
            }
        } else {
            game.In(new CReader(m_ggfGames.get(iGame - m_nThorGames).getText()));
        }

        return game;
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
                ArrayList<String> fns = new ArrayList<String>();
                String fn;
                while (null != (fn = config.readLine())) {
                    fns.add(fn);
                }
                LoadGames(fns);
                return true;
            }
            catch (IllegalArgumentException e) {
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
    public void LookUpPosition(final COsBoard pos) {
        if (pos.NEmpty() > 3) {
            // look up position
            final MatchingPositions matchingPositions = ThorFindMatchingPositions(m_tgis, pos);
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
            m_summary = ThorSummarize(m_tgis, pos, fi, fir);
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
    void SetFilter(String text, int field, final COsBoard pos) {
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
            if (!sFilter.isEmpty() && !sFilter.equals(GameItemText(item, field)))
                return false;
        }
        return true;
    }


    /**
     * @return the text (e.g. "Welty Chris") given the game (item) and field (e.g. 0=black player name)
     */
    public String GameItemText(int item, int field) {
        int n = 0;

        final ThorGameInternal game = m_tgis.get(item);

        if (item < m_nThorGames) {
            // Thor game

            switch (field) {
                case 0:
                    return PlayerName(game.iBlackPlayer);
                case 1:
                    return PlayerName(game.iWhitePlayer);
                case 2:
                    n = game.year;
                    break;
                case 3:
                    return TournamentName(game.iTournament);
                case 4:
                    n = game.nBlackDiscs * 2 - 64;
                    break;
                case 5:
                    return OpeningName(game.openingCode);
                default:
                    return "";
            }
        } else {
            // GGF game
            final GgfGameText text = m_ggfGames.get(item - m_nThorGames);
            switch (field) {
                case 0:
                    return text.PB();
                case 1:
                    return text.PW();
                case 2:
                    n = m_tgis.get(item).year;
                    break;
                case 3:
                    return text.PC();
                case 4:
                    n = game.nBlackDiscs * 2 - 64;
                    break;
                case 5:
                    return OpeningName(game.openingCode);
                default:
                    return "";
            }
        }

        return Integer.toString(n);
    }

    String PlayerName(char iPlayer) {
        if (iPlayer >= NPlayers())
            return "???";
        else
            return m_players.get(iPlayer);
    }

    String TournamentName(char iTournament) {
        if (iTournament >= NTournaments())
            return "???";
        else
            return m_tournaments.get(iTournament);
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
            final ArrayList<String> filenames = new ArrayList<String>();
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
        m_ggfGames.clear();
        m_tgis.clear();
        m_fnThorGames.clear();

        // Reclaim memory
        m_ggfGames.trimToSize();
        m_tgis.trimToSize();
    }

    /**
     * Load a set of Thor games files
     */
    void LoadGames(final List<String> fns) {
        if (!fns.isEmpty()) {
            UnloadGames();
            ArrayList<ThorGameInternal> games = new ArrayList<ThorGameInternal>();
            for (String it : fns) {
                try {
                    if (IsWtbFilename(it)) {
                        games.addAll(Thor.ThorLoadGames(it, games.size()));
                    } else {
                        m_ggfGames.addAll(GgfGameText.Load(new File(it)));
                    }
                    m_fnThorGames.add(it);
                }
                catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error loading games file", JOptionPane.ERROR_MESSAGE);
                }
            }
            ThorHideProgressWindow();
            m_index.clear();
            for (int i = 0; i < NGames(); i++)
                m_index.add(i);

            // copy database data for games into m_ggfTgis for faster searching
            m_tgis = games;
            m_nThorGames = games.size();
            for (final GgfGameText game : m_ggfGames) {
                final int nBlackSquares = (new CReader(game.RE()).readInt(0) / 2) + 32;
                final String dt = game.DT();
                int year = new CReader(dt).readInt(0);
                if (year > 100000) {
                    // early GGF games have a bug where the DT field is given in seconds since 1970-01-01 instead of
                    // the standard format. In this case, translate
                    final GregorianCalendar cal = new GregorianCalendar();
                    cal.setTimeInMillis((long) year * 1000);
                    year = cal.get(Calendar.YEAR);
                }
                ThorGameInternal tgi = new ThorGameInternal(nBlackSquares, game.Moves(), game.m_openingCode, year);
                m_tgis.add(tgi);
            }

            LookUpPosition();
            fireTableDataChanged();
        }
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
            m_players = ThorLoadPlayers(fn);
            m_fnThorPlayers = fn;
            fireTableDataChanged();
        }
        catch (IllegalArgumentException e) {
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
            m_tournaments = ThorLoadTournaments(fn);
            m_fnThorTournaments = fn;
            NBoard.RegistryWriteString("Thor/TournamentsFile", fn);
            fireTableDataChanged();
        }
        catch (IllegalArgumentException e) {
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
            int[] counts = new int[nOpenings];
            for (ThorGameInternal it : m_tgis) {
                counts[it.openingCode]++;
            }

            // write to file
            double nGames = m_tgis.size();
            StringBuilder os = new StringBuilder();
            os.append("freq.\tOpening Name\n");
            for (char openingCode = 0; openingCode < nOpenings; openingCode++) {
                if (openingCode != 0 || counts[openingCode] != 0) {
                    final double freq = counts[openingCode] / nGames;
                    os.append(String.format("%5.2f", freq * 100)).append("%\t").append(OpeningName(openingCode)).append("\n");
                }
            }
        }
        return file != null;
    }

    /**
     * @return result of the game, #black discs - #white discs, for Thor games only
     */
    int GameResult(int iGame) {
        return m_tgis.get(iGame).nBlackDiscs * 2 - 64;
    }

    /**
     * @return year in which the game was played, for Thor Games only
     */
    int GameYear(int iGame) {
        return m_tgis.get(iGame).year;
    }

    public int getRowCount() {
        return m_index.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return GameItemText(m_index.get(rowIndex), columnIndex);
    }

    public String getStatusString() {
        return getRowCount() + "/" + NGames() + " games selected";
    }
}
