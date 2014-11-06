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

package com.welty.nboard.nboard;

import com.welty.nboard.gui.AutoRadioGroup;
import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.nboard.engine.EngineSynchronizer;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static com.welty.nboard.nboard.GraphicsUtils.setPlainFont;
import static com.welty.othello.thor.ThorOpeningMap.OpeningCodeFromGgf;
import static com.welty.othello.thor.ThorOpeningMap.OpeningName;

/**
 * Panel that displays the move buttons and the opening name
 */
class NavigationBar extends JPanel {

    private static final int preferredHeight = 24;
    private static final int openingWidth = 120;

    NavigationBar(@NotNull ReversiData reversiData, @NotNull AutoRadioGroup modes, EnginePack opponent, EnginePack analyst) {
        setLayout(new BorderLayout());

        add(createButtonPanel(reversiData), BorderLayout.LINE_START);
        add(new EngineField(modes, opponent, analyst));
        add(createOpeningField(reversiData), BorderLayout.LINE_END);

        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    }

    private static @NotNull JLabel createOpeningField(final @NotNull ReversiData reversiData) {
        final JLabel openingField = new JLabel();
        openingField.setPreferredSize(new Dimension(openingWidth, preferredHeight));
        setPlainFont(openingField);
        openingField.setHorizontalAlignment(SwingConstants.TRAILING);

        reversiData.addListener(new SignalListener<OsMoveListItem>() {

            public void handleSignal(OsMoveListItem data) {
                final String sGgfGame = reversiData.getGame().toString();
                final int openingCode = OpeningCodeFromGgf(sGgfGame);
                openingField.setText(OpeningName(openingCode));
            }
        });

        return openingField;
    }

    private static JPanel createButtonPanel(final @NotNull ReversiData reversiData) {
        final JPanel buttonPanel = new JPanel();
        final FlowLayout flowLayout = (FlowLayout) (buttonPanel.getLayout());
        flowLayout.setHgap(0);
        addButton(buttonPanel, "first", KeyEvent.VK_UP, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reversiData.First();
            }
        });
        addButton(buttonPanel, "undo", KeyEvent.VK_LEFT, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reversiData.Undo();
            }
        });
        addButton(buttonPanel, "redo", KeyEvent.VK_RIGHT, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reversiData.Redo();
            }
        });
        addButton(buttonPanel, "last", KeyEvent.VK_DOWN, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reversiData.Last();
            }
        });
        return buttonPanel;
    }

    private static void addButton(JPanel buttonPanel, String iconName, int mnemonic, ActionListener listener) {
        final ImageIcon icon = NBoard.getImage(iconName + ".GIF");
        final JButton button = new JButton(icon);
        button.setMnemonic(mnemonic);
        button.addActionListener(listener);
        button.setPreferredSize(new Dimension(20, 20));
        buttonPanel.add(button);
    }

    private static class EngineField extends JLabel {
        private final AutoRadioGroup modes;
        private final EnginePack opponent;
        private final EnginePack analyst;

        public EngineField(AutoRadioGroup modes, EnginePack opponent, EnginePack analyst) {
            this.modes = modes;
            this.opponent = opponent;
            this.analyst = analyst;

            setPlainFont(this);

            modes.addListener(new AutoRadioGroup.Listener() {
                @Override public void selectionChanged(int index) {
                    setEngineField();
                }
            });

            final EngineSynchronizer.NameListener listener = new EngineSynchronizer.NameListener() {
                @Override public void nameChanged(@NotNull String engineName) {
                    setEngineField();
                }
            };
            opponent.engine.getNameListenerManager().addListener(listener);
            analyst.engine.getNameListenerManager().addListener(listener);

            setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
            setEngineField();
        }

        private void setEngineField() {
            final int index = modes.getIndex();
            if (index == 0) {
                setText(analyst.getName() + " analyzing");
            } else {
                setText("Playing " + opponent.getName());
            }
        }

    }
}
