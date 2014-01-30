package com.welty.nboard.nboard;

import com.welty.nboard.gui.RadioGroup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static com.welty.nboard.gui.MenuItemBuilder.menuItem;

/**
 * User choices for engine max search depth
 */
public class DepthRadioGroup extends RadioGroup {
    // Depths for depth menu
    private static final int depths[] = {2, 3, 4, 6, 8, 12, 16, 20, 22, 26, 30};
    private int m_userDefinedDepth;
    private static final String userDepthKey = "Engine/UserDefinedDepth";

    public DepthRadioGroup(ReversiWindow window, JMenu menu, List<Runnable> shutdownHooks) {
        this(window, menu, shutdownHooks, createMenuItems());
    }

    private DepthRadioGroup(final ReversiWindow window, JMenu menu, List<Runnable> shutdownHooks, JRadioButtonMenuItem[] items) {
        super(menu, "Engine/Depth", 2, shutdownHooks, items);
        m_userDefinedDepth = NBoard.RegistryReadU4(userDepthKey, 5);
        for (int i = 0; i < items.length; i++) {
            ActionListener engineArbitraryDepthSetter = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final String text = JOptionPane.showInputDialog("Select Engine Depth (1-60): ", m_userDefinedDepth);
                    try {
                        final int depth = Integer.parseInt(text);
                        if (depth >= 1 && depth <= 60) {
                            m_userDefinedDepth = depth;
                            window.SetEngineDepth(depth);
                        }
                    }
                    catch (NumberFormatException ignore) {
                        // ignore, user should be able to figure this out
                    }
                }
            };
            ActionListener engineDepthSetter = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    window.SetEngineDepth(depths[getIndex()]);
                }
            };
            items[i].addActionListener(i < depths.length ? engineDepthSetter : engineArbitraryDepthSetter);
        }
    }

    public int getDepth() {
        final int index = getIndex();
        if (index >= depths.length) {
            return m_userDefinedDepth;
        } else {
            return depths[index];
        }
    }

    @Override public void writeIndex() {
        super.writeIndex();
        NBoard.RegistryWriteU4(userDepthKey, m_userDefinedDepth);
    }

    private static JRadioButtonMenuItem[] createMenuItems() {
        final ArrayList<JRadioButtonMenuItem> depthMenuItems = new ArrayList<JRadioButtonMenuItem>();
        for (final int depth : depths) {
            depthMenuItems.add(menuItem("" + depth).buildRadioButton());
        }
        depthMenuItems.add(menuItem("Other...").buildRadioButton());
        return depthMenuItems.toArray(new JRadioButtonMenuItem[depthMenuItems.size()]);
    }
}
