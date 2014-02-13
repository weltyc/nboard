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
public class DatabaseWindow extends JFrame implements TableModelListener {
    private final DatabaseTable databaseTable;
    private @NotNull final DatabaseTableModel databaseTableModel;

    public DatabaseWindow(ReversiWindow reversiWindow, ReversiData reversiData, @NotNull DatabaseTableModel dd) {
        super("Database");
        databaseTableModel = dd;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        databaseTable = new DatabaseTable(reversiWindow, reversiData, dd);
        add(databaseTable);
        pack();
        final TableModel tableModel = databaseTable.getTableModel();
        tableModel.addTableModelListener(this);
    }

    /**
     * @return a pointer to the database data
     */
    public DatabaseTableModel PD() {
        return databaseTable.PD();
    }

    /**
     * Invalidate the data area. If all data files have been loaded, show the window
     */
    void ShowIfReady() {
        databaseTable.repaint();
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
