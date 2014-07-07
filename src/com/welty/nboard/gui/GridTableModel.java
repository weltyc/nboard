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

package com.welty.nboard.gui;

import javax.swing.table.AbstractTableModel;

/**
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
