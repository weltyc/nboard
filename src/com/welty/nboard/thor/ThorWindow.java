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
    private ThorTable m_ptable;
    private @NotNull DatabaseData m_pdd;

    public ThorWindow(ReversiWindow pwTarget, ReversiData pd, DatabaseData dd) {
        super("Database");
        m_pdd = dd;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        m_ptable = new ThorTable(pwTarget, pd, dd);
        add(m_ptable);
        pack();
        final TableModel tableModel = m_ptable.getTableModel();
        tableModel.addTableModelListener(this);
    }

    /**
     * @return a pointer to the database data
     */
    public DatabaseData PD() {
        return m_ptable.PD();
    }

    /**
     * Invalidate the data area. If all data files have been loaded, show the window
     */
    void ShowIfReady() {
        m_ptable.repaint();
        final boolean isReady = PD().IsReady();
        if (isReady && !isVisible()) {
            pack();
            setVisible(true);
        }
    }

    public @NotNull DatabaseData PDD() {
        return m_pdd;
    }

    public void tableChanged(TableModelEvent e) {
        setTitle("Database (" + m_pdd.getStatusString() + ")");
        ShowIfReady();
    }
}
