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

import com.orbanova.common.graph.xy.ListXYSeries;
import com.welty.othello.gdk.COsMoveList;
import com.welty.othello.gdk.OsMoveListItem;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 */
public class EvalGraphTest extends TestCase {
    public void testExtractSeries() {
        final COsMoveList ml = new COsMoveList();

        // expected result when black moves first
        final ListXYSeries blackB = new ListXYSeries("Black");
        final ListXYSeries whiteB = new ListXYSeries("White");
        final List<ListXYSeries> expectedB = Arrays.asList(blackB, whiteB);

        // expected result when white moves first
        final ListXYSeries blackW = new ListXYSeries("Black");
        final ListXYSeries whiteW = new ListXYSeries("White");
        final List<ListXYSeries> expectedW = Arrays.asList(blackW, whiteW);

        // no moves yet
        assertEquals(expectedB, EvalGraph.extractSeries(ml, true));
        assertEquals(expectedW, EvalGraph.extractSeries(ml, false));

        // first move
        ml.add(new OsMoveListItem("F5/6.25/2.0"));
        blackB.add(1., 6.25);
        whiteW.add(1., -6.25);
        assertEquals(expectedB, EvalGraph.extractSeries(ml, true));
        assertEquals(expectedW, EvalGraph.extractSeries(ml, false));

        // second move
        ml.add(new OsMoveListItem("D6/5"));
        whiteB.add(2., -5);
        blackW.add(2, 5);
        assertEquals(expectedB, EvalGraph.extractSeries(ml, true));
        assertEquals(expectedW, EvalGraph.extractSeries(ml, false));
    }
}
