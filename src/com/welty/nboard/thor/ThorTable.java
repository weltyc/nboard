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
class ThorTable extends Grid {

    private final ReversiData reversiData;
    private final DatabaseTableModel dd;

    private final ReversiWindow reversiWindow;    //!< When a game is clicked, update this window with the new game

    /**
     * Construct the table; create columns and sizer
     *
     * @param pwTarget When the user selects a game it is sent to this window
     */
    ThorTable(ReversiWindow pwTarget, ReversiData reversiData, DatabaseTableModel dd) {
        super(dd, new JTable(dd), true, false, true);
        reversiWindow = pwTarget;
        this.reversiData = reversiData;
        this.dd = dd;
        final JTable table = getTable();
        table.setAutoCreateRowSorter(true);
        final DefaultRowSorter rowSorter = (DefaultRowSorter) table.getRowSorter();
        rowSorter.setComparator(2, new AsDoubleSort());
        rowSorter.setComparator(4, new AsDoubleSort());
    }

    DatabaseTableModel PD() {
        return dd;
    }

    /**
     * Send the selected game to the ReversiWindow
     */
    public void selectionChanged(int modelRow, int modelCol) {
        if (modelRow < PD().NGames()) {
            reversiData.setGame(dd.GameFromFilteredRow(modelRow), false);
            reversiWindow.BringToTop();
        }
    }
}
