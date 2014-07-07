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

import com.welty.nboard.gui.Grid;
import com.welty.nboard.nboard.ReversiData;
import com.welty.nboard.nboard.ReversiWindow;

import javax.swing.*;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 8, 2009
 * Time: 7:31:36 PM
 * </PRE>
 */
class DatabaseTable extends Grid {

    private final ReversiData reversiData;
    private final DatabaseTableModel dtm;

    private final ReversiWindow reversiWindow;    //!< When a game is clicked, update this window with the new game

    /**
     * Construct the table; create columns and sizer
     *
     * @param reversiWindow When the user selects a game it is sent to this window
     */
    DatabaseTable(ReversiWindow reversiWindow, ReversiData reversiData, DatabaseTableModel dtm) {
        super(dtm, new JTable(dtm), true, false, true);
        this.reversiWindow = reversiWindow;
        this.reversiData = reversiData;
        this.dtm = dtm;
        final JTable table = getTable();
        table.setAutoCreateRowSorter(true);
        final DefaultRowSorter rowSorter = (DefaultRowSorter) table.getRowSorter();
        rowSorter.setComparator(2, new AsDoubleSort());
        rowSorter.setComparator(4, new AsDoubleSort());
    }

    DatabaseTableModel PD() {
        return dtm;
    }

    /**
     * Send the selected game to the ReversiWindow
     */
    public void selectionChanged(int modelRow, int modelCol) {
        if (modelRow < PD().getRowCount()) {
            reversiData.setGame(dtm.gameFromRow(modelRow), false);
            reversiWindow.BringToTop();
        }
    }
}
