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

import com.welty.nboard.gui.Align;
import com.welty.nboard.gui.GridColumn;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.nboard.BoardSource;
import com.welty.nboard.nboard.OptionSource;
import com.welty.othello.gdk.COsBoard;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsPosition;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.thor.DatabaseData;
import com.welty.othello.thor.ThorSummary;
import gnu.trove.list.array.TIntArrayList;
import org.jetbrains.annotations.NotNull;

import static com.welty.othello.thor.Thor.MatchingPositions;

/**
 * Encapsulate all data needed by the database gui
 */
public class DatabaseTableModel extends GridTableModel {
    /**
     * Column number for year data
     */
    public static final int YEAR = 2;

    private final DatabaseData databaseData;

    private static final GridColumn[] columns = {
            new GridColumn(120, "Black", Align.LEFT),
            new GridColumn(120, "White", Align.LEFT),
            new GridColumn(50, "Year", Align.RIGHT),
            new GridColumn(150, "Place", Align.LEFT),
            new GridColumn(50, "Result", Align.RIGHT),
            new GridColumn(110, "Opening", Align.LEFT),
    };
    private final @NotNull OptionSource optionSource;
    private final @NotNull BoardSource boardSource;

    /**
     * Summary data for moves
     */
    public ThorSummary summary = new ThorSummary();

    /**
     * List of games that match the displayed position and pass the filters.
     * <p/>
     * The int is an index into DatabaseData.GameItemText()
     */
    private TIntArrayList matchingIndices = new TIntArrayList();

    /**
     * Text that must match the given field in order to display the position
     */
    private final String[] filters = new String[columns.length];


    DatabaseTableModel(@NotNull OptionSource optionSource, @NotNull BoardSource boardSource, @NotNull DatabaseData databaseData) {
        super(columns);
        this.optionSource = optionSource;
        this.boardSource = boardSource;
        this.databaseData = databaseData;
        for (int i = 0; i < filters.length; i++) {
            filters[i] = "";
        }
        boardSource.addListener(new SignalListener<OsMoveListItem>() {
            public void handleSignal(OsMoveListItem data) {
                onBoardChanged();
            }
        });
        databaseData.addListener(new DatabaseData.Listener() {
            @Override public void databaseChanged() {
                lookUpPosition();
            }
        });

    }

    void onBoardChanged() {
            lookUpPosition();
    }

    /**
     * @return true if the database window should be displayed.
     */
    public boolean isReady() {
        return nGamesInDatabase() != 0;
    }

    /**
     * @return the total number of games loaded (both Thor and GGF)
     */
    public int nGamesInDatabase() {
        return databaseData.NGames();
    }

    /**
     * @param row model row, i.e. index into the list of displayed games
     * @return a game in GGS/os format.
     */
    public COsGame gameFromRow(int row) {
        return databaseData.GameFromIndex(matchingIndices.get(row));
    }

    /**
     * Look up the position and notify listeners that the table data has changed.
     */
    public void lookUpPosition() {
        final COsPosition position = boardSource.DisplayedPosition();
        lookUpPosition(position.board);
    }

    /**
     * Look up a position in the database, filter, set summary, and signal that this has been done.
     */
    public void lookUpPosition(final COsBoard pos) {
        if (pos.nEmpty() > 3) {
            // look up position
            final MatchingPositions matchingPositions = databaseData.findMatchingPositions(pos);
            final TIntArrayList positionMatches = matchingPositions.index;
            final TIntArrayList iReflections = matchingPositions.iReflections;

            // filter
            final int n = positionMatches.size();
            TIntArrayList fi = new TIntArrayList();
            TIntArrayList fir = new TIntArrayList();
            for (int i = 0; i < n; i++) {
                final int ddGameId = positionMatches.get(i);
                if (filterMatches(ddGameId)) {
                    fi.add(ddGameId);
                    fir.add(iReflections.get(i));
                }
            }
            this.matchingIndices = fi;

            // set summary
            summary = databaseData.summarize(pos, fi, fir);
        } else {
            // for performance reasons, don't look up games with very small number of empties.
            matchingIndices.clear();
            summary.clear();
        }
        fireTableDataChanged();
    }

    /**
     * Alter the data's filter (e.g. filter by player name) and signal that this has been done.
     * <p/>
     *
     * @param field column index. Must be in the range [0,5]
     */
    void setFilter(String text, int field) {
        System.out.println("setting filter " + field + " to " + text);
        assert (field < 6);
        if (field < 6) {
            filters[field] = text;
            lookUpPosition();
        }
    }

    /**
     * @return true if the item matches all filters set in the filter window
     */
    boolean filterMatches(int item) {
        for (int field = 0; field < filters.length; field++) {
            String sFilter = filters[field];
            if (!sFilter.isEmpty() && !databaseData.GameItemText(item, field).startsWith(sFilter)) {
                return false;
            }
        }
        return true;
    }

    public int getRowCount() {
        return matchingIndices.size();
    }

    public String getValueAt(int rowIndex, int columnIndex) {
        return databaseData.GameItemText(matchingIndices.get(rowIndex), columnIndex);
    }

    public String getStatusString() {
        return getRowCount() + "/" + nGamesInDatabase() + " games selected";
    }
}
