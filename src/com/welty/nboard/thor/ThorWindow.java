package com.welty.nboard.thor;

import com.welty.nboard.nboard.ReversiData;
import com.welty.nboard.nboard.ReversiWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Displays the games in tabular format
 * <PRE>
 * User: Chris
 * Date: Jul 8, 2009
 * Time: 3:03:09 PM
 * </PRE>
 */
public class ThorWindow extends JFrame implements TableModelListener {
    private final ThorTable thorTable;
    private @NotNull final DatabaseTableModel databaseTableModel;

    public ThorWindow(ReversiWindow reversiWindow, ReversiData reversiData, @NotNull DatabaseTableModel dd) {
        super("Database");
        databaseTableModel = dd;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        thorTable = new ThorTable(reversiWindow, reversiData, dd);
        add(thorTable);
        pack();
        final TableModel tableModel = thorTable.getTableModel();
        tableModel.addTableModelListener(this);
    }

    /**
     * @return a pointer to the database data
     */
    public DatabaseTableModel PD() {
        return thorTable.PD();
    }

    /**
     * Invalidate the data area. If all data files have been loaded, show the window
     */
    void ShowIfReady() {
        thorTable.repaint();
        final boolean isReady = PD().IsReady();
        if (isReady && !isVisible()) {
            pack();
            setVisible(true);
        }
    }

    public @NotNull DatabaseTableModel PDD() {
        return databaseTableModel;
    }

    public void tableChanged(TableModelEvent e) {
        setTitle("Database (" + databaseTableModel.getStatusString() + ")");
        ShowIfReady();
    }
}
