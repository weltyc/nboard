package com.welty.nboard.nboard;

import com.welty.othello.c.CReader;

/**
 * Structure used by ReversiWindow and its MoveGrid to store the engine's evaluations.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 17, 2009
 * Time: 2:46:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class Hint {

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

