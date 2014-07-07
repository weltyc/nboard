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

import com.welty.nboard.nboard.ReversiData;
import com.welty.nboard.nboard.ReversiWindow;

/**
 * Create all database objects needed by the UI
 */
public class DatabaseUiPack {
    public final DatabaseLoader loader;
    public final DatabaseTableModel tableModel;
    public final DatabaseWindow window;

    public DatabaseUiPack(ReversiWindow reversiWindow, ReversiData reversiData) {
        final DatabaseData databaseData = new DatabaseData();
        tableModel = new DatabaseTableModel(reversiWindow, reversiData, databaseData);
        loader = new DatabaseLoader(databaseData);
        window = new DatabaseWindow(reversiWindow, reversiData, tableModel);
    }
}
