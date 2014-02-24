package com.welty.nboard.nboard;

import com.orbanova.common.misc.ListenerManager;
import com.welty.graph.xy.XYPoint;
import com.welty.graph.xy.XYSeries;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.procedure.TIntDoubleProcedure;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class AnalysisData extends ListenerManager<AnalysisData.Listener> implements XYSeries {
    private final TIntDoubleHashMap scores = new TIntDoubleHashMap();

    public void put(int moveNumber, double eval) {
        scores.put(moveNumber, eval);
        for (Listener listener : getListeners()) {
            listener.dataChanged();
        }
    }

    @NotNull @Override public List<XYPoint> getPoints() {
        final ArrayList<XYPoint> points = new ArrayList<>();
        scores.forEachEntry(new TIntDoubleProcedure() {
            @Override public boolean execute(int a, double b) {
                points.add(new XYPoint(a, b));
                return true;
            }
        });
        return points;
    }

    @NotNull @Override public String getName() {
        return "Analysis";
    }

    public boolean hasData() {
        return !scores.isEmpty();
    }

    public interface Listener {
        /**
         * Notify the listener that this AnalysisData has been updated.
         */
        void dataChanged();
    }
}
