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

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * A file chooser window that allows the user to choose a text file
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
