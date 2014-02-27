package com.welty.nboard.nboard;

import com.orbanova.common.graph.cy.BarGraph;
import com.orbanova.common.graph.cy.CategoryGraphData;
import com.orbanova.common.graph.cy.CategorySeries;
import com.orbanova.common.graph.cy.ListCategorySeries;
import com.orbanova.common.graph.x.Range;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveList;
import com.welty.othello.gdk.OsMoveListItem;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * A graph that displays time taken by the players.
 */
class TimeGraph extends BarGraph<Integer> {


    TimeGraph(ReversiData reversiData) {
        super("Time used", new CategoryGraphData<>(extractSeries(reversiData)));
        setPreferredSize(new Dimension(200, 100));
        interior().setBackground(Color.GRAY);
        setSeriesColors(Color.BLACK, Color.WHITE);
        reversiData.addListener(new MyListener(reversiData));
        yAxis().setMinSegments(3);
        yAxis().setRequiredRange(new Range(0., 1.));
    }

    static List<CategorySeries<Integer>> extractSeries(ReversiData reversiData) {
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
    static List<CategorySeries<Integer>> extractSeries(COsMoveList ml, boolean blackMovesFirst) {
        final ListCategorySeries<Integer> black = new ListCategorySeries<>("Black");
        final ListCategorySeries<Integer> white = new ListCategorySeries<>("White");

        boolean blackToMove = blackMovesFirst;

        for (int moveNumber = 1; moveNumber <= ml.size(); moveNumber++) {
            final OsMoveListItem mli = ml.get(moveNumber - 1);
            if (blackToMove) {
                black.add(moveNumber, mli.getElapsedTime());
            } else {
                white.add(moveNumber, mli.getElapsedTime());
            }
            blackToMove = !blackToMove;
        }
        return Arrays.<CategorySeries<Integer>>asList(black, white);
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
