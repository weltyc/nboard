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

import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Require;
import com.orbanova.common.misc.Utils;
import com.welty.othello.gdk.COsBoard;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.prefs.Preferences;

/**
 * Main class for the NBoard application
 */
public class NBoard {
    private static final Logger log = Logger.logger(NBoard.class);

    public static final Color highlightColor = new Color(0x28, 0x98, 0x30);
    public static final Color boardColor = new Color(0x38, 0x78, 0x30);

    private static final String sRegKey = "/Software/Welty/NBoard/";

    public static void main(final String[] args) {
        logVersion();
        checkMaxMemory();
        Install.install();
//        CheckThreadViolationRepaintManager.install();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(args);
            }
        });
    }

    /**
     * Print the NBoard version number in case there are issues.
     */
    private static void logVersion() {
        try (InputStream stream = NBoard.class.getResourceAsStream("version.txt")) {
            if (stream == null) {
                log.info("No version number available");
            } else {
                final String version = new BufferedReader(new InputStreamReader(stream)).readLine();
                log.info("NBoard version " + version);
            }
        } catch (Exception e) {
            log.info("Exception when reading version number: " + e);
        }
    }

    /**
     * Log the memory usage. If there's not enough memory available,
     * notify the user and exit.
     */
    private static void checkMaxMemory() {
        long maxMegs = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        log.info("Max memory: " + maxMegs + "M");
        if (maxMegs < 100) {
            try {
                Path jarPath = Utils.getJarPath(NBoard.class);
                log.info("trying to restart with more memory at " + jarPath);
                new ProcessBuilder()
                        .inheritIO()
                        .command("java", "-Xmx128M", "-jar", jarPath.toString())
                        .start();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Tried to restart with more RAM but couldn't. The error was:\n" + e, "Out of Memory", JOptionPane.ERROR_MESSAGE);
                log.info("Exit due to lack of RAM");
            }
            System.exit(-1);
        }
    }
    private static void createAndShowGUI(String[] args) {

        // todo associate .ggf files with this executable
        // Z::AssociateFileType(".ggf", "ggf_games", "GGF Games", sMFN+",101", sMFN+" %1");


        // main window
        ReversiWindow pw = new ReversiWindow();

        // tip of the day
        if (RegistryReadU4("View/Totd", 1) != 0) {
            new TipOfTheDayTlw("View/TotdId");
        }
        if (args.length > 0)
            pw.OpenFile(new File(args[0]));
    }

    public static int RegistryReadU4(String key, int def) {
        Preferences p = Preferences.userRoot();
        final int prefValue = p.getInt(sRegKey + key, def);
        return prefValue;
    }

    public static String RegistryReadString(String key, String def) {
        Preferences p = Preferences.userRoot();
        return p.get(sRegKey + key, def);
    }

    public static void RegistryWriteU4(String key, int i) {
        Preferences p = Preferences.userRoot();
        p.putInt(sRegKey + key, i);
    }

    public static void RegistryWriteString(String key, String s) {
        Preferences p = Preferences.userRoot();
        p.put(sRegKey + key, s);
    }

    /**
     * Load an ImageIcon from a resource.
     *
     * @param path Image name within com.welty.nboard.nboard.images.
     * @return the ImageIcon.
     */
    public static ImageIcon getImage(String path) {
        java.net.URL imgURL = NBoard.class.getResource("images/" + path);
        Require.notNull(imgURL, "image url for " + path);
        final ImageIcon icon = new ImageIcon(imgURL);
        Require.notNull(icon, "icon");
        return icon;
    }

    public static JLabel createLabel(int width, int alignment) {
        final JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, 18));
        label.setFont(label.getFont().deriveFont(0));
        //noinspection MagicConstant
        label.setHorizontalAlignment(alignment);
        return label;
    }

    static void drawPiece(Graphics gd, char pc, Rectangle rect, Color bgColor) {
        // draw background
        rect.x++;
        rect.y++;
        GraphicsUtils.fillRect(gd, rect, bgColor);
        if (pc == COsBoard.BLACK || pc == COsBoard.WHITE) {
            rect = GraphicsUtils.FractionalInflate(rect, -.2);
            rect.x -= 1;
            rect.y -= 1;
            GraphicsUtils.fillEllipse(gd, rect, pc == COsBoard.BLACK ? Color.black : Color.white);
            GraphicsUtils.outlineEllipse(gd, rect, Color.BLACK);
        }
    }

    public static void paintSquare(Graphics g, int col, int row, char piece, Color bgColor) {
        Rectangle rect = BoardPanel.rectFromSquare(col, row, BoardPanel.n, BoardPanel.boardArea);
        drawPiece(g, piece, rect, bgColor);
    }
}
