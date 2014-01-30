package com.welty.nboard.nboard;

import com.welty.othello.c.CReader;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.nboard.gui.SignalEvent;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.core.CMove;

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
        CReader isply = new CReader(sPly);
        nPly = isply.readInt(0);

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

    final SignalEvent<ArrayList<Byte>> m_seUpdate = new SignalEvent<ArrayList<Byte>>();    //!< Fires when a squares are added, removed, or gone old. Data = modified squares

    String m_sPlyRecent;


    /**
     * @return the best neutral value of all hints
     *         <p/>
     *         If there are no hints return -10000
     */
    float VBest() {
        float vbest = -10000;
        for (Hint it : values()) {
            float v = it.VNeutral();
            if (v > vbest)
                vbest = v;
        }
        return vbest;
    }

    /**
     * read in a pv and a hint, and add the pv-move/hint pair to hints
     *
     * @param is         istream to read the input from
     * @param fBlackMove true if black to move
     * @param fInBook    true if the move is from the opening book
     *                   <p/>
     *                   istream should contain a hint in the format:
     *                   <PV> <value> <nGames> <ply>
     *                   - PV (first two characters assumed to be the move)
     *                   - value value (optionally, <vBlack>,<vWhite>)
     *                   - nGames number of games in book
     *                   - ply search depth
     */
    void Add(CReader is, boolean fBlackMove, boolean fInBook) {
        // read in the pv. The first two characters will be the move.
        String pv = is.readString();
        CMove move = new CMove(pv.substring(0, 2));

        Hint h = new Hint();
        h.In(is, fBlackMove, fInBook);
        if (!fInBook && !h.sPly.equals(m_sPlyRecent)) {
            RemoveNonbookHints(m_sPlyRecent);
            m_sPlyRecent = h.sPly;
            OldNonbookHints(h.sPly);
        }
        put((byte) move.Square(), h);

        ArrayList<Byte> added = new ArrayList<Byte>();
        added.add((byte) move.Square());

        m_seUpdate.Raise(added);

        //return move;
    }

    /**
     * Remove all hints from the hint list. Raise the Remove signal (unless there were no hints to begin with)
     */
    void Clear() {
        ArrayList<Byte> squaresRemoved = new ArrayList<Byte>();

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
        ArrayList<Byte> squaresRemoved = new ArrayList<Byte>();

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
        ArrayList<Byte> squaresRemoved = new ArrayList<Byte>();

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
