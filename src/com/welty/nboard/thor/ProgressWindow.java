package com.welty.nboard.thor;

import static com.welty.nboard.GraphicsUtils.drawString;
import static com.welty.nboard.GraphicsUtils.fillRect;

import javax.swing.*;
import java.awt.*;

/**
 * Window where progress information is displayed
 */
class ProgressWindow extends JFrame {
    private String m_text;

    ProgressWindow() {
        super("Thor");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setPreferredSize(new Dimension(200, 100));
    }

    void SetText(final String text) {
        m_text = text;
    }

    public void paint(Graphics gd) {
        final Rectangle rect = getBounds();
        fillRect(gd, rect, Color.lightGray);
        drawString(gd, m_text, rect);
    }
}
