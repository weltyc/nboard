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

import com.welty.nboard.nboard.NBoard;

import javax.swing.*;
import java.util.List;

/**
 * A group of RadioButtonMenuItems
 */
public class RadioGroup {
    private final String key;
    private final int def;
    private final JRadioButtonMenuItem[] items;

    /**
     * Add a group of RadioButtonMenuItems to the menu.
     * <p/>
     * The selected index is read from the registry in this constructor.
     * It is written to the registry on shutdown.
     *
     * @param menu          Menu to add group to
     * @param key           registry key
     * @param def           default value if registry key does not exist or is outside the valid range
     * @param shutdownHooks list of shutdown hooks. This adds a hook that writes the value to the registry on shutdown.
     * @param items         the RadioButtonMenuItems to be added
     */
    public RadioGroup(JMenu menu, String key, int def, List<Runnable> shutdownHooks, JRadioButtonMenuItem... items) {
        this.key = key;
        this.def = def;
        this.items = items;
        ButtonGroup group = new ButtonGroup();

        for (JRadioButtonMenuItem item : items) {
            menu.add(item);
            group.add(item);
        }
        items[readIndex()].setSelected(true);
        shutdownHooks.add(new Runnable() {
            public void run() {
                writeIndex();
            }
        });
    }

    /**
     * Read the index of the selected item from the registry
     * <p/>
     * If the registry index does not exist or is outside the legal range, returns the default value
     * as set in the constructor.
     *
     * @return the index.
     */
    public int readIndex() {
        int i = NBoard.RegistryReadU4(key, def);
        if (i < 0 || i >= items.length) {
            i = def;
        }
        return i;
    }

    /**
     * @return index of the selected item, or 0 if no item is selected
     */
    public int getIndex() {
        for (int i = 0; i < items.length; i++) {
            if (items[i].isSelected()) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Write the index of the selected item to the registry
     */
    protected void writeIndex() {
        NBoard.RegistryWriteU4(key, getIndex());
    }

    public void setIndex(int i) {
        items[i].doClick();
    }
}
