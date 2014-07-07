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

import com.orbanova.common.misc.ListenerManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Constructs a RadioGroup that communicates via listener
 */
public class AutoRadioGroup extends ListenerManager<AutoRadioGroup.Listener> {
    private final RadioGroup radioGroup;

    /**
     * Add a group of RadioButtonMenuItems to the menu.
     * <p/>
     * The selected index is read from the registry in this constructor.
     * It is written to the registry on shutdown.
     *
     * @param menu                Menu to add group to
     * @param key                 registry key
     * @param def                 default value if registry key does not exist or is outside the valid range
     * @param shutdownHooks       list of shutdown hooks. This adds a hook that writes the value to the registry on shutdown.
     * @param textAndAccelerators texts of menu items to be added, with optional accelerator marking.
     *                            See {@link MenuItemBuilder#menuItem(String)} for the format.
     */
    public AutoRadioGroup(JMenu menu, String key, int def, List<Runnable> shutdownHooks, String... textAndAccelerators) {
        final ActionListener actionListener = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                fireSelectionChanged();
            }
        };

        final JRadioButtonMenuItem[] buttons = new JRadioButtonMenuItem[textAndAccelerators.length];
        for (int i = 0; i < textAndAccelerators.length; i++) {
            buttons[i] = MenuItemBuilder.menuItem(textAndAccelerators[i]).buildRadioButton(actionListener);
        }
        radioGroup = new RadioGroup(menu, key, def, shutdownHooks, buttons);
    }

    private void fireSelectionChanged() {
        final int index = radioGroup.getIndex();
        for (Listener listener : getListeners()) {
            listener.selectionChanged(index);
        }
    }

    public int getIndex() {
        return radioGroup.getIndex();
    }

    public void setIndex(int i) {
        if (i != radioGroup.getIndex()) {
            radioGroup.setIndex(i);
            fireSelectionChanged();
        }
    }

    public interface Listener {
        /**
         * Notify the user that the selection changed
         *
         * @param index new selection index
         */
        void selectionChanged(int index);
    }
}
