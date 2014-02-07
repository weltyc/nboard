package com.welty.nboard.nboard.selector;

import com.welty.othello.gui.ExternalEngineManager;
import com.welty.othello.gui.selector.EngineSelector;
import com.welty.othello.gui.selector.ExternalEngineSelector;

import javax.swing.*;
import java.util.prefs.BackingStoreException;

class EngineListModel extends DefaultListModel<EngineSelector> {
    public EngineListModel(java.util.List<EngineSelector> engineSelectors) {
        for (EngineSelector es : engineSelectors) {
            addElement(es);
        }
        try {
            for (ExternalEngineManager.Xei xei : ExternalEngineManager.getXei()) {
                final ExternalEngineSelector selector = new ExternalEngineSelector(xei.name, xei.wd, xei.cmd);
                addElement(selector);
            }
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(null, "External engine preferences are unavailable");
        }
    }

    public void put(EngineSelector engineSelector) {
        final int i = find(engineSelector.name);
        if (i < 0) {
            addElement(engineSelector);
        } else {
            set(i, engineSelector);
        }
    }

    /**
     * @param name name of element to find
     * @return index of the first element whose name equals name, or -1 if no match found
     */
    int find(String name) {
        for (int i = 0; i < size(); i++) {
            if (get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

}
