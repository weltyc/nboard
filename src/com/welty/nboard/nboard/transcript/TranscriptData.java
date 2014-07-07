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

package com.welty.nboard.nboard.transcript;

import com.orbanova.common.misc.ListenerManager;
import com.welty.novello.core.BitBoardUtils;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.gdk.OsMove;
import com.welty.othello.gdk.OsMoveListItem;

import java.util.ArrayList;
import java.util.List;

class TranscriptData extends ListenerManager<TranscriptData.Listener> {
    /**
     * Move number that has been typed into each square, or 0 if no move number has been typed in
     */
    private final int[] moveNumbers = new int[64];

    /**
     * Calculated and cached information about the current board state.
     * <p/>
     * Note: This is not automatically updated after a call to setMoveNumber() because the caller
     * may be only entering one digit of the moveNumber. Callers should call recalculate() after setMoveNUmber()
     * if they wish to recalculate.
     * <p/>
     * It is automatically updated after a call to clear().
     */
    private CalcResult calcResult;

    TranscriptData() {
        recalculate();
    }

    /**
     * Get the square number corresponding to a move
     *
     * @param moveNumber number of the move (1..64)
     * @return square number corresponding to a move, -1 if there is no such square number, -2 if there are multiple
     *         such square numbers.
     */
    int getSquare(int moveNumber) {
        int square = -1;
        for (int i = 0; i < 64; i++) {
            if (moveNumbers[i] == moveNumber) {
                if (square == -1) {
                    square = i;
                } else {
                    square = -2;
                }
            }
        }
        return square;
    }

    /**
     * Get the game described by this transcript
     *
     * @return the game, as calculated up until the first error
     */
    COsGame getGame() {
        return calcResult.game;
    }

    /**
     * Get the move number on which the first error occurs
     *
     * @return move number on which the first error occurs, or 0 if there are no errors
     */
    int getErrorMoveNumber() {
        return calcResult.errorMoveNumber;
    }

    /**
     * Get error descriptions, in a form suitable for passing on to a user
     *
     * @return a list of error descriptions
     */
    List<String> getErrors() {
        return calcResult.errors;
    }

    void recalculate() {
        final COsGame game = new COsGame();
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);

        final ArrayList<String> errors = new ArrayList<>();

        int errorMoveNumber = 0;

        for (int moveNumber = 1; ; moveNumber++) {
            final int sq = getSquare(moveNumber);
            if (sq < 0) {
                if (sq == -1) {
                    errors.add("Game complete through move " + (moveNumber - 1));
                } else {
                    errors.add("Multiple moves numbered " + moveNumber);
                    errorMoveNumber = moveNumber;
                }
                break;
            } else {
                final int row = BitBoardUtils.row(sq);
                final int col = BitBoardUtils.col(sq);
                final OsMove move = new OsMove(row, col);
                if (!game.getPos().board.isMoveLegal(move)) {
                    errors.add("Illegal move - number " + moveNumber + " at " + move);
                    errorMoveNumber = moveNumber;
                    break;
                }
//                played[sq] = board.isBlackMove() ? COsBoard.BLACK : COsBoard.WHITE;
                game.append(new OsMoveListItem(move));
                final int nPass = game.getPos().board.nPass();
                if (nPass == 1) {
                    game.append(OsMoveListItem.PASS);
                } else if (nPass == 2) {
                    break; // game over
                }
            }
        }

        calcResult = new CalcResult(game, errors, errorMoveNumber);

        for (Listener listener : getListeners()) {
            listener.transcriptDataUpdated();
        }
    }

    void setMoveNumber(int sq, int moveNumber) {
        moveNumbers[sq] = moveNumber;
    }

    public void shiftMoveNumber(int sq, int digit) {
        moveNumbers[sq] = (moveNumbers[sq] % 10) * 10 + digit;
    }

    public int getMoveNumber(int row, int col) {
        return moveNumbers[BitBoardUtils.square(row, col)];
    }

    public void clearBoard() {
        for (int i = 0; i < moveNumbers.length; i++) {
            moveNumbers[i] = 0;
        }
        recalculate();
    }

    static class CalcResult {
        final COsGame game;
        final ArrayList<String> errors;
        final int errorMoveNumber;

        CalcResult(COsGame game, ArrayList<String> errors, int errorMoveNumber) {
            this.game = game;
            this.errors = errors;
            this.errorMoveNumber = errorMoveNumber;
        }
    }

    interface Listener {
        void transcriptDataUpdated();
    }
}
