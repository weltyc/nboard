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

package com.welty.nboard.thor;

import com.orbanova.common.feed.Handler;
import com.orbanova.common.jsb.JsbFileChooser;
import com.welty.othello.thor.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.welty.othello.thor.ThorOpeningMap.NOpenings;
import static com.welty.othello.thor.ThorOpeningMap.OpeningName;

/**
 * Class responsible for loading data into the DatabaseData
 */
public class DatabaseLoader {
    private final JsbFileChooser chooser;
    private final DatabaseData databaseData;

    /**
     * Construct a DatabaseLoader that loads data into the database
     *
     * @param databaseData database to load into
     */
    public DatabaseLoader(JFrame frame, DatabaseData databaseData) {
        chooser = new JsbFileChooser(frame, DatabaseLoader.class);
        this.databaseData = databaseData;
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

    public void loadDirectory() {
        final Handler<File> callback = new Handler<File>() {
            @Override public void handle(@NotNull File file) {
                databaseData.loadFromThorDirectory(file);
            }
        };
        JFileChooser base = chooser.getChooser();
        JTextArea textArea = new JTextArea("Select directory containing games.\n\nSupported formats:\n Thor files (.wtb, .jou, .trn)\n GGF files (.ggf)");
        textArea.setOpaque(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        base.setAccessory(textArea);
        base.setDialogTitle("Select database directory");
        chooser.chooseFile(callback, JFileChooser.DIRECTORIES_ONLY);
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
            databaseData.clearGames();
            for (String it : fns) {
                try {
                    if (DatabaseData.isThorGamesFile(it)) {
                        games.addAll(Thor.ThorLoadGames(it, tracker));
                    } else {
                        final ArrayList<GgfGameText> ggfGameTexts = GgfGameText.Load(new File(it), tracker);
                        databaseData.addGgfGames(ggfGameTexts);
                    }
                } catch (IllegalArgumentException e) {
                    errorDisplayer.notify("loading games file", e.getMessage());
                }
            }
            databaseData.setThorGames(games);
        }
    }
}
