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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Class to display "Tip of the day"
 * <PRE>
 * User: Chris
 * Date: Jul 7, 2009
 * Time: 11:56:29 PM
 * </PRE>
 */
class TipOfTheDayTlw extends JFrame implements ActionListener {


    private int m_id;
    private final String m_sRegKey;
    private final ArrayList<String> tips;
    private final JLabel m_pst;

    TipOfTheDayTlw(final String sRegKey) {
        super("Tip of the Day");
        this.m_sRegKey = sRegKey;
        tips = readTips();
        // get current string id value from the resource
        m_id = NBoard.RegistryReadU4(sRegKey, 0);
        if (m_id < 0 || m_id >= tips.size())
            m_id = 0;

        final BoxLayout layout = new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS);
        setLayout(layout);
        JPanel labelPanel = new JPanel();
        m_pst = new JLabel();
        setText();
        m_pst.setFont(m_pst.getFont().deriveFont(0));
        final Dimension labelSize = new Dimension(400, 75);
        m_pst.setPreferredSize(labelSize);
        m_pst.setMinimumSize(labelSize);
        m_pst.setHorizontalAlignment(JLabel.LEFT);
        labelPanel.add(m_pst);
        add(labelPanel);
        // buttons
        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        final JButton nextButton = new JButton("Next");
        nextButton.addActionListener(this);
        buttons.add(nextButton);
        buttons.add(Box.createHorizontalStrut(3));
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        buttons.add(okButton);
        buttons.add(Box.createHorizontalStrut(3));
        add(buttons);

        getRootPane().setDefaultButton(okButton);

        // display string
        pack();
        setVisible(true);
    }

    private static ArrayList<String> readTips() {
        ArrayList<String> tips = new ArrayList<String>();

        final InputStream in = TipOfTheDayTlw.class.getResourceAsStream("tips.txt");
        final BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        String tip;
        try {
            while (null != (tip = bin.readLine())) {
                tips.add(tip);
            }
        } catch (IOException e) {
            // ignore, shouldn't bother user about this.
        }
        return tips;
    }

    /**
     * Destroy window, write tomorrow's tip id to the registry
     */
    void Close() {
        // save next string m_id value to the resource
        Next();
        NBoard.RegistryWriteU4(m_sRegKey, m_id);
        setVisible(false);
    }

    /**
     * Calculate the next tip of the day
     */
    void Next() {
        m_id++;
        if (m_id >= tips.size()) {
            m_id = 0;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Next")) {
            Next();
            setText();
        } else if (e.getActionCommand().equals("OK")) {
            Close();
        } else {
            throw new IllegalStateException("unknown action event : " + e);
        }
    }

    private void setText() {
        m_pst.setText("<html>" + tips.get(m_id) + "</html>");
    }
}
