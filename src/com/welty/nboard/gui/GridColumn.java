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
