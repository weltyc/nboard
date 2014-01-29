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
