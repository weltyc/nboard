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

package com.welty.nboard.nboard.startpos;

import com.orbanova.common.feed.Feeds;
import com.welty.novello.core.Board;
import com.welty.othello.gdk.OsMove;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class StartPositionChooser {
    private static final XotChooser xotChooser = new XotChooser();

    public static StartPosition next(String startPositionType) {
        final StartPosition startPosition;
        switch (startPositionType) {
            case "Standard":
                startPosition = new StartPosition(Board.START_BOARD);
                break;
            case "Alternate":
                startPosition = new StartPosition(Board.ALTERNATE_START_BOARD);
                break;
            case "XOT":
                startPosition = xotChooser.next();
                break;
            case "F5":
                startPosition = new StartPosition(Board.START_BOARD, new OsMove("F5"));
                break;
            default:
                throw new RuntimeException("Unknown start position type : " + startPositionType);
        }
        return startPosition;
    }

    private static class XotChooser {
        private final List<String> xots;
        private int lastIndex = 0;

        private XotChooser() {
            final InputStream in = XotChooser.class.getResourceAsStream("xot-large.txt");
            xots = Feeds.ofLines(in).asList();
            Collections.shuffle(xots);
        }

        public synchronized StartPosition next() {
            lastIndex++;
            if (lastIndex >= xots.size()) {
                lastIndex = 0;
            }

            return new StartPosition(Board.START_BOARD, xots.get(lastIndex));
        }
    }
}
