package com.welty.nboard.thor;

/**
 * Summary of thor data for a given move
 */
public class ThorSummaryData {
    int nBlackWins;
    int nWhiteWins;
    int nPlayed;
    private float score;    //*< Average score (+1 for a win, 0.5 for a draw, 0 for a loss)
    private float frequency;    //*< frequency with which this move was chosen.


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
        frequency = (float) (nPlayed) / total;
    }
}
