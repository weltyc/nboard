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

import com.welty.nboard.nboard.NBoard;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 7, 2009
 * Time: 9:28:13 PM
 * </PRE>
 */
class ThorFileChooser {
    private final JFileChooser chooser;

    public ThorFileChooser() {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
    }

    /**
     * @param regKey      registry key to store previously selected file of this type
     * @param description e.g. "Text Files"
     * @param extension   e.g. ".txt". Multiple extensions are allowed if they are separated by ';', e.g. ".txt;.ggf" @return file to open, or null if user did not with to open
     * @return file to open, or null if no file should be chosen
     */
    public @Nullable File open(String regKey, String description, String extension) {
        String fn = NBoard.RegistryReadString(regKey, "");
        if (fn != null) {
            setSelectedFile(fn);
        }
        setFileFilter(description, extension, false);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
            final File file = chooser.getSelectedFile();
            NBoard.RegistryWriteString(regKey, file.getAbsolutePath());
            return file;
        } else {
            return null;
        }
    }

    /**
     * @param description e.g. "Text Files"
     * @param extension   e.g. ".txt". Multiple extensions are allowed if they are separated by ';', e.g. ".txt;.ggf"
     * @return file to save to, or null if user did not with to save
     */
    public @Nullable File save(String description, String extension) {
        setFileFilter(description, extension, false);
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(null)) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    /**
     * @param description e.g. "Text Files"
     * @param extension   e.g. ".txt". Multiple extensions are allowed if they are separated by ';', e.g. ".txt;.ggf"
     * @return files to save, or null if user did not wish to open
     */
    public @Nullable File[] opens(String description, String extension) {
        setFileFilter(description, extension, true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
            return chooser.getSelectedFiles();
        } else {
            return null;
        }
    }

    private void setFileFilter(String description, String extension, boolean multiSelection) {
        chooser.resetChoosableFileFilters();
        chooser.addChoosableFileFilter(new MyFileFilter(description, extension));
        chooser.setMultiSelectionEnabled(multiSelection);
    }

    public void setSelectedFile(String fn) {
        chooser.setSelectedFile(new File(fn));
    }

    private static class MyFileFilter extends FileFilter {
        private final String description;
        private final String[] extensions;

        /**
         * @param description e.g. "Text Files"
         * @param extension   e.g. ".txt". Multiple extensions are allowed if they are separated by ';', e.g. ".txt;.ggf"
         */
        MyFileFilter(String description, String extension) {
            this.description = description;
            extensions = extension.split(";");
        }

        public boolean accept(File file) {
            return file.isDirectory() || extensionOk(file.getName());
        }

        private boolean extensionOk(String name) {
            for (String extension : extensions) {
                if (name.toUpperCase().endsWith(extension.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return description;
        }
    }
}
