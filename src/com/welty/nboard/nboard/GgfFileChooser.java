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
class GgfFileChooser {
    private final JFrame reversiWindow;
    private final JFileChooser chooser;

    public GgfFileChooser(JFrame reversiWindow) {
        this.reversiWindow = reversiWindow;
        chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new GgfFileFilter()
        );
    }

    public @Nullable File open() {
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(reversiWindow)) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public @Nullable File save() {
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(reversiWindow)) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    private static class GgfFileFilter extends FileFilter {
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().endsWith(".ggf");
        }

        public String getDescription() {
            return "GGF files (*.ggf)";
        }
    }
}
