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

import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JSwingBuilder;
import com.welty.nboard.nboard.ReversiData;
import com.welty.nboard.nboard.ReversiWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;

/**
 * Displays the games in tabular format
 */
class DatabaseWindow extends JFrame implements TableModelListener {
    private final DatabaseTable table;
    private @NotNull final DatabaseTableModel tableModel;

    public DatabaseWindow(ReversiWindow reversiWindow, ReversiData reversiData, @NotNull DatabaseTableModel tableModel) {
        super("Database");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        this.tableModel = tableModel;
        table = new DatabaseTable(reversiWindow, reversiData, tableModel);

        add(JSwingBuilder.vBox(1,
                createFilterBoxes(),
                table
        )
        );
        pack();

        tableModel.addTableModelListener(this);
    }

    private Grid<Component> createFilterBoxes() {
        final Grid<Component> filterBoxes = JSwingBuilder.hBox();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            final JTextField input = createFilterBox(i);
            filterBoxes.add(input);
        }
        return filterBoxes;
    }

    private JTextField createFilterBox(int field) {
        final JTextField input = new JTextField();
        final int width = tableModel.getColumnWidth(field);
        input.setPreferredSize(new Dimension(width, 24));
        input.setFont(table.getFont());
        input.getDocument().addDocumentListener(new FilterBoxListener(tableModel, field, input));
        return input;
    }

    /**
     * Invalidate the data area. If all data files have been loaded, show the window
     */
    void ShowIfReady() {
        table.repaint();
        final boolean isReady = tableModel.isReady();
        if (isReady && !isVisible()) {
            pack();
            setVisible(true);
        }
    }

    public void tableChanged(TableModelEvent e) {
        setTitle("Database (" + tableModel.getStatusString() + ")");
        ShowIfReady();
    }

    private static class FilterBoxListener implements DocumentListener {
        private final DatabaseTableModel dtm;
        private final int field;
        private final JTextField input;

        private FilterBoxListener(DatabaseTableModel dtm, int field, JTextField input) {
            this.dtm = dtm;
            this.field = field;
            this.input = input;
        }

        @Override public void insertUpdate(DocumentEvent e) {
            handle();
        }

        @Override public void removeUpdate(DocumentEvent e) {
            handle();
        }

        @Override public void changedUpdate(DocumentEvent e) {
            handle();
        }

        private void handle() {
            dtm.setFilter(input.getText(), field);
        }
    }
}
