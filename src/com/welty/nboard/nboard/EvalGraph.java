package com.welty.nboard.nboard;

import com.welty.graph.x.Range;
import com.welty.graph.xy.ListXYSeries;
import com.welty.graph.xy.XYGraph;
import com.welty.graph.xy.XYGraphData;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveList;
import com.welty.othello.gdk.OsMoveListItem;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * A graph that displays evals.
 * <p/>
 * Evals by both players in the game are displayed.
 * <p/>
 * Evals favouring the black player are displayed as positive in the graph. Evals produced by the white
 * player are inverted before graphing; if the white player returns an eval of "-1" this is graphed as a +1.
 */
class EvalGraph extends XYGraph {


    EvalGraph(ReversiData reversiData) {
        super("Score", new XYGraphData(extractSeries(reversiData)));
        setPreferredSize(new Dimension(200, 100));
        interior().setBackground(Color.GRAY);
        setSeriesColors(Color.BLACK, Color.WHITE);
        reversiData.addListener(new MyListener(reversiData));
        yAxis().setMinSegments(3);
        yAxis().setRequiredRange(new Range(-2, 2));
    }

    static List<ListXYSeries> extractSeries(ReversiData reversiData) {
        final COsGame game = reversiData.getGame();
        return extractSeries(game.getMoveList(), game.posStart.board.isBlackMove());
    }

    /**
     * Calculate graph data from a list of moves
     *
     * @param ml              move list
     * @param blackMovesFirst true if black makes the first move in the move list, false if white does
     * @return the graph data
     */
    static List<ListXYSeries> extractSeries(COsMoveList ml, boolean blackMovesFirst) {
        final ListXYSeries black = new ListXYSeries("Black");
        final ListXYSeries white = new ListXYSeries("White");

        boolean blackToMove = blackMovesFirst;

        for (int moveNumber = 1; moveNumber <= ml.size(); moveNumber++) {
            final OsMoveListItem mli = ml.get(moveNumber - 1);
            if (blackToMove) {
                black.add(moveNumber, mli.getEval());
            } else {
                white.add(moveNumber, -mli.getEval());
            }
            blackToMove = !blackToMove;
        }
        return Arrays.asList(black, white);
    }

    private class MyListener implements SignalListener<OsMoveListItem> {
        private final ReversiData reversiData;

        private MyListener(ReversiData reversiData) {
            this.reversiData = reversiData;
        }

        @Override public void handleSignal(OsMoveListItem data) {
            getGraphData().setSeries(extractSeries(reversiData));
        }
    }
}
