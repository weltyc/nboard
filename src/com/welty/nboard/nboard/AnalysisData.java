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

import com.orbanova.common.misc.ListenerManager;
import com.orbanova.common.graph.xy.XYPoint;
import com.orbanova.common.graph.xy.XYSeries;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;
import gnu.trove.map.hash.TIntDoubleHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class AnalysisData extends ListenerManager<AnalysisData.Listener> implements XYSeries, SignalListener<OsMoveListItem> {
    private final TIntDoubleHashMap scores = new TIntDoubleHashMap();
    private final ReversiData reversiData;
    /**
     * The game to which this analysis relates.
     */
    private COsGame game = null;

    AnalysisData(ReversiData reversiData) {
        this.reversiData = reversiData;
        reversiData.addListener(this);
    }
    public void put(int moveNumber, double eval) {
        scores.put(moveNumber, eval);
        fireDataChanged();
    }

    private void fireDataChanged() {
        for (Listener listener : getListeners()) {
            listener.dataChanged();
        }
    }

    @NotNull @Override public List<XYPoint> getPoints() {
        final ArrayList<XYPoint> points = new ArrayList<>();
        final int[] nEmpties = scores.keys();
        Arrays.sort(nEmpties);
        for (int nEmpty : nEmpties) {
            final double value = scores.get(nEmpty);
            points.add(new XYPoint(nEmpty, value));
        }
        return points;
    }

    @NotNull @Override public String getName() {
        return "Analysis";
    }

    public boolean hasData() {
        return !scores.isEmpty();
    }

    public void setGame(COsGame game) {
        if (!(game.equals(this.game))) {
            this.game = new COsGame(game);
            clearData();
        }
    }

    public void clearData() {
        scores.clear();
        fireDataChanged();
    }

    @Override public void handleSignal(OsMoveListItem data) {
        setGame(reversiData.getGame());
    }

    public interface Listener {
        /**
         * Notify the listener that this AnalysisData has been updated.
         */
        void dataChanged();
    }
}
