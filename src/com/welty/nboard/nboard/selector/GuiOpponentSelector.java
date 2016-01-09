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

package com.welty.nboard.nboard.selector;

import com.orbanova.common.jsb.JsbGridLayout;
import com.orbanova.common.prefs.PrefString;
import com.welty.novello.external.api.OpponentSelection;
import com.welty.novello.external.api.OpponentSelector;
import com.orbanova.common.prefs.PrefInt;
import com.welty.novello.external.gui.ExternalEngineManager;
import com.welty.novello.external.gui.selector.EngineFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static com.orbanova.common.jsb.JSwingBuilder.*;

/**
 * A Window that allows the user to select an Opponent to play
 */
public class GuiOpponentSelector extends OpponentSelector {

    private final PrefInt levelPref;
    private final PrefString enginePref;


    private final JDialog frame;
    private final JList<Integer> levels = new JList<>();
    private final EngineList engineList;
    private final JLabel strengthLabel = new JLabel("Strength");

    // these are written to when the user clicks "OK"
    private int selectedLevel;
    private @NotNull EngineFactory selectedEngine;

    /**
     * Create a window that allows the user to select an Opponent (=engine + depth)
     *
     * @param windowTitle        title of the selection window
     * @param includeWeakEngines if true, weak engines are included in the selection list. If false, they are not
     * @param preferencePrefix   prefix for saving the user's choices.
     * @param type               What the engine will be used for, displayed in the border of the engine selection list
     */
    public GuiOpponentSelector(String windowTitle, boolean includeWeakEngines, String preferencePrefix, String type) {
        levelPref = new PrefInt(GuiOpponentSelector.class, preferencePrefix + "Level", includeWeakEngines ? 1 : 12);
        enginePref = new PrefString(GuiOpponentSelector.class, preferencePrefix + "Opponent", includeWeakEngines ? "Abigail" : "Vegtbl");

        // Level selection list box.
        // Need to create this before Opponent selection list box because the
        // Opponent selection list box modifies it.
        final DefaultListModel<Integer> levelModel = new DefaultListModel<>();
        setLevelElements(levelModel, EngineFactory.advancedLevels);
        levels.setModel(levelModel);
        EngineList.setUpList(levels);
        levels.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        levels.setVisibleRowCount(EngineFactory.advancedLevels.length / 2);
        levels.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    setStrength();
                }
            }
        });


        // Opponent selection list box.
        engineList = new EngineList(includeWeakEngines);

        engineList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    final EngineFactory engineFactory = engineList.getSelectedValue();
                    setLevelElements(levelModel, engineFactory.availableLevels);
                    levels.setSelectedIndex(findNearestLevel(selectedLevel, engineFactory.availableLevels));
                    setStrength();
                }
            }
        });

        selectUsersPreferredEngine();
        selectUsersPreferredLevel();
        setStrength();

        final JButton ok = button(new AbstractAction("OK") {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                selectedLevel = levels.getSelectedValue();
                levelPref.put(levels.getSelectedValue());
                setSelectedEngine();
                enginePref.put(selectedEngine.name);
                fireOpponentChanged();
            }
        });


        final JButton cancel = button(new AbstractAction("Cancel") {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);

            }
        });

        final JButton addEngine = button(new AbstractAction("Add engine...") {
            @Override public void actionPerformed(ActionEvent e) {
                new AddEngineDialog(frame);
            }
        });

        final JButton deleteEngine = button (engineList.getDeleteEngineAction());

        strengthLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        frame = new JDialog(null, windowTitle, Dialog.ModalityType.APPLICATION_MODAL);
        frame.setLayout(new JsbGridLayout(1));
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.add(
                vBox(
                        grid(2, 0, -1,
                                wrap(type, engineList), wrap("Level", levels)
                        ),
                        strengthLabel,
                        buttonBar(false, addEngine, deleteEngine),
                        buttonBar(true, ok, cancel)
                )
        );
        frame.pack();
        frame.setVisible(false);

        frame.getRootPane().setDefaultButton(ok);
    }

    private void setSelectedEngine() {
        selectedEngine = engineList.getSelectedValue();
    }

    private void setStrength() {
        final EngineFactory selector = engineList.getSelectedValue();
        if (selector != null) {
            final Integer selectedLevel = levels.getSelectedValue();
            if (selectedLevel != null) {
                final String strength = selector.strengthEstimate(selectedLevel);
                strengthLabel.setText(strength);
            }
        }
    }

    /**
     * Notify all listeners that the opponent was changed.
     */
    private void fireOpponentChanged() {
        final List<Listener> listeners = getListeners();
        for (Listener listener : listeners) {
            listener.opponentChanged();
        }
    }

    /**
     * Select the User's preferred level both in the dialog box and in the
     * persistent variables (so it will be used even if the user presses "cancel").
     */
    private void selectUsersPreferredLevel() {
        selectedLevel = levelPref.get();
        levels.setSelectedIndex(findNearestLevel(selectedLevel, selectedEngine.availableLevels));
        selectedLevel = levels.getSelectedValue();
    }

    /**
     * Select the User's preferred engine both in the dialog box and in the
     * persistent variables (so it will be used even if the user presses "cancel").
     */
    private void selectUsersPreferredEngine() {
        final String preferredEngineName = enginePref.get();
        final int i = engineList.find(preferredEngineName);
        engineList.setSelectedIndex(Math.max(0, i));
        setSelectedEngine();
    }

    /**
     * Outline a list with a scrollPane which has a titled border
     *
     * @param title bordered title
     * @param list  list
     * @return the scrollPane
     */
    private static JComponent wrap(String title, JList list) {
        final Dimension preferredSize = list.getPreferredSize();
        list.setBorder(null);
        final JLabel jLabel = new JLabel(title);
        jLabel.setFont(UIManager.getFont("TitledBorder.font"));
        final int minWidth = 50 + Math.max(preferredSize.width, jLabel.getPreferredSize().width);
        final JScrollPane scrollPane = scrollPane(list);
        scrollPane.setPreferredSize(new Dimension(minWidth, 50 + preferredSize.height));
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), title));
        return scrollPane;
    }

    /**
     * Set the contents of the ListModel to the given levels.
     */
    private static void setLevelElements(DefaultListModel<Integer> ListModel, Integer[] levels) {
        ListModel.removeAllElements();
        for (Integer level : levels) {
            ListModel.addElement(level);
        }
    }

    /**
     * Find the index of the highest level <= targetLevel.
     * <p/>
     * This implementation assumes the levels are in order.
     *
     * @param targetLevel desired search depth
     * @param levels      available search depth
     * @return index of search depth
     */
    private static int findNearestLevel(int targetLevel, Integer[] levels) {
        int i;
        for (i = 0; i < levels.length; i++) {
            if (levels[i] > targetLevel) {
                break;
            }
        }
        if (i > 0) {
            i--;
        }

        return i;
    }

    /**
     * Display this window
     */
    public void show() {
        frame.setVisible(true);
    }

    /**
     * Nuke engine selectors
     */
    public static void main(String[] args) throws BackingStoreException {
        ExternalEngineManager.instance.removeAll();
    }

    @NotNull @Override public OpponentSelection getOpponent() {
        return new OpponentSelection(selectedEngine, selectedLevel);
    }

}
