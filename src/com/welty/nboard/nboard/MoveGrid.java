package com.welty.nboard.nboard;

import com.welty.nboard.gui.*;
import com.welty.nboard.thor.DatabaseTableModel;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Displays the available moves, their evaluation, and # of games or search depth.
 */
public class MoveGrid extends Grid {

    private final ReversiData reversiData;

    static final GridColumn[] columns = {
            new GridColumn(37, "Move", Object.class, Align.CENTER),
            new GridColumn(50, "D=0"),
            new GridColumn(50, "DtW"),
            new GridColumn(50, "DtB"),
            new GridColumn(50, "#Games"),
            new GridColumn(40, "Depth"),
            new GridColumn(120, "Principal Variation", Align.LEFT),
            new GridColumn(55, "Played"),
            new GridColumn(50, "Win %")

    };

    MoveGrid(ReversiData reversiData, DatabaseTableModel m_pdd, Hints hints) {
        super(new MoveGridTableModel(m_pdd, hints));
        this.reversiData = reversiData;
        final JTable table = getTable();

        setPreferredSize(new Dimension(96, 200));

        table.setAutoCreateRowSorter(true);
        final DefaultRowSorter rowSorter = (DefaultRowSorter) table.getRowSorter();
        final AsDoubleSort comparator = new AsDoubleSort();
        for (int col = 1; col < columns.length; col++) {
            rowSorter.setComparator(col, comparator);
        }
        ArrayList<RowSorter.SortKey> sortKeys
                = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        rowSorter.setSortKeys(sortKeys);


        // make move column orange and Database area grey
        final TableColumnModel columnModel = table.getColumnModel();
        setColorColumn(columnModel, new Color(0xFF, 0xDF, 0xBF), 0);
        setColorColumn(columnModel, new Color(0xDF, 0xDF, 0xDF), 7);
        setColorColumn(columnModel, new Color(0xDF, 0xDF, 0xDF), 8);

        // register for the signal event when a new hint is added
        hints.m_seUpdate.Add(new SignalListener<ArrayList<Byte>>() {
            public void handleSignal(ArrayList<Byte> data) {
                getModel().UpdateHints();
            }
        });
        disableAllKeys();
    }

    private static void setColorColumn(TableColumnModel columnModel, Color backgroundColor, int column) {
        final ColorColumnRenderer renderer = new ColorColumnRenderer(backgroundColor, Color.blue);
        //noinspection MagicConstant
        renderer.setHorizontalAlignment(columns[column].align.getSwingValue());
        columnModel.getColumn(column).setCellRenderer(renderer);
    }

    private MoveGridTableModel getModel() {
        return ((MoveGridTableModel) getTable().getModel());
    }

    @Override protected void onMouseClick(int row, int col) {
        if (row >= 0) {
            final OsMove mv = getModel().getMove(row);
            reversiData.update(new OsMoveListItem(mv), true);
        }
    }
}

