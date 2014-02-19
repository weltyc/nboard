package com.welty.nboard.nboard;

import com.welty.graph.ListXYSeries;
import com.welty.graph.XYGraph;
import com.welty.graph.XYGraphData;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveList;
import com.welty.othello.gdk.OsMoveListItem;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 */
class EvalGraph extends XYGraph {

    private final ReversiData reversiData;

    EvalGraph(ReversiData reversiData) {
        this(reversiData, createSampleData());
        setPreferredSize(new Dimension(200, 100));
        interior().setBackground(Color.GRAY);
        setSeriesColors(Color.BLACK, Color.WHITE);
        reversiData.addListener(new MyListener());
        yAxis().setMinSegments(3);
    }

    private EvalGraph(ReversiData reversiData, XYGraphData graphData) {
        super(graphData);
        this.reversiData = reversiData;
    }

    private static XYGraphData createSampleData() {
        final ListXYSeries black = new ListXYSeries("black");
        black.add(0, +2);
        black.add(2, 0);
        black.add(4, -4);
        final ListXYSeries white = new ListXYSeries("white");
        white.add(1, +2);
        white.add(3, 0);
        white.add(5, -3);
        return new XYGraphData(black, white);
    }

    /**
     * Calculate graph data from a list of moves
     * @param ml move list
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
        @Override public void handleSignal(OsMoveListItem data) {
            final COsGame game = reversiData.getGame();
            getGraphData().setSeries(extractSeries(game.getMoveList(), game.posStart.board.isBlackMove()));
        }
    }
}
