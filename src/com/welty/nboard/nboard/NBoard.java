package com.welty.nboard.nboard;

import com.orbanova.common.misc.Require;
import com.welty.othello.gdk.COsBoard;
import com.welty.othello.util.CheckThreadViolationRepaintManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Main class for the NBoard application
 */
public class NBoard {

    public static final Color highlightColor = new Color(0x28, 0x98, 0x30);
    public static final Color boardColor = new Color(0x38, 0x78, 0x30);

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
