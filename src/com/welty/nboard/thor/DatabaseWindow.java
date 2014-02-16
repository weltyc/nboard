package com.welty.nboard.thor;

import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JSwingBuilder;
import com.welty.nboard.nboard.ReversiData;
import com.welty.nboard.nboard.ReversiWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

        add(JSwingBuilder.vBox(
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
            final JTextField input = new JTextField();
            final int width = tableModel.getColumnWidth(i);
            input.setPreferredSize(new Dimension(width, 24));
            input.setFont(table.getFont());
            filterBoxes.add(input);
        }
        return filterBoxes;
    }

    /**
     * Invalidate the data area. If all data files have been loaded, show the window
     */
    void ShowIfReady() {
        table.repaint();
        final boolean isReady = tableModel.IsReady();
        if (isReady && !isVisible()) {
            pack();
            setVisible(true);
        }
    }

    public void tableChanged(TableModelEvent e) {
        setTitle("Database (" + tableModel.getStatusString() + ")");
        ShowIfReady();
    }
}
