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
 * Date: Jun 26, 2009
 * Time: 11:10:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class GridColumn {
    final int width;
    final String name;
    public final Class columnClass;
    public final Align align;

    public GridColumn(int width, String name) {
        this(width, name, Object.class, Align.RIGHT);
    }

    public GridColumn(int width, String name, Align align) {
        this(width, name, Object.class, align);
    }

    public GridColumn(int width, String name, Class columnClass) {
        this(width, name, columnClass, Number.class.isAssignableFrom(columnClass) ? Align.RIGHT : Align.LEFT);
    }

    public GridColumn(int width, String name, Class columnClass, Align align) {
        this.width = width;
        this.name = name;
        this.columnClass = columnClass;
        this.align = align;
    }
}
