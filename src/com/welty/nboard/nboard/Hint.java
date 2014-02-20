package com.welty.nboard.nboard;

import com.welty.othello.protocol.Depth;
import com.welty.othello.protocol.Value;

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

    final float vBlack;    //!< Value in "draw to black" mode
    final float vWhite;    //!< Value in "draw to white" mode
    final int nGames;        //!< Number of games in book after this move
    final Depth depth;    //!< Search depth, as text.
    final boolean fBook;        //!< True if this node was in book

    /**
     * Construct a hint.
     *
     * @param eval       Evaluation. Either a single float, or two floats separated by commas (if draw-to-white differs from draw-to-black)
     * @param nGames     number of games played in this line, if a book move
     * @param depth      search depth used to create the evaluation. Must start with an integer, but does not need to be completely integral, for example "100%" is ok.
     * @param fromBook   if true, the move came from the engine's book
     * @param fBlackMove if true, the root position has black to move
     */
    public Hint(Value eval, int nGames, Depth depth, boolean fromBook, boolean fBlackMove) {
        fBook = fromBook;
        this.depth = depth;
        this.nGames = nGames;
        if (fBlackMove) {
            vBlack = eval.drawSeekingValue;
            vWhite = eval.drawAvoidingValue;
        } else {
            vWhite = eval.drawSeekingValue;
            vBlack = eval.drawAvoidingValue;
        }
    }

    /**
     * @return the value with contempt=0, or Float.NaN if the value does not exist.
     */
    float VNeutral() {
        if (Float.isNaN(vBlack)) {
            return vBlack;
        }
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
     * @return true if the position is solved exactly (disk differential is proven)
     */
    boolean isExact() {
        return depth.isExact() || (depth.isWldProven() && VNeutral() == 0);
    }
}

