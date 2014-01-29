package com.welty.nboard;

import com.orbanova.common.misc.Require;
import com.welty.othello.gdk.COsMove;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.thor.DatabaseData;
import com.welty.nboard.thor.ThorSummaryData;
import com.welty.othello.core.CMove;
import gnu.trove.list.array.TIntArrayList;

import java.util.Map;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 14, 2009
 * Time: 7:10:25 PM
 * </PRE>
 */
class MoveGridTableModel extends GridTableModel {
    private final TIntArrayList m_moves = new TIntArrayList();
    private final DatabaseData pdd;
    private final OptionSource optionSource;
    private final Hints m_hints;

    public MoveGridTableModel(DatabaseData pdd, OptionSource optionSource, Hints m_hints) {
        super(MoveGrid.columns);
        this.pdd = pdd;
        this.optionSource = optionSource;
        this.m_hints = m_hints;
    }

    /**
     * The hints structure has been updated. Determine whether to show evals and update the move grid accordingly
     */
    void UpdateHints() {
        UpdateHints(optionSource.ShowEvals());
    }

    /**
     * The hints structure has been updated. Update the movegrid accordingly.
     */
    void UpdateHints(boolean fShowEvals) {
        m_moves.clear();
        for (Map.Entry<Byte, Hint> it : m_hints.Map().entrySet()) {
            m_moves.add(it.getKey());
        }
        for (int key : pdd.m_summary.keys()) {
            if (m_hints.Map().get((byte) key) == null)
                m_moves.add(key);
        }
        // todo do we need this?
        // Sort();
        if (fShowEvals) {
            this.fireTableDataChanged();
        }
    }

    public int getRowCount() {
        return m_moves.size();
    }

    public Object getValueAt(int item, int field) {
        StringBuilder os = new StringBuilder();

        final int sq = m_moves.get(item);

        if (field == 0)
            os.append(new CMove((byte) sq).toString());
        else if (field < 6)
            OutputHintText(sq, field, os);
        else
            OutputSummaryText(sq, field, os);

        return os.toString();


    }

    public COsMove getMove(int row) {
        Require.lt(row, "row", getRowCount());
        CMove mv = new CMove((byte) m_moves.get(row));
        return mv.toOsMove();
    }

    /**
     * Write the text of the square's hint to the ostream
     */
    void OutputHintText(int sq, int field, StringBuilder os) {
        Hint hint = m_hints.Map().get((byte) sq);
        if (hint != null) {
            switch (field) {
                case 1:
                case 2:
                case 3:
                    if (field == 1) {
                        os.append(String.format("%+.2f", hint.VNeutral()));
                    } else if (field == 2) {
                        os.append(String.format("%+.2f", hint.vBlack));
                    } else {
                        os.append(String.format("%+.2f", hint.vWhite));
                    }
                    break;
                case 4:
                    os.append(hint.nGames);
                    break;
                case 5:
                    os.append(hint.sPly);
                    break;
                default:
                    throw new IllegalArgumentException("illegal field : " + field);
            }
        }
    }

    /**
     * Write the text of the summary field to the ostream
     */
    void OutputSummaryText(int sq, int field, StringBuilder os) {
        ThorSummaryData it = pdd.m_summary.get(sq);
        if (it != null) {
            switch (field) {
                case 6:
                    os.append(it.getNPlayed());
                    break;
                case 7:
                    os.append((int) (it.getScore() * 100)).append("%");
                    break;
                default:
                    throw new IllegalArgumentException("Can't have field id = " + field);
            }
        }
    }

}
