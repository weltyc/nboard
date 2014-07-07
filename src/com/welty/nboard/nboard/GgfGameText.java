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

import com.orbanova.common.misc.Logger;
import com.welty.nboard.thor.IndeterminateProgressTracker;
import com.welty.nboard.thor.ThorOpeningMap;
import com.welty.othello.c.CReader;

import javax.swing.*;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Text of a GGF game plus cached information
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 25, 2009
 * Time: 8:22:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class GgfGameText {
    private static final Logger log = Logger.logger(GgfGameText.class);

    private static final String[] fields = {
            "PB[", "PW[", "DT[", "TY[", "RE[", "PC["
    };

    // Text of the game
    final String m_text;
    // Location of the various fields in the game, or int(-1) if the field was not found in the game
    // Value of the result field (field 4) converted to an int. This is precomputed to save time while sorting.
    int m_nResult;
    // Opening code, precomputed to save time
    public int m_openingCode;

    boolean Is8x8Standard() {
        return m_f8x8Standard;
    }


    public final byte[] Moves() {
        return ThorOpeningMap.moveBytesFromGgf(getText());
    }

    private final int[] m_starts = new int[fields.length];    //< Start loc of value text for each field
    private boolean m_f8x8Standard;                //< true if the game was played on an 8x8 board

    public String getText() {
        return m_text;
    }

    static class StringLoc {
        final String data;
        int loc;

        public StringLoc(String data) {
            this.data = data;
            loc = data.indexOf('(');
        }

        /**
         * find the next game in the string. Update 'loc' to be after the end of game
         *
         * @return string from end up to and including the game's terminal ";)"
         */
        public String nextGame() {
            int end = data.indexOf(";)", loc);
            if (end == -1) {
                throw new IllegalArgumentException("Corrupt GGF game");
            }
            end += 2;
            String game = data.substring(loc, end);
            loc = end;
            return game;
        }

        public void nextStart() {
            loc = data.indexOf('(', loc);
        }
    }

    /**
     * construct a GgfGameText from the text in data starting at position loc.
     * <p/>
     * The game is checked for validity.
     *
     * @throws IllegalArgumentException if the game is invalid.
     */
    GgfGameText(StringLoc stringLoc) {
        m_text = stringLoc.nextGame();
        for (int i = 0; i < fields.length; i++) {
            final int tagStart = m_text.indexOf(fields[i]);
            if (tagStart == -1) {
                m_starts[i] = -1;
            } else {
                m_starts[i] = tagStart + fields[i].length();
                if (m_text.indexOf(']', m_starts[i]) == -1) {
                    throw new IllegalArgumentException("Corrupt GGF game");
                }
            }
        }
        try {
            final CReader in = new CReader(m_text.substring(m_starts[4]));
            m_nResult = in.readInt();
        } catch (IllegalArgumentException | EOFException e) {
            m_nResult = 0;
        }
        m_f8x8Standard = TY().equals("8");
        SetOpeningCode();

        stringLoc.nextStart();
    }

    /**
     * @return the text of the field
     */
    String GetText(int field) {
        if (field < fields.length) {
            if (field != 4) {
                int start = m_starts[field];
                if (start == -1) {
                    return "";
                } else {
                    int end = m_text.indexOf(']', start);
                    return m_text.substring(start, end);
                }
            } else {
                return String.format("%+d", m_nResult);
            }
        } else
            return ThorOpeningMap.OpeningName(m_openingCode);
    }

    /**
     * @return name of black player
     */
    public String PB() {
        return GetText(0);
    }

    /**
     * @return name of white player
     */
    public String PW() {
        return GetText(1);
    }

    /**
     * @return date of match
     */
    public String DT() {
        return GetText(2);
    }

    /**
     * @return game type text
     */
    String TY() {
        return GetText(3);
    }

    /**
     * @return result text
     */
    public String RE() {
        return GetText(4);
    }

    /**
     * @return game location (Place)
     */
    public String PC() {
        return GetText(5);
    }

    /**
     * Calculate the opening code and store it in m_openingCode
     * also determines whether the game was played on an 8x8 board.
     */
    void SetOpeningCode() {
        m_openingCode = ThorOpeningMap.OpeningCodeFromGgf(m_text);
    }

    /**
     * Load GgfGameTexts from a text file
     * <p/>
     * If there are invalid or corrupt games in the games file, inform the user via message box.
     *
     * @return GgfGameTexts
     */
    public static ArrayList<GgfGameText> Load(final File fn, IndeterminateProgressTracker tracker) {
        try {
            return Load(new CReader(fn), tracker);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Can't find file : " + fn, "Error loading games file", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    static ArrayList<GgfGameText> Load(CReader is, IndeterminateProgressTracker tracker) {
        ArrayList<GgfGameText> result = new ArrayList<>();

        log.info("Starting load");
        int nInvalid = 0;
        String data = is.readLine('\0');
        log.info("Read data");
        StringLoc stringLoc = new StringLoc(data);

        // now we need to find the beginning of each game.
        result.clear();
        while (stringLoc.loc != -1) {
            try {
                GgfGameText gt = new GgfGameText(stringLoc);
                if (gt.Is8x8Standard()) {
                    result.add(gt);
                }
                tracker.increment();
            } catch (IllegalStateException | IllegalArgumentException e) {
                nInvalid++;
            }
        }
        log.info("parsed file");
        tracker.update(); // #L6. If nInvalid!=0 the tracker would otherwise display an incorrect number of games.

        if (nInvalid != 0) {
            JOptionPane.showMessageDialog(null, "This file has " + nInvalid + " corrupt games", "Corrupt File alert", JOptionPane.WARNING_MESSAGE);
        }
        is.close();
        return result;
    }
}
