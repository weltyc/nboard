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
