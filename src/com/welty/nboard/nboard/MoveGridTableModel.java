package com.welty.nboard.nboard;

import com.orbanova.common.misc.Require;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.thor.DatabaseData;
import com.welty.nboard.thor.ThorSummaryData;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMove;
import gnu.trove.list.array.TIntArrayList;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.Map;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 14, 2009
 * Time: 7:10:25 PM
 * </PRE>
 */
class MoveGridTableModel extends GridTableModel implements TableModelListener {
    private final TIntArrayList m_moves = new TIntArrayList();
    private final DatabaseData pdd;
    private final Hints m_hints;

    public MoveGridTableModel(DatabaseData pdd, Hints m_hints) {
        super(MoveGrid.columns);
        this.pdd = pdd;
        pdd.addTableModelListener(this);
        this.m_hints = m_hints;
    }

    /**
     * The hints structure has been updated. Determine whether to show evals and update the move grid accordingly
     */
    void UpdateHints() {
        m_moves.clear();
        for (Map.Entry<Byte, Hint> it : m_hints.Map().entrySet()) {
            m_moves.add(it.getKey());
        }
        for (int key : pdd.m_summary.keys()) {
            if (m_hints.Map().get((byte) key) == null)
                m_moves.add(key);
        }
        this.fireTableDataChanged();
    }

    public int getRowCount() {
        return m_moves.size();
    }

    public Object getValueAt(int item, int field) {
        final int sq = m_moves.get(item);

        if (field == 0)
            return new CMove((byte) sq).toString();
        else if (field < 6)
            return OutputHintText(sq, field);
        else
            return OutputSummaryText(sq, field);


    }

    public OsMove getMove(int row) {
        Require.lt(row, "row", getRowCount());
        CMove mv = new CMove((byte) m_moves.get(row));
        return mv.toOsMove();
    }

    /**
     * get the hint text for the move
     */
    String OutputHintText(int sq, int col) {
        Hint hint = m_hints.Map().get((byte) sq);
        if (hint == null) {
            return "";
        }
        switch (col) {
            case 1:
                return formatEval(hint.VNeutral());
            case 2:
                return formatEval(hint.vBlack);
            case 3:
                return formatEval(hint.vWhite);
            case 4:
                return "" + hint.nGames;
            case 5:
                return hint.sPly;
            default:
                throw new IllegalArgumentException("illegal field : " + col);
        }
    }

    private static String formatEval(float value) {
        if (Float.isNaN(value)) {
            return "";
        } else {
            return String.format("%+.2f", value);
        }
    }

    /**
     * Write the text of the summary field to the StringBuilder
     */
    String OutputSummaryText(int sq, int field) {
        ThorSummaryData it = pdd.m_summary.get(sq);
        if (it == null) {
            return "";
        }
        switch (field) {
            case 6:
                return "" + it.getNPlayed();
            case 7:
                return ((int) (it.getScore() * 100)) + "%";
            default:
                throw new IllegalArgumentException("Can't have field id = " + field);
        }
    }

    @Override public void tableChanged(TableModelEvent e) {
        UpdateHints();
    }
}
