package com.welty.nboard.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 27, 2009
 * Time: 1:01:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class ColorColumnRenderer extends DefaultTableCellRenderer {
    private final Color bkgndColor;
    private final Color fgndColor;

    public ColorColumnRenderer(Color bkgnd, Color foregnd) {
        super();
        bkgndColor = bkgnd;
        fgndColor = foregnd;
    }

    public Component getTableCellRendererComponent
            (JTable table, Object value, boolean isSelected,
             boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent
                (table, value, isSelected, hasFocus, row, column);

        cell.setBackground(bkgndColor);
        cell.setForeground(fgndColor);

        return cell;
    }
}