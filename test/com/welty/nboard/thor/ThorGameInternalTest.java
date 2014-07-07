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

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

import static com.welty.othello.core.Utils.D6;
import static com.welty.othello.core.Utils.F5;

/**
 * <PRE>
 * User: Chris
 * Date: Jul 13, 2009
 * Time: 8:15:59 PM
 * </PRE>
 */
public class ThorGameInternalTest extends TestCase {
    public void testToOsGame() {
        final ThorGameInternal game = new ThorGameInternal(0, 0, 1, 4, 32, new byte[]{F5, D6}, 2009, 0);
        final String expected = "(;GM[Othello]PC[fooTournament]DT[2009]PB[Foo]PW[Bar]RE[-56]TI[0]TY[8]BO[8 ---------------------------O*------*O--------------------------- *]B[F5]W[D6];)";
        final List<String> tournaments = Arrays.asList("fooTournament");
        final List<String> players = Arrays.asList("Foo", "Bar", "Foobar");
        assertEquals(expected, game.toOsGame(tournaments, players).toString());
    }
}
