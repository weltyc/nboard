package com.welty.nboard.gui;

import javax.swing.table.AbstractTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 26, 2009
 * Time: 5:38:15 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class GridTableModel extends AbstractTableModel {
    private final GridColumn[] columns;

    public GridTableModel(GridColumn[] columns) {
        this.columns = columns;
    }

    public String getColumnName(int column) {
        return columns[column].name;
    }

    public int getColumnWidth(int column) {
        return columns[column].width;
    }

    public int getColumnCount() {
        return columns.length;
    }

    public int getColumnSwingAlignment(int column) {
        return columns[column].align.getSwingValue();
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
        return columns[columnIndex].columnClass;
    }
}
