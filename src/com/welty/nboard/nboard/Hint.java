package com.welty.nboard.nboard;

import com.welty.nboard.gui.SignalEvent;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.c.CReader;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsMoveListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Structure used by ReversiWindow and its MoveGrid to store the engine's evaluations.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 17, 2009
 * Time: 2:46:14 AM
 * To change this template use File | Settings | File Templates.
 */
class Hint {

    float vBlack;    //!< Value in "draw to black" mode
    float vWhite;    //!< Value in "draw to white" mode
    int nGames;        //!< Number of games in book after this move
    String sPly;    //!< Search depth, as text.
    int nPly;        //!< Search depth, first numerical component
    boolean fBook;        //!< True if this node was in book

    /**
     * @return the value with contempt=0
     */
    float VNeutral() {
        float vLow, vHigh;
        if (vBlack < vWhite) {
            vLow = vBlack;
            vHigh = vWhite;
        } else {
            vLow = vWhite;
            vHigh = vBlack;
        }
        if (vHigh <= 0)
            return vHigh;
        else if (vLow >= 0)
            return vLow;
        else
            return 0;
    }


    /**
     * @return true if the position is solved exactly
     */
    boolean IsExact() {
        return (sPly.equals("100%")) || (VNeutral() == 0 && sPly.equals("100%W"));
    }

    /**
     * Read the hint in from the program.
     *
     * @param is         istream to read the input from
     * @param fBlackMove true if black to move
     * @param fInBook    true if the move is from the opening book
     * @return is
     */
    CReader In(CReader is, boolean fBlackMove, boolean fInBook) {
        vBlack = vWhite = 0;
        fBook = fInBook;

        vBlack = is.readFloatNoExponent();
        if (is.peek() == ',') {
            is.ignore();
            vWhite = is.readFloatNoExponent();
            if (fBlackMove ^ (vBlack > vWhite)) {
                final float temp = vBlack;
                vBlack = vWhite;
                vWhite = temp;
            }
        } else {
            vWhite = vBlack;
        }
        nGames = is.readInt(0);
        sPly = is.readString();
        CReader isPly = new CReader(sPly);
        nPly = isPly.readInt(0);

        return is;
    }
}

/**
 * The set of all hints for the current position
 */
class Hints extends HashMap<Byte, Hint> implements SignalListener<COsMoveListItem> {
    final HashMap<Byte, Hint> Map() {
        return this;
    }

    /**
     * Fires when a squares are added, removed, or gone old. Data = modified squares
     */
    final SignalEvent<ArrayList<Byte>> m_seUpdate = new SignalEvent<>();

    String m_sPlyRecent;


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
     * @param pv         principal variation. (first two characters assumed to be the move)
     * @param is         istream to read the Hint from
     * @param fBlackMove true if black to move
     * @param fInBook    true if the move is from the opening book
     *                   <p/>
     */
    void Add(String pv, CReader is, boolean fBlackMove, boolean fInBook) {
        CMove move = new CMove(pv.substring(0, 2));

        Hint h = new Hint();
        h.In(is, fBlackMove, fInBook);
        if (!fInBook && !h.sPly.equals(m_sPlyRecent)) {
            RemoveNonbookHints(m_sPlyRecent);
            m_sPlyRecent = h.sPly;
            OldNonbookHints(h.sPly);
        }
        put((byte) move.Square(), h);

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
    void RemoveNonbookHints(final String sPly) {
        ArrayList<Byte> squaresRemoved = calcOldHints(sPly);

        for (Byte sq : squaresRemoved) {
            remove(sq);
        }

        if (!squaresRemoved.isEmpty())
            m_seUpdate.Raise(squaresRemoved);
    }

    private ArrayList<Byte> calcOldHints(String sPly) {
        ArrayList<Byte> squaresRemoved = new ArrayList<>();

        for (Map.Entry<Byte, Hint> it : entrySet()) {
            if (!it.getValue().fBook && !it.getValue().sPly.equals(sPly)) {
                squaresRemoved.add(it.getKey());
            }
        }
        return squaresRemoved;
    }

    /**
     * Raise the Old event for all nonbook hints with hint.sPly!=sPly
     */
    void OldNonbookHints(final String sPly) {
        ArrayList<Byte> old = calcOldHints(sPly);

        if (!old.isEmpty())
            m_seUpdate.Raise(old);
    }

    /**
     * @return true if some of the hints are from an opening book
     *         <p/>
     *         Things are displayed slightly differently if one if the hints is from an opening book
     */
    boolean HasBookHint() {
        for (Hint it : values()) {
            if (it.fBook)
                return true;
        }
        return false;
    }

    public void handleSignal(COsMoveListItem data) {
        Clear();
    }
}
