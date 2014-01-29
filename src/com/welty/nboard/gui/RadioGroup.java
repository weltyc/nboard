package com.welty.nboard.gui;

import com.welty.nboard.NBoard;

import javax.swing.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 23, 2009
 * Time: 7:43:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class RadioGroup {
    private final String key;
    private final int def;
    private final JRadioButtonMenuItem[] items;

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

    public int readIndex() {
        int i = NBoard.RegistryReadU4(key, def);
        if (i < 0 || i >= items.length) {
            i = def;
        }
        return i;
    }

    public int getIndex() {
        for (int i = 0; i < items.length; i++) {
            if (items[i].isSelected()) {
                return i;
            }
        }
        return 0;
    }

    protected void writeIndex() {
        NBoard.RegistryWriteU4(key, getIndex());
    }

    public void setIndex(int i) {
        items[i].doClick();
    }
}
