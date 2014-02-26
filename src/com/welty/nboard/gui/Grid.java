package com.welty.nboard.gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;

/**
 * A window containing a scroll pane and table; The table is row-selectable and the columns are right-aligned.
 * <PRE>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 25, 2009
 * Time: 7:24:39 PM
 * </PRE>
 */
public abstract class Grid extends JScrollPane {
    private final JTable table;

    /**
     * Create a grid
     *
     * @param tableModel model for the table; the table will be created
     */
    protected Grid(GridTableModel tableModel) {
        this(tableModel, new JTable(tableModel));
    }

    /**
     * Create a grid with row selections enabled, column selections disabled (i.e. select a whole row of the table).
     * Clicks are processed only if the row changes
     */
    private Grid(GridTableModel tableModel, JTable table) {
        this(tableModel, table, true, false, false);
    }

    /**
     * Create a grid with selection enabled for rows, columns, both (cell) or neither
     * Clicks are processed only if the selection changes, e.g. for row selection only if the row changes.
     */
    protected Grid(GridTableModel tableModel, JTable table, boolean rowSelectionAllowed, boolean columnSelectionAllowed, boolean leaveSpaceForScrollBars) {
        super(table);
        this.table = table;
        table.setFillsViewportHeight(true);

        table.setRowSelectionAllowed(rowSelectionAllowed);
        table.setColumnSelectionAllowed(columnSelectionAllowed);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // set up selection processing
        final SelectionListener listener = new SelectionListener();
        final TableColumnModel columns = table.getColumnModel();
        if (rowSelectionAllowed) {
            table.getSelectionModel().addListSelectionListener(listener);
        }
        if (columnSelectionAllowed) {
            columns.getSelectionModel().addListSelectionListener(listener);
        }
        table.addMouseListener(new MyMouseAdapter(table));

        final JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);

        table.setShowGrid(false);

        int gridWidth = 5; // looks better with a little space to the right

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            final int width = tableModel.getColumnWidth(i);
            final TableColumn column = columns.getColumn(i);
            column.setMinWidth(width);
            column.setMaxWidth(width);
            column.setPreferredWidth(width);

            final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            //noinspection MagicConstant
            renderer.setHorizontalAlignment(tableModel.getColumnSwingAlignment(i));

            column.setCellRenderer(renderer);
            gridWidth += width;
        }
        if (leaveSpaceForScrollBars) {
            table.setPreferredScrollableViewportSize(new Dimension(gridWidth, 461));
        } else {
            setPreferredSize(new Dimension(gridWidth, 461));
        }
    }

    protected void setSelectedRange(int row0, int row1, int col0, int col1) {
        table.getColumnModel().getSelectionModel().setSelectionInterval(col0, col1);
        table.getSelectionModel().setSelectionInterval(row0, row1);
    }

    /**
     * The grid selection has changed. Handle it.
     *
     * @param modelRow selection row
     * @param modelCol selection column
     */
    protected void selectionChanged(int modelRow, int modelCol) {
    }

    protected JTable getTable() {
        return table;
    }

    public TableModel getTableModel() {
        return table.getModel();
    }

    protected void disableAllKeys() {
        table.setFocusable(false);
    }

    private class SelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            final int viewRow = table.getSelectedRow();
            final int viewCol = table.getSelectedColumn();
            if (viewRow >= 0 && viewCol >= 0) {
                final int modelRow = table.convertRowIndexToModel(viewRow);
                final int modelCol = table.convertColumnIndexToModel(viewCol);
                selectionChanged(modelRow, modelCol);
            }
        }
    }

    public class AsDoubleSort implements Comparator<String> {
        public int compare(String a, String b) {
            final double va = value(a);
            final double vb = value(b);
            return Double.compare(va, vb);
        }

        private double value(String a) {
            try {
                return Double.parseDouble(a);
            } catch (NumberFormatException e) {
                return Double.NEGATIVE_INFINITY;
            }
        }
    }

    /**
     * The mouse was clicked in the given grid location
     * <p/>
     * To convert view row to model row, use table.convertRowIndexToModel(viewRow);
     * likewise for cols
     *
     * @param viewRow table row the mouse was clicked in, or -1 if the user clicked outside the row range.
     * @param viewCol table column the mouse was clicked in, or -1 if the user clicked outside the viewCol range.
     */
    protected void onMouseClick(int viewRow, int viewCol) {
    }

    private class MyMouseAdapter extends MouseAdapter {
        private final JTable jTable;

        public MyMouseAdapter(JTable jTable) {
            this.jTable = jTable;
        }

        @Override public void mousePressed(MouseEvent e) {
            final int viewRow = jTable.rowAtPoint(e.getPoint());
            final int viewCol = jTable.columnAtPoint(e.getPoint());
            onMouseClick(viewRow, viewCol);
        }
    }


}
