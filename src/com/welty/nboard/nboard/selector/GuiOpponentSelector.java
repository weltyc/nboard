package com.welty.nboard.nboard.selector;

import com.orbanova.common.jsb.JsbGridLayout;
import com.welty.novello.eval.SimpleEval;
import com.welty.othello.api.OpponentSelection;
import com.welty.othello.api.OpponentSelector;
import com.welty.othello.gui.ExternalEngineManager;
import com.welty.othello.gui.prefs.PrefInt;
import com.welty.othello.gui.prefs.PrefString;
import com.welty.othello.gui.selector.EngineSelector;
import com.welty.othello.gui.selector.InternalEngineSelector;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static com.orbanova.common.jsb.JSwingBuilder.*;

/**
 * A Window that allows the user to select an Opponent to play
 */
public class GuiOpponentSelector extends OpponentSelector {

    private static final PrefInt levelPref = new PrefInt(GuiOpponentSelector.class, "Level", 1);
    private static final PrefString enginePref = new PrefString(GuiOpponentSelector.class, "Opponent", "Abigail");
    /**
     * Data used to initialize engineSelectors on startup.
     */
    private static final java.util.List<EngineSelector> INTERNAL_ENGINE_SELECTORS = internalOpponentSelectors();

    /**
     * Create a list of internal opponent selectors
     *
     * @return the list
     */
    public static List<EngineSelector> internalOpponentSelectors() {
        final ArrayList<EngineSelector> selectors = new ArrayList<>();

        for (String name : SimpleEval.getEvalNames()) {
            selectors.add(new InternalEngineSelector(name));
        }
        selectors.add(new InternalEngineSelector("Vegtbl", true, "d2", ""));

        return selectors;
    }


    private static GuiOpponentSelector instance;

    private final JDialog frame;
    private final JList<Integer> levels = new JList<>();
    static final EngineListModel engineListModel = new EngineListModel(INTERNAL_ENGINE_SELECTORS);
    private final JList<EngineSelector> engineSelectors = new JList<>(engineListModel);

    // these are written to when the user clicks "OK"
    private int selectedLevel;
    private @NotNull EngineSelector selectedEngine;

    public synchronized static GuiOpponentSelector getInstance() {
        if (instance == null) {
            instance = new GuiOpponentSelector();
        }
        return instance;
    }

    private GuiOpponentSelector() {
        // Level selection list box.
        // Need to create this before Opponent selection list box because the
        // Opponent selection list box modifies it.
        final DefaultListModel<Integer> levelModel = new DefaultListModel<>();
        setLevelElements(levelModel, EngineSelector.advancedLevels);
        levels.setModel(levelModel);
        setUpList(levels);
        levels.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        levels.setVisibleRowCount(EngineSelector.advancedLevels.length / 2);


        // Opponent selection list box.
        engineSelectors.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    final EngineSelector engineSelector = engineSelectors.getSelectedValue();
                    setLevelElements(levelModel, engineSelector.availableLevels);
                    levels.setSelectedIndex(findNearestLevel(selectedLevel, engineSelector.availableLevels));
                }
            }
        });
        setUpList(engineSelectors);

        selectUsersPreferredEngine();
        selectUsersPreferredLevel();


        final JButton ok = button(new AbstractAction("OK") {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                selectedLevel = levels.getSelectedValue();
                levelPref.put(levels.getSelectedValue());
                selectedEngine = engineSelectors.getSelectedValue();
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

        frame = new JDialog(null, "Select Opponent", Dialog.ModalityType.APPLICATION_MODAL);
        frame.setLayout(new JsbGridLayout(1));
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.add(
                vBox(
                        grid(2, 0, -1,
                                wrap("Opponent", engineSelectors), wrap("Level", levels)
                        ),
                        buttonBar(false, addEngine),
                        buttonBar(true, ok, cancel)
                )
        );
        frame.pack();
        frame.setVisible(false);

        frame.getRootPane().setDefaultButton(ok);
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

    private void selectUsersPreferredLevel() {
        selectedLevel = levelPref.get();
        levels.setSelectedIndex(findNearestLevel(selectedLevel, selectedEngine.availableLevels));
        selectedLevel = levels.getSelectedValue();
    }

    /**
     * Select the User's preferred engine both in the dialog box and in the data model.
     */
    private void selectUsersPreferredEngine() {
        final String preferredEngineName = enginePref.get();
        final int i = engineListModel.find(preferredEngineName);
        engineSelectors.setSelectedIndex(Math.max(0, i));
        selectedEngine = engineSelectors.getSelectedValue();
    }

    private static JComponent wrap(String title, JList list) {
        final Dimension preferredSize = list.getPreferredSize();
        list.setBorder(null);
        final JLabel jLabel = new JLabel(title);
        jLabel.setFont(UIManager.getFont("TitledBorder.font"));
        final int minWidth = 50 + Math.max(preferredSize.width, jLabel.getPreferredSize().width);
        System.out.println("min width = " + minWidth);
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

    private static <T> void setUpList(JList<T> ops) {
        ops.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        ops.setFont(UIManager.getFont("TextField.font"));
        ops.setAlignmentY(0.0f);
        ops.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ops.setSelectedIndex(0);
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
        ExternalEngineManager.removeAll();
    }

    @NotNull @Override public OpponentSelection getOpponent() {
        return new OpponentSelection(selectedEngine, selectedLevel);
    }
}
