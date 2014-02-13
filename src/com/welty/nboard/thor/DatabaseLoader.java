package com.welty.nboard.thor;

import com.orbanova.common.misc.Require;
import com.welty.nboard.nboard.GgfGameText;
import com.welty.nboard.nboard.NBoard;
import com.welty.othello.c.CReader;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.welty.nboard.thor.Thor.ThorLoadPlayers;
import static com.welty.nboard.thor.Thor.ThorLoadTournaments;
import static com.welty.nboard.thor.ThorOpeningMap.NOpenings;
import static com.welty.nboard.thor.ThorOpeningMap.OpeningName;

/**
 * Class responsible for loading data into the DatabaseData
 */
public class DatabaseLoader {
    private final ThorFileChooser thorFileChooser = new ThorFileChooser();
    private String m_fnThorTournaments;    //*< Currently loaded Thor tournaments file
    private String m_fnThorPlayers;        //*< Currently loaded Thor players file
    private final HashSet<String> m_fnThorGames = new HashSet<>();        //*< Currently loaded Thor games files
    private final DatabaseData databaseData;

    /**
     * Construct a DatabaseLoader that loads data into the database
     *
     * @param databaseData database to load into
     */
    public DatabaseLoader(DatabaseData databaseData) {
        this.databaseData = databaseData;
    }

    public DatabaseLoader(DatabaseTableModel databaseTableModel) {
        this(databaseTableModel.getDatabase());
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
     * Unload games database to restore memory
     */
    public void UnloadGames() {
        m_fnThorGames.clear();
        databaseData.clearGames();
    }

    /**
     * Unload existing games files and load a new set, creating and updating a GUI to do so.
     * <p/>
     * Since this creates and updates a GUI, it must run on the EDT.
     * <p/>
     * Does nothing if fns is empty.
     */
    void LoadGames(final List<String> fns) {
        Require.isTrue(SwingUtilities.isEventDispatchThread(), "must run on EDT");
        final ErrorDisplayer errorDisplayer = new DialogErrorDisplayer();
        try (final IndeterminateProgressMonitor monitor = new IndeterminateProgressMonitor(" games loaded")) {
            reloadGames(fns, errorDisplayer, monitor);
        }
    }

    /**
     * Reloads the database games (but not players or tournaments) while updating the errorDisplayer and the tracker
     * <p/>
     * If the list of files is empty, this does nothing (on the assumption that this was called in error).
     * Otherwise it unloads all existing games files and loads all games from the file.
     *
     * @param fns            list of files to load
     * @param errorDisplayer location to display error messages
     * @param tracker        location to display progress tracking
     */
    void reloadGames(List<String> fns, ErrorDisplayer errorDisplayer, IndeterminateProgressTracker tracker) {
        if (!fns.isEmpty()) {
            ArrayList<ThorGameInternal> games = new ArrayList<>();
            UnloadGames();
            for (String it : fns) {
                try {
                    if (IsWtbFilename(it)) {
                        games.addAll(Thor.ThorLoadGames(it, tracker));
                    } else {
                        final ArrayList<GgfGameText> ggfGameTexts = GgfGameText.Load(new File(it), tracker);
                        databaseData.addGgfGames(ggfGameTexts);
                    }
                    m_fnThorGames.add(it);
                } catch (IllegalArgumentException e) {
                    errorDisplayer.notify("loading games file", e.getMessage());
                }
            }
            databaseData.setGames(games);
        }
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
            databaseData.setPlayers(ThorLoadPlayers(fn));
            m_fnThorPlayers = fn;
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
            databaseData.setTournaments(ThorLoadTournaments(fn));
            m_fnThorTournaments = fn;
            NBoard.RegistryWriteString("Thor/TournamentsFile", fn);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error loading tournament file", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Save opening frequencies to a file.
     * <p/>
     * The user chooses a filename and then the DatabaseData's opening frequencies are stored to that file.
     */
    public boolean SaveOpeningFrequencies() {
        final File file = new TextFileChooser().save();
        if (file != null) {
            // count opening frequencies
            final int nOpenings = NOpenings();
            final int[] counts = databaseData.getOpeningCounts(nOpenings);
            double nGames = databaseData.NGames();

            // write to file
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
}
