package com.welty.nboard.nboard;

import com.welty.nboard.gui.Align;
import com.welty.nboard.gui.VAlign;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 20, 2009
 * Time: 12:05:00 AM
 * To change this template use File | Settings | File Templates.
 */
abstract class GraphicsUtils {
    public static void fillRect(Graphics gd, Rectangle rect, Color color) {
        gd.setColor(color);
        fillRect(gd, rect);
    }

    public static void fillRect(Graphics gd, Rectangle rect) {
        gd.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    public static void outlineRect(Graphics gd, Rectangle rect, Color color) {
        gd.setColor(color);
        gd.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    public static void fillEllipse(Graphics gd, Rectangle rect, Color color) {
        gd.setColor(color);
        fillEllipse(gd, rect);
    }

    private static void fillEllipse(Graphics gd, Rectangle rect) {
        gd.fillOval(rect.x, rect.y, rect.width, rect.height);
    }

    public static void outlineEllipse(Graphics gd, Rectangle rect, Color color) {
        gd.setColor(color);
        gd.drawOval(rect.x, rect.y, rect.width, rect.height);
    }

    public static void drawString(Graphics gd, String string, Rectangle rect) {
        drawString(gd, string, rect, Align.CENTER, VAlign.MIDDLE);
    }

    public static void drawString(Graphics gd, String string, Rectangle rect, Align align, VAlign vAlign) {
        final FontMetrics metrics = gd.getFontMetrics();

        final String[] parts = string.split("\n");
        final int rowHeight = metrics.getHeight();
        final int totalHeight = parts.length * rowHeight - metrics.getLeading();
        int y = vAlign.stringY(rect.y, rect.height, -metrics.getAscent(), totalHeight);
        for (String part : parts) {
            final Rectangle bounds = metrics.getStringBounds(part, gd).getBounds();
            final int x = align.stringStart(rect.x, rect.width, bounds.width);
            gd.drawString(part, x, y);
            y += rowHeight;
        }
    }

    public static Rectangle FractionalInflate(Rectangle rectColor, double fraction) {
        int dx = (int) (.5 * fraction * rectColor.width);
        int dy = (int) (.5 * fraction * rectColor.height);
        return new Rectangle(rectColor.x - dx, rectColor.y - dy, rectColor.width + dx + dx, rectColor.height + dy + dy);
    }


    public static void setPlainFont(JLabel label) {
        label.setFont(label.getFont().deriveFont(0));
    }
}
