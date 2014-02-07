package com.welty.nboard.nboard;

import com.welty.nboard.gui.*;
import com.welty.nboard.thor.DatabaseData;
import com.welty.othello.gdk.COsMove;
import com.welty.othello.gdk.COsMoveListItem;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Displays the available moves, their evaluation, and # of games or search depth.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 26, 2009
 * Time: 11:06:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveGrid extends Grid {

    private final ReversiData m_pd;

    static final GridColumn[] columns = {
            new GridColumn(37, "Move", Align.CENTER),
            new GridColumn(50, "D=0"),
            new GridColumn(50, "DtW"),
            new GridColumn(50, "DtB"),
            new GridColumn(50, "#Games"),
            new GridColumn(40, "Depth"),
            new GridColumn(55, "Played"),
            new GridColumn(50, "Win %"),

    };

    MoveGrid(ReversiData m_pd, DatabaseData m_pdd, Hints hints) {
        super(new MoveGridTableModel(m_pdd, hints));
        this.m_pd = m_pd;
        final JTable table = getTable();

        setPreferredSize(new Dimension(96, 200));

        table.setAutoCreateRowSorter(true);
        final DefaultRowSorter rowSorter = (DefaultRowSorter) table.getRowSorter();
        final AsDoubleSort comparator = new AsDoubleSort();
        for (int col = 1; col < columns.length; col++) {
            rowSorter.setComparator(col, comparator);
        }
        ArrayList<RowSorter.SortKey> sortKeys
                = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        rowSorter.setSortKeys(sortKeys);


        // make move column orange and Thor area grey
        final TableColumnModel columnModel = table.getColumnModel();
        setColorColumn(columnModel, new Color(0xFF, 0xEF, 0xDF), 0);
        setColorColumn(columnModel, new Color(0xEF, 0xEF, 0xEF), 6);
        setColorColumn(columnModel, new Color(0xEF, 0xEF, 0xEF), 7);

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
        renderer.setHorizontalAlignment(columns[column].align.getSwingValue());
        columnModel.getColumn(column).setCellRenderer(renderer);
    }

    private MoveGridTableModel getModel() {
        return ((MoveGridTableModel) getTable().getModel());
    }


    /**
     * The mouseclick implementation sends the move as a message to the desktop window.
     */
    public void MouseDataClick(int row, int col) {
        MoveGridTableModel model = getModel();
        COsMove mv = model.getMove(row);
        m_pd.Update(new COsMoveListItem(mv), true);
    }

    public void UpdateHints() {
        getModel().UpdateHints();
    }


}

