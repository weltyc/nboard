package com.welty.nboard.gui;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 17, 2009
 * Time: 9:57:49 PM
 * To change this template use File | Settings | File Templates.
 */
public enum Align {
    LEFT(SwingConstants.LEFT) {
        @Override public int stringStart(int x, int width, int stringWidth) {
            return x;
        }
    },

    CENTER(SwingConstants.CENTER) {
        @Override public int stringStart(int x, int width, int stringWidth) {
            return x + ((width - stringWidth) >> 1);
        }
    },
    RIGHT(SwingConstants.RIGHT) {
        @Override public int stringStart(int x, int width, int stringWidth) {
            return x + width - stringWidth;
        }
    };
    private final int swingValue;

    private Align(int swingValue) {
        this.swingValue = swingValue;
    }

    abstract public int stringStart(int x, int width, int stringWidth);

    public int getSwingValue() {
        return swingValue;
    }
}
