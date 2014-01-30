package com.welty.nboard.thor;

import com.welty.nboard.nboard.ReversiData;
import com.welty.nboard.nboard.ReversiWindow;
import com.welty.nboard.gui.Grid;

import javax.swing.*;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 8, 2009
 * Time: 7:31:36 PM
 * </PRE>
 */
class ThorTable extends Grid {

    private ReversiData m_pd;            //!< Pointer to reversi data
    private final DatabaseData dd;

    private ReversiWindow m_pwTarget;    //!< When a game is clicked, update this window with the new game

    /**
     * Construct the table; create columns and sizer
     *
     * @param pwTarget When the user selects a game it is sent to this window
     */
    ThorTable(ReversiWindow pwTarget, ReversiData pd, DatabaseData dd) {
        super(dd, new JTable(dd), true, false, true);
        m_pwTarget = pwTarget;
        m_pd = pd;
        this.dd = dd;
        final JTable table = getTable();
        table.setAutoCreateRowSorter(true);
        final DefaultRowSorter rowSorter = (DefaultRowSorter) table.getRowSorter();
        rowSorter.setComparator(2, new AsDoubleSort());
        rowSorter.setComparator(4, new AsDoubleSort());
    }

    DatabaseData PD() {
        return dd;
    }

    /**
     * Send the selected game to the ReversiWindow
     */
    public void MouseDataClick(int modelRow, int modelCol) {
        if (modelRow < PD().NGames()) {
            m_pd.Update(dd.GetOsGame(modelRow), false);
            m_pwTarget.BringToTop();
        }
    }

}
