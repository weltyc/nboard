package com.welty.nboard;

import com.welty.nboard.gui.RadioGroup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 23, 2009
 * Time: 8:14:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class DepthRadioGroup extends RadioGroup {
    // Depths for depth menu
    private static final int depths[] = {2, 3, 4, 6, 8, 12, 16, 20, 22, 26, 30};
    private int m_userDefinedDepth;
    private static final String userDepthKey = "Engine/UserDefinedDepth";
    private final ReversiWindow window;

    public DepthRadioGroup(ReversiWindow window, JMenu menu, List<Runnable> shutdownHooks) {
        this(window, menu, shutdownHooks, createMenuItems());
    }

    private DepthRadioGroup(final ReversiWindow window, JMenu menu, List<Runnable> shutdownHooks, JRadioButtonMenuItem[] items) {
        super(menu, "Engine/Depth", 2, shutdownHooks, items);
        this.window = window;
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
            depthMenuItems.add(ReversiWindow.createRadioButtonMenuItem("" + depth));
        }
        depthMenuItems.add(ReversiWindow.createRadioButtonMenuItem("Other..."));
        return depthMenuItems.toArray(new JRadioButtonMenuItem[depthMenuItems.size()]);
    }
}
