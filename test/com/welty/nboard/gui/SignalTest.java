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
 * Date: Jun 17, 2009
 * Time: 3:12:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class SignalTest extends TestCase {

    /**
     * Class for testing signals
     */
    class SignalTester<T> implements SignalListener<T> {
        SignalTester(int n) {
            m_n = n;
        }

        /**
         * Increment m_n
         */
        public void handleSignal(T t) {
            m_n++;
        }

        int m_n;
    }

    /**
     * Test signals by creating an event and seeing if Raising the event calls the handler
     */
    public void testSignal() {
        SignalEvent se = new SignalEvent();
        SignalTester tester = new SignalTester(0);

        se.Add(tester);
        assertEquals(0, tester.m_n);
        se.Raise();
        assertEquals(1, tester.m_n);
    }

}
