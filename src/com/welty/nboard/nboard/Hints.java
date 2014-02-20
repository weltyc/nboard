package com.welty.nboard.nboard;

import com.welty.nboard.gui.SignalEvent;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.Depth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The set of all hints for the current position
 */
public class Hints extends HashMap<Byte, Hint> implements SignalListener<OsMoveListItem> {
    final HashMap<Byte, Hint> Map() {
        return this;
    }

    /**
     * Fires when a squares are added, removed, or gone old. Data = modified squares
     */
    final SignalEvent<ArrayList<Byte>> m_seUpdate = new SignalEvent<>();

    /**
     * Depth of the hint received most recently from the engine
     */
    Depth lastDepth;


    /**
     * @return the best neutral value of all hints
     *         <p/>
     *         If there are no hints return -10000
     */
    float VBest() {
        float vBest = -10000;
        for (Hint it : values()) {
            float v = it.VNeutral();
            if (v > vBest)
                vBest = v;
        }
        return vBest;
    }

    /**
     * read in a hint, and add the pv-move/hint pair to hints
     * <p/>
     * istream should contain a hint in the format:
     * <p/>
     * {value} {nGames} {ply}
     * <p/>
     * - value value (optionally, {vBlack},{vWhite})
     * <p/>
     * - nGames number of games in book
     * <p/>
     * - ply search depth
     *
     * @param move move that is being valued
     * @param hint hint that was created
     */
    void Add(CMove move, Hint hint) {
        if (!hint.fBook && !hint.depth.equals(lastDepth)) {
            RemoveNonbookHints(lastDepth);
            lastDepth = hint.depth;
            OldNonbookHints(hint.depth);
        }
        put((byte) move.Square(), hint);

        ArrayList<Byte> added = new ArrayList<>();
        added.add((byte) move.Square());

        m_seUpdate.Raise(added);
    }

    /**
     * Remove all hints from the hint list. Raise the Remove signal (unless there were no hints to begin with)
     */
    void Clear() {
        ArrayList<Byte> squaresRemoved = new ArrayList<>();

        for (Byte it : keySet()) {
            squaresRemoved.add(it);
        }
        clear();

        if (!squaresRemoved.isEmpty())
            m_seUpdate.Raise(squaresRemoved);
    }

    /**
     * Remove non-book hints from the hint list. Raise the Remove signal.
     */
    void RemoveNonbookHints() {
        ArrayList<Byte> squaresRemoved = new ArrayList<>();

        for (Map.Entry<Byte, Hint> it : entrySet()) {
            if (!it.getValue().fBook) {
                squaresRemoved.add(it.getKey());
            }
        }

        for (Byte sq : squaresRemoved) {
            remove(sq);
        }

        if (!squaresRemoved.isEmpty())
            m_seUpdate.Raise(squaresRemoved);
    }

    /**
     * Remove all nonbook hints with hint.sPly!=sPly
     */
    void RemoveNonbookHints(final Depth aDepth) {
        final ArrayList<Byte> squaresRemoved = calcOldHints(aDepth);

        for (Byte sq : squaresRemoved) {
            remove(sq);
        }

        if (!squaresRemoved.isEmpty())
            m_seUpdate.Raise(squaresRemoved);
    }

    private ArrayList<Byte> calcOldHints(Depth aDepth) {
        ArrayList<Byte> squaresRemoved = new ArrayList<>();

        for (Map.Entry<Byte, Hint> it : entrySet()) {
            if (!it.getValue().fBook && !it.getValue().depth.equals(aDepth)) {
                squaresRemoved.add(it.getKey());
            }
        }
        return squaresRemoved;
    }

    /**
     * Raise the Old event for all nonbook hints with hint.sPly!=sPly
     */
    void OldNonbookHints(final Depth aDepth) {
        ArrayList<Byte> old = calcOldHints(aDepth);

        if (!old.isEmpty())
            m_seUpdate.Raise(old);
    }

    /**
     * Determine if some of the hints are from an opening book.
     * <p/>
     * Things are displayed slightly differently if one of the hints is from an opening book
     *
     * @return true if some of the hints are from an opening book
     */
    boolean HasBookHint() {
        for (Hint it : values()) {
            if (it.fBook)
                return true;
        }
        return false;
    }

    public void handleSignal(OsMoveListItem data) {
        Clear();
    }
}
