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
