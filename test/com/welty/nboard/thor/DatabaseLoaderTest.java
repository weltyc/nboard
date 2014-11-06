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

import com.welty.othello.thor.DatabaseData;
import junit.framework.TestCase;

public class DatabaseLoaderTest extends TestCase {

    public static void testIsWtbFilename() {
        assertTrue(DatabaseData.isThorGamesFile("temp.wtb"));
        assertTrue(!DatabaseData.isThorGamesFile("temp"));
        assertTrue(!DatabaseData.isThorGamesFile("bla.wtba"));
        assertTrue(DatabaseData.isThorGamesFile("foo.WTB"));
        assertTrue(DatabaseData.isThorGamesFile("c:/devl/othello/foo.wtB"));
    }
}
