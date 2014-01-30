package com.welty.nboard.nboard;

import com.welty.nboard.nboard.GgfGameText;
import com.welty.othello.c.CReader;
import com.welty.nboard.thor.ThorOpeningMap;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 25, 2009
 * Time: 10:08:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class GgfGameTextTest extends TestCase {

    public void testGgfGameText() {
        {
            String data = "(;GM[Othello]PC[NBoard]DT[2004-11-24 13:47:34 GMT]PB[Chris]PW[Ntest2]RE[-12]TI[0]TY[8]BO[8 ---------------------------O*------*O--------------------------- *]B[F5]W[D6];)";
            GgfGameText gt = checkGameEqualsData(data);

            assertEquals(gt.PB(), "Chris");
            assertEquals(gt.PW(), "Ntest2");
            assertEquals(gt.PC(), "NBoard");
            assertEquals(gt.DT(), "2004-11-24 13:47:34 GMT");
            assertEquals(gt.RE(), "-12");
        }
        {
            // whitespace in the middle of the board field is ok
            String data = "(;GM[Othello]PC[NBoard]DT[2004-11-24 13:47:34 GMT]PB[Chris]PW[Ntest2]RE[-12]TI[0]TY[8]BO[8 -------- -------- -------- ---O*--- ---*O--- -------- -------- -------- *]B[F5]W[D6];)";
            GgfGameText gt = checkGameEqualsData(data);
        }
        {
            // no BO field
            testConstructorThrows("(;GM[Othello]PC[NBoard]DT[2004-11-24 13:47:34 GMT]PB[Chris]PW[Ntest2]RE[-12]TI[0]TY[s8r5]B[F5]W[D6];)");
        }
        {
            //  BO field has no ]
            testConstructorThrows("(;GM[Othello]PC[NBoard]DT[2004-11-24 13:47:34 GMT]PB[Chris]PW[Ntest2]RE[-12]TI[0]TY[s8r5]B[F5]W[D6]O[8 --------------------------*O*------*O--------------------------- *;)");
        }
        {
            // rand game
            String data = "(;GM[Othello]PC[NBoard]DT[2004-11-24 13:47:34 GMT]PB[Chris]PW[Ntest2]RE[-12]TI[0]TY[s8r5]BO[8 --------------------------*O*------*O--------------------------- *]B[F5]W[D6];)";
            final GgfGameText.StringLoc stringLoc = new GgfGameText.StringLoc(data);
            GgfGameText gt = new GgfGameText(stringLoc);
            assertEquals(gt.m_openingCode, 0);
            assertFalse(gt.Is8x8Standard());
        }
        {
            // 10x10 game
            String data = "(;GM[Othello]PC[NBoard]DT[2004-11-24 13:47:34 GMT]PB[Chris]PW[Ntest2]RE[-12]TI[0]TY[s10r5]BO[10 --------------------------*O*------*O--------------------------------------------------------------- *];)";
            final GgfGameText.StringLoc stringLoc = new GgfGameText.StringLoc(data);
            GgfGameText gt = new GgfGameText(stringLoc);
            assertEquals(gt.m_openingCode, 0);
            assertFalse(gt.Is8x8Standard());
        }
        {
            testConstructorThrows("(;GM[Othello]PC[This game is corrupt");
        }

        {
            testConstructorThrows("(;GM[Othello]PB[This is also corrupt as it's missing a closing right square bracket;)");
        }
    }

    private static GgfGameText checkGameEqualsData(String data) {
        final GgfGameText.StringLoc stringLoc = new GgfGameText.StringLoc(data);
        GgfGameText gt = new GgfGameText(stringLoc);
        assertEquals(stringLoc.loc, -1);
        assertEquals(gt.m_text, data);
        assertEquals(gt.GetText(0), "Chris");
        assertEquals(gt.GetText(1), "Ntest2");
        assertEquals(gt.m_nResult, -12);
        assertTrue(gt.Is8x8Standard());
        assertEquals(ThorOpeningMap.OpeningName(gt.m_openingCode), "Perpendicular");
        return gt;
    }

    public void testMultipleFiles() {
        final String data = "(;GM[Othello]PC[]PB[HP_Administrator]PW[HP_Administrator]RE[?]TI[0//0]TY[8]BO[8 ---------------------------O*------*O--------------------------- *]B[F5]W[D6]B[C3];)\n" +
                "(;GM[Othello]PC[]PB[Ntest2]PW[Ntest2]RE[18.0]TI[0//0]TY[8]BO[8 ---------------------------O*------*O--------------------------- *]B[F5/-1.39]W[D6/0.98]B[C4/-1.34/0.016]W[D3/1.29]B[C3/-1.30]W[F4/1.13]B[E6/-0.51/0.015]W[B3/0.25]B[F3/-0.76]W[C5/1.79]B[D2/-1.82]W[E3/3.04]B[E2/-1.89]W[F1/1.99]B[E1/-0.52]W[D1/1.31]B[B4/-0.78]W[A3/1.19]B[B6/-1.89]W[B5/2.94/0.016]B[C6/-2.71/0.015]W[F2/2.85/0.015]B[A5/-1.83]W[A6/0.37]B[A4/1.71]W[G3/-0.31]B[C2/0.35/0.015]W[G5/0.57]B[G4/2.58]W[D7/-0.20]B[H3/2.26/0.015]W[F7/-0.03]B[H6/2.25]W[F6/-1.43]B[C1/0.46]W[B1/-0.92]B[B2/-1.12]W[C7/6.09]B[E8/-7.01]W[D8/7.71]B[E7/-8.83]W[A1/7.30]B[A2/-8.36]W[H5/5.20]B[F8/-4.95]W[H4/6.66]B[C8/-7.67]W[G7/18.00/0.687]B[G6/-18.00]W[G8/18.00]B[H8/-18.00]W[H7/18.00]B[G2/-18.00/0.015]W[G1/18.00]B[A7]W[A8/18.00]B[PA]W[B7/18.00]B[B8]W[H1/18.00]B[H2];)";
        final ArrayList<GgfGameText> texts = GgfGameText.Load(new CReader(data));
        assertEquals(2, texts.size());
        for (int i = 0; i < 2; i++) {
            assertTrue(texts.get(i).m_text.startsWith("(;GM[Othello]"));
        }
    }

    public void testMissingDate() {
        final String data = "(;GM[Othello]PC[]PB[HP_Administrator]PW[HP_Administrator]RE[?]TI[0//0]TY[8]BO[8 ---------------------------O*------*O--------------------------- *]B[F5]W[D6]B[C3];)\n";
        final ArrayList<GgfGameText> texts = GgfGameText.Load(new CReader(data));
        assertEquals("", texts.get(0).DT());
    }

    private void testConstructorThrows(String s) {
        try {
            new GgfGameText(new GgfGameText.StringLoc(s));
            fail("error, should throw");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }
}
