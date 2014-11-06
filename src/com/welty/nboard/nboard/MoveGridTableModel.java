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

package com.welty.nboard.nboard;

import com.orbanova.common.misc.Require;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.thor.DatabaseTableModel;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.thor.ThorSummaryData;
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
    private final DatabaseTableModel pdd;
    private final Hints m_hints;

    public MoveGridTableModel(DatabaseTableModel pdd, Hints m_hints) {
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
        for (int key : pdd.summary.keys()) {
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
        else if (field < 7) {
            return OutputHintText(sq, field - 1);
        } else {
            return OutputSummaryText(sq, field - 7);
        }
    }

    public OsMove getMove(int row) {
        Require.lt(row, "row", getRowCount());
        CMove mv = new CMove((byte) m_moves.get(row));
        return mv.toOsMove();
    }

    /**
     * get the hint text for the move
     *
     * @param field field number, from 0 to 5.
     */
    String OutputHintText(int sq, int field) {
        Hint hint = m_hints.Map().get((byte) sq);
        if (hint == null) {
            return "";
        }
        switch (field) {
            case 0:
                return formatEval(hint.VNeutral());
            case 1:
                return formatEval(hint.vBlack);
            case 2:
                return formatEval(hint.vWhite);
            case 3:
                return "" + hint.nGames;
            case 4:
                return hint.depth.toString();
            case 5:
                // prepend with a space to make it look nicer
                return " " + hint.principalVariation;
            default:
                throw new IllegalArgumentException("illegal field : " + field);
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
     *
     * @param field, from 0 to 1.
     */
    String OutputSummaryText(int sq, int field) {
        ThorSummaryData it = pdd.summary.get(sq);
        if (it == null) {
            return "";
        }
        switch (field) {
            case 0:
                return "" + it.getNPlayed();
            case 1:
                return ((int) (it.getScore() * 100)) + "%";
            default:
                throw new IllegalArgumentException("Can't have field id = " + field);
        }
    }

    @Override public void tableChanged(TableModelEvent e) {
        UpdateHints();
    }
}
