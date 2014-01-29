package com.welty.nboard.thor;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 23, 2009
 * Time: 9:25:27 PM
 * To change this template use File | Settings | File Templates.
 */
class TextFileChooser {
    private final JFileChooser chooser;

    public TextFileChooser() {
        chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new TextFileFilter());
    }

    public @Nullable File open() {
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public @Nullable File save() {
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(null)) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    private static class TextFileFilter extends FileFilter {
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().endsWith(".txt");
        }

        public String getDescription() {
            return "Text files (*.txt)";
        }
    }
}
