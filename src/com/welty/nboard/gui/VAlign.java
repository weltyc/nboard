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

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 19, 2009
 * Time: 9:51:19 PM
 * To change this template use File | Settings | File Templates.
 */
public enum VAlign {
    TOP {
        @Override public int stringY(int y, int height, int stringY, int stringHeight) {
            return y - stringY;
        }
    },

    MIDDLE {
        @Override public int stringY(int y, int height, int stringY, int stringHeight) {
            return y-stringY + ((height - stringHeight)>>1);
        }
    },
    BOTTOM  {
        @Override public int stringY(int y, int height, int stringY, int stringHeight) {
            return y-stringY + height - stringHeight;
        }
    };


    /**
     * @param y y-value at top of box
     * @param height height of box
     * @param stringY y-value of rectangle returned by FontMetrics.getStringBounds()
     * @param stringHeight height of rectangle returned by FontMetrics.getStringBounds()
     * @return y-value for baseline of string
     */
    abstract public int stringY(int y, int height, int stringY, int stringHeight);
}
