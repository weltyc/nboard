package com.welty.nboard.nboard;

import com.welty.nboard.gui.Grid;
import com.welty.nboard.gui.GridColumn;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.OsMoveListItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
    private final BoardSource boardSource;
    private final MoveListTableModel tableModel;

    private static final GridColumn[] columns = {
            new GridColumn(30, "#"),
            new GridColumn(35, "Bk"),
            new GridColumn(50, "Eval", Double.class),
            new GridColumn(35, "Wt"),
            new GridColumn(50, "Eval", Double.class)
    };

    MoveList(ReversiData pd) {
        this(pd, new MoveListTableModel(pd));
        disableAllKeys();
    }

    private MoveList(ReversiData pd, MoveListTableModel tableModel) {
        super(tableModel, new MoveListTable(tableModel), true, true, false);
        boardSource = pd;
        this.tableModel = tableModel;
        final JTable table = getTable();
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);


        boardSource.AddListener(new SignalListener<OsMoveListItem>() {
            public void handleSignal(OsMoveListItem data) {
                final int iMove = boardSource.IMove();
                final int col = field(iMove);
                final int row = item(iMove);
                setSelectedRange(row, row, col, col + 1);
                repaint();
            }
        });
    }

    /**
     * Switch the displayed position to the one that the user clicked on.
     */
    public void selectionChanged(int row, int col) {
    }

    @Override protected void onMouseClick(int row, int col) {
        System.out.println("mouse clicked at " + row + ", " + col);
        if (col >= 1) {
            final int iMove;
            if (row >= 0) {
                iMove = Math.min(boardSource.nMoves(), row * 2 + (col > 2 ? 1 : 0));
            } else {
                iMove = boardSource.nMoves();
            }
            boardSource.SetIMove(iMove);
        }
    }

    private static int item(int iMove) {
        return iMove >> 1;
    }

    private static int field(int iMove) {
        return ((iMove & 1) << 1) + 1;
    }

    private static class MoveListTableModel extends GridTableModel {
        private final BoardSource boardSource;

        public MoveListTableModel(BoardSource boardSource) {
            super(columns);
            this.boardSource = boardSource;
        }

        public int getRowCount() {
            return (boardSource.nMoves() + 2) / 2;
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
            if (iMove < boardSource.nMoves()) {
                final OsMoveListItem mli = boardSource.Game().getMli(iMove);
                if (field == 1) {
                    return mli.move.toString();
                } else {
                    return mli.hasEval() ? mli.getEval() : null;

                }
            }
            return null;
        }
    }

    private static class MoveListTable extends JTable {
        private static final EvalRenderer evalRenderer = new EvalRenderer();

        public MoveListTable(MoveListTableModel tableModel) {
            super(tableModel);

            // disable standard mouse selection
            for (MouseListener l : getMouseListeners()) {
                removeMouseListener(l);
            }
            for (MouseMotionListener l : getMouseMotionListeners()) {
                removeMouseMotionListener(l);
            }
        }

        @Override public TableCellRenderer getCellRenderer(int row, int column) {
            if (column == 2 || column == 4) {
                return evalRenderer;
            } else {
                return super.getCellRenderer(row, column);
            }
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
