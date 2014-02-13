package com.welty.nboard.thor;

import com.welty.nboard.gui.Align;
import com.welty.nboard.gui.GridColumn;
import com.welty.nboard.gui.GridTableModel;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.nboard.BoardSource;
import com.welty.nboard.nboard.OptionSource;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsPosition;
import com.welty.othello.gdk.OsBoard;
import com.welty.othello.gdk.OsMoveListItem;
import gnu.trove.list.array.TIntArrayList;
import org.jetbrains.annotations.NotNull;

import static com.welty.nboard.thor.Thor.MatchingPositions;

/**
 * Encapsulate all data needed by the database window
 */
public class DatabaseTableModel extends GridTableModel {
    private final DatabaseData databaseData = new DatabaseData();

    private static final GridColumn[] m_columns = {
            new GridColumn(120, "Black", Align.LEFT),
            new GridColumn(120, "White", Align.LEFT),
            new GridColumn(50, "Year", Align.RIGHT),
            new GridColumn(150, "Place", Align.LEFT),
            new GridColumn(50, "Result", Align.RIGHT),
            new GridColumn(110, "Opening", Align.LEFT),
    };
    private final @NotNull OptionSource optionSource;
    private final @NotNull BoardSource boardSource;

    public DatabaseTableModel(@NotNull OptionSource optionSource, @NotNull BoardSource boardSource) {
        super(m_columns);
        this.optionSource = optionSource;
        this.boardSource = boardSource;
        for (int i = 0; i < filters.length; i++) {
            filters[i] = "";
        }
        boardSource.addListener(new SignalListener<OsMoveListItem>() {
            public void handleSignal(OsMoveListItem data) {
                OnBoardChanged();
            }
        });
        databaseData.addListener(new DatabaseData.Listener() {
            @Override public void databaseChanged() {
                LookUpPosition();
            }
        });

    }

    void OnBoardChanged() {
        if (optionSource.ThorLookUpAll()) {
            LookUpPosition();
        } else {
            m_summary.clear();
            fireTableDataChanged();
        }
    }

    // Filtering
    public TThorSummary m_summary = new TThorSummary();

    /**
     * List of games that match the displayed position.
     * <p/>
     * The int is an index into m_tgis.
     */
    private TIntArrayList matchingIndices = new TIntArrayList();
    private final static int nFields = 6;

    /**
     * Text that must match the given field in order to display the position
     */
    private final String[] filters = new String[nFields];


    /**
     * @return true if the database window should be displayed.
     */
    public boolean IsReady() {
        return nGamesInDatabase() != 0;
    }

    /**
     * @return the total number of games loaded (both Thor and GGF)
     */
    public int nGamesInDatabase() {
        return databaseData.NGames();
    }

    /**
     * @param iFiltered model row, i.e. index into the list of displayed games
     * @return a game in GGS/os format.
     */
    public COsGame GameFromFilteredRow(int iFiltered) {
        return databaseData.GameFromIndex(matchingIndices.get(iFiltered));
    }

    /**
     * Look up the position and notify listeners that the table data has changed.
     */
    public void LookUpPosition() {
        final COsPosition position = boardSource.DisplayedPosition();
        LookUpPosition(position.board);
    }

    /**
     * Look up a position in the database, filter, set summary, and signal that this has been done.
     */
    public void LookUpPosition(final OsBoard pos) {
        if (pos.nEmpty() > 3) {
            // look up position
            final MatchingPositions matchingPositions = databaseData.findMatchingPositions(pos);
            matchingIndices = matchingPositions.index;
            final TIntArrayList iReflections = matchingPositions.iReflections;

            // filter
            final int n = matchingIndices.size();
            TIntArrayList fi = new TIntArrayList();
            TIntArrayList fir = new TIntArrayList();
            for (int i = 0; i < n; i++) {
                final int j = matchingIndices.get(i);
                if (FilterOk(j)) {
                    fi.add(j);
                    fir.add(iReflections.get(i));
                }
            }

            // set summary
            m_summary = databaseData.summarize(pos, fi, fir);
        } else {
            // for performance reasons, don't look up games with very small number of empties.
            matchingIndices.clear();
            m_summary.clear();
        }
        fireTableDataChanged();
    }

    /**
     * Alter the data's filter (e.g. filter by player name) and signal that this has been done.
     * <p/>
     *
     * @param field column index. Must be in the range [0,5]
     */
    void SetFilter(String text, int field) {
        assert (field < 6);
        if (field < 6) {
            filters[field] = text;
            LookUpPosition();
        }
    }

    /**
     * @return true if the item matches all filters set in the filter window
     */
    boolean FilterOk(int item) {
        for (int field = 0; field < nFields; field++) {
            String sFilter = filters[field];
            if (!sFilter.isEmpty() && !sFilter.equals(databaseData.GameItemText(item, field)))
                return false;
        }
        return true;
    }

    /**
     * @return result of the game, #black discs - #white discs, for Thor games only
     */
    int GameResult(int iGame) {
        return databaseData.GameResult(iGame);
    }

    /**
     * @return year in which the game was played
     */
    int GameYear(int iGame) {
        return databaseData.getGameYear(iGame);
    }

    public int getRowCount() {
        return matchingIndices.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return databaseData.GameItemText(matchingIndices.get(rowIndex), columnIndex);
    }

    public String getStatusString() {
        return getRowCount() + "/" + nGamesInDatabase() + " games selected";
    }

    public String GameItemText(int item, int field) {
        return databaseData.GameItemText(item, field);
    }

    DatabaseData getDatabase() {
        return databaseData;
    }
}
