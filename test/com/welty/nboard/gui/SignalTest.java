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
