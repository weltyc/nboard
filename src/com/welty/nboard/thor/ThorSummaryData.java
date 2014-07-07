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

/**
 * Summary of thor data for a given move
 */
public class ThorSummaryData {
    int nBlackWins;
    int nWhiteWins;
    int nPlayed;
    private float score;    //*< Average score (+1 for a win, 0.5 for a draw, 0 for a loss)


    public int getNPlayed() {
        return nPlayed;
    }

    public float getScore() {
        return score;
    }

    void CalcScore(boolean fBlackMove) {
        score = (float) (nBlackWins - nWhiteWins + nPlayed) / (nPlayed + nPlayed);
        if (!fBlackMove)
            score = 1 - score;
    }

    void CalcFrequency(int total) {
        float frequency = (float) (nPlayed) / total;
    }
}
