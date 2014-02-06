package com.welty.nboard.nboard;

import com.orbanova.common.misc.Require;
import com.welty.othello.util.CheckThreadViolationRepaintManager;

import javax.swing.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Main class for the NBoard application
 */
public class NBoard {

    private static final String sRegKey = "/Software/Welty/NBoard/";

    public static void main(final String[] args) {
        CheckThreadViolationRepaintManager.install();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(args);
            }
        });
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

    public static ImageIcon getImage(String path) {
        java.net.URL imgURL = NBoard.class.getResource("images/" + path);
        Require.notNull(imgURL, "image url for " + path);
        final ImageIcon icon = new ImageIcon(imgURL);
        Require.notNull(icon, "icon");
        return icon;
    }
}
