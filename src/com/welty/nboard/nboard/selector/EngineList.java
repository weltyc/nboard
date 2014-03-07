package com.welty.nboard.nboard.selector;

import com.welty.othello.gui.ExternalEngineManager;
import com.welty.othello.gui.selector.EngineFactory;
import com.welty.othello.gui.selector.ExternalEngineFactory;
import com.welty.othello.gui.selector.InternalEngineFactoryManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;

class EngineList extends JList<EngineFactory> {
    private final EngineListModel engineListModel;
    private final EngineList.DeleteEngineAction deleteEngineAction;

    EngineList(boolean includeWeakEngines) {
        this(new EngineListModel(InternalEngineFactoryManager.internalOpponentSelectors(includeWeakEngines)));
    }

    private EngineList(final EngineListModel engineListModel) {
        super(engineListModel);
        this.engineListModel = engineListModel;
        setUpList(this);

        ExternalEngineManager.instance.addListener(new EngineManagerListener(engineListModel));
        deleteEngineAction = new DeleteEngineAction();
    }

    /**
     * @param engineName name of engine to find
     * @return index of the first engine whose name equals name, or -1 if no match found
     */
    int find(String engineName) {
        return engineListModel.find(engineName);
    }

    @NotNull Action getDeleteEngineAction() {
        return deleteEngineAction;
    }


    static <T> void setUpList(JList<T> ops) {
        ops.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        ops.setFont(UIManager.getFont("TextField.font"));
        ops.setAlignmentY(0.0f);
        ops.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ops.setSelectedIndex(0);
    }

    private class DeleteEngineAction extends AbstractAction implements ListSelectionListener {
        public DeleteEngineAction() {
            super("Delete engine...");
            addListSelectionListener(this);
            setVisibility();
        }

        private void setVisibility() {
            final EngineFactory selectedValue = getSelectedValue();
            final boolean external = selectedValue.isExternal();
            this.setEnabled(external);
        }

        @Override public void actionPerformed(ActionEvent e) {
            final String name = getSelectedValue().name;
            final int result = JOptionPane.showConfirmDialog(null, "Delete " + name + "?", "Confirm Delete Engine", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                ExternalEngineManager.instance.delete(name);
            }
        }

        @Override public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                setVisibility();
            }
        }
    }

    private class EngineManagerListener implements ExternalEngineManager.Listener {
        private final EngineListModel engineListModel;

        public EngineManagerListener(EngineListModel engineListModel) {
            this.engineListModel = engineListModel;
        }

        @Override public void engineAdded(String name, String wd, String command) {
            engineListModel.put(new ExternalEngineFactory(name, wd, command));
        }

        @Override public void engineDeleted(String name) {
            final int i = engineListModel.find(name);
            if (i >= 0) {
                final int selectedIndex = getSelectedIndex();
                if (selectedIndex == i) {
                    if (selectedIndex > 0) {
                        setSelectedIndex(selectedIndex - 1);
                    } else {
                        setSelectedIndex(selectedIndex + 1);
                    }
                }
                engineListModel.remove(i);

            }
        }
    }
}
