package com.welty.nboard.nboard.selector;

import com.welty.othello.gui.ExternalEngineManager;
import com.welty.othello.gui.selector.EngineFactory;
import com.welty.othello.gui.selector.ExternalEngineFactory;

import javax.swing.*;
import java.util.prefs.BackingStoreException;

class EngineListModel extends DefaultListModel<EngineFactory> {
    public EngineListModel(java.util.List<EngineFactory> engineFactories) {
        for (EngineFactory es : engineFactories) {
            addElement(es);
        }
        try {
            for (ExternalEngineManager.Xei xei : ExternalEngineManager.instance.getXei()) {
                final ExternalEngineFactory selector = new ExternalEngineFactory(xei);
                addElement(selector);
            }
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(null, "External engine preferences are unavailable");
        }
    }

    public void put(EngineFactory engineFactory) {
        final int i = find(engineFactory.name);
        if (i < 0) {
            addElement(engineFactory);
        } else {
            set(i, engineFactory);
        }
    }

    /**
     * @param engineName name of engine to find
     * @return index of the first engine whose name equals name, or -1 if no match found
     */
    int find(String engineName) {
        for (int i = 0; i < size(); i++) {
            if (get(i).name.equals(engineName)) {
                return i;
            }
        }
        return -1;
    }

}
