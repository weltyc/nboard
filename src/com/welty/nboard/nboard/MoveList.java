package com.welty.nboard.nboard;

import com.welty.nboard.gui.Grid;
import com.welty.nboard.gui.GridColumn;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.COsMoveListItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Displays all moves played in the game, in the order that they were played
 * <p/>
 * Evaluations are also displayed, to the extent that they exist.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 21, 2009
 * Time: 12:08:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveList extends Grid {
    private final BoardSource m_pd;
    private static final GridColumn[] columns = {
            new GridColumn(30, "#"),
            new GridColumn(35, "Bk"),
            new GridColumn(50, "Eval"),
            new GridColumn(35, "Wt"),
            new GridColumn(50, "Eval")
    };

    MoveList(ReversiData pd) {
        this(pd, new MoveListTableModel(pd));
        disableAllKeys();
    }

    private MoveList(ReversiData pd, MoveListTableModel tableModel) {
        super(tableModel, new MoveListTable(tableModel), true, true, false);
        m_pd = pd;
        getTable().setColumnSelectionAllowed(true);

        m_pd.AddListener(new SignalListener<COsMoveListItem>() {
            public void handleSignal(COsMoveListItem data) {
                final int iMove = m_pd.IMove();
                final int col = field(iMove);
                final int row = item(iMove);
                setSelectedCell(row, col);
                repaint();
            }
        });
    }

    /**
     * Switch the displayed position to the one that the user clicked on.
     */
    public void MouseDataClick(int row, int col) {
        if (row >= 0) {
            // row < 0 is header row? maybe
            if ((col & 1) != 0) {
                int iMove = IMove(row, col);
                if (iMove <= m_pd.NMoves()) {
                    m_pd.SetIMove(iMove);
                }
            }
        }
    }

    private static int IMove(int item, int field) {
        return item * 2 + (field > 2 ? 1 : 0);
    }

    private static int item(int iMove) {
        return iMove >> 1;
    }

    private static int field(int iMove) {
        return ((iMove & 1) << 1) + 1;
    }

    private static class MoveListTableModel extends GridTableModel {
        private final BoardSource m_pd;

        public MoveListTableModel(ReversiData pd) {
            super(columns);
            m_pd = pd;
        }

        public int getRowCount() {
            return (m_pd.NMoves() + 2) / 2;
        }

        public Object getValueAt(int item, int field) {
            int iMove = item * 2;
            if (field == 0) {
                return (iMove + 1) + ".";
            }
            if (field > 2) {
                iMove++;
                field -= 2;
            }
            if (iMove < m_pd.NMoves()) {
                COsMoveListItem mli = m_pd.Game().ml.get(iMove);
                if (field == 1) {
                    return mli.mv.toString();
                } else {
                    return mli.dEval;

                }
            }
            return "";
        }
    }

    private static class MoveListTable extends JTable {
        @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
            if ((columnIndex & 1) == 1) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
            }
        }

        public MoveListTable(MoveListTableModel tableModel) {
            super(tableModel);
            setDefaultRenderer(Double.class, new EvalRenderer());
        }
    }

    private static class EvalRenderer extends DefaultTableCellRenderer {
        private static final DecimalFormat numberFormat = new DecimalFormat("#.0");

        @Override protected void setValue(Object value) {
            if (value == null) {
                super.setValue("");
                return;
            }
            final double v = (Double) value;
            if (v == 0) {
                super.setValue("");
            } else {
                super.setValue(numberFormat.format(v));
            }
            setHorizontalAlignment(JLabel.RIGHT);
            setForeground(v < 0 ? Color.RED : Color.BLACK);
        }
    }
}
