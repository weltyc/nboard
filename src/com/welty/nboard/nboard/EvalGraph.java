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

package com.welty.nboard.nboard;

import com.orbanova.common.graph.x.Range;
import com.orbanova.common.graph.xy.ListXYSeries;
import com.orbanova.common.graph.xy.XYGraph;
import com.orbanova.common.graph.xy.XYGraphData;
import com.orbanova.common.graph.xy.XYSeries;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveList;
import com.welty.othello.gdk.OsMoveListItem;

import java.awt.*;
import java.util.ArrayList;
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
    EvalGraph(ReversiData reversiData, AnalysisData analysisData) {
        super("Score", new XYGraphData(extractAllSeries(reversiData, analysisData)));
        setPreferredSize(new Dimension(200, 100));
        interior().setBackground(Color.GRAY);
        setSeriesColors(Color.BLACK, Color.WHITE, Color.BLUE);
        final MyListener listener = new MyListener(reversiData, analysisData);
        reversiData.addListener(listener);
        analysisData.addListener(listener);
        yAxis().setMinSegments(3);
        yAxis().setRequiredRange(new Range(-2, 2));
    }

    /**
     * Get graph data from a list of moves, appending analysisData if it exists.
     *
     * @param reversiData  evaluations from the game
     * @param analysisData evaluations from the analysis engine
     * @return complete list of evaluations.
     */
    static List<XYSeries> extractAllSeries(ReversiData reversiData, AnalysisData analysisData) {
        final COsGame game = reversiData.getGame();
        final ArrayList<XYSeries> serieses = new ArrayList<XYSeries>(extractSeries(game.getMoveList(), game.posStart.board.isBlackMove()));
        if (analysisData.hasData()) {
            serieses.add(analysisData);
        }
        return serieses;
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

    private class MyListener implements SignalListener<OsMoveListItem>, AnalysisData.Listener {
        private final ReversiData reversiData;
        private final AnalysisData analysisData;

        private MyListener(ReversiData reversiData, AnalysisData analysisData) {
            this.reversiData = reversiData;
            this.analysisData = analysisData;
        }

        @Override public void handleSignal(OsMoveListItem data) {
            dataChanged();
        }

        @Override public void dataChanged() {
            final XYGraphData graphData = getGraphData();
            graphData.setSeries(extractAllSeries(reversiData, analysisData));
            graphData.setCursor(reversiData.IMove());
        }
    }
}
