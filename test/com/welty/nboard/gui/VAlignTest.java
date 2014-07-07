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

package com.welty.nboard.gui;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 22, 2009
 * Time: 9:22:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class VAlignTest extends TestCase {
    public void testMiddle() {
        assertEquals(10, VAlign.MIDDLE.stringY(10, 0, 0, 0));
        assertEquals(5, VAlign.MIDDLE.stringY(0, 10, 0, 0));
        assertEquals(10, VAlign.MIDDLE.stringY(0, 0, -10, 0));
        assertEquals(-5, VAlign.MIDDLE.stringY(0, 0, 0, 10));
    }

    public void testTop() {
        assertEquals(10, VAlign.TOP.stringY(10, 0, 0, 0));
        assertEquals(0, VAlign.TOP.stringY(0, 10, 0, 0));
        assertEquals(10, VAlign.TOP.stringY(0, 0, -10, 0));
        assertEquals(0, VAlign.TOP.stringY(0, 0, 0, 10));
    }
}
