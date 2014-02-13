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
            reversiData.setGame(dtm.GameFromFilteredRow(modelRow), false);
            reversiWindow.BringToTop();
        }
    }
}
