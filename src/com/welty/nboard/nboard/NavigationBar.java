package com.welty.nboard.nboard;

import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.OsMoveListItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static com.welty.nboard.nboard.GraphicsUtils.setPlainFont;
import static com.welty.nboard.thor.ThorOpeningMap.OpeningCodeFromGgf;
import static com.welty.nboard.thor.ThorOpeningMap.OpeningName;

/**
 * Panel that displays the move buttons and the opening name
 */
class NavigationBar extends JPanel {
    private final ReversiData reversiData;
    private final JLabel openingField = new JLabel();

    private static final int preferredHeight = 24;
    private static final int openingWidth = 120;

    NavigationBar(ReversiData reversiData) {
        this.reversiData = reversiData;
        this.reversiData.addListener(new SignalListener<OsMoveListItem>() {

            public void handleSignal(OsMoveListItem data) {
                final String sGgfGame = NavigationBar.this.reversiData.getGame().toString();
                final int openingCode = OpeningCodeFromGgf(sGgfGame);
                openingField.setText(OpeningName(openingCode));
            }
        });

        setLayout(new BorderLayout());

        add(createButtonPanel(), BorderLayout.LINE_START);

        createOpeningField();
        add(openingField, BorderLayout.LINE_END);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    }

    private void createOpeningField() {
        openingField.setPreferredSize(new Dimension(openingWidth, preferredHeight));
        setPlainFont(openingField);
        openingField.setHorizontalAlignment(SwingConstants.TRAILING);
    }

    private JPanel createButtonPanel() {
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

    private void addButton(JPanel buttonPanel, String iconName, int mnemonic, ActionListener listener) {
        final ImageIcon icon = NBoard.getImage(iconName + ".GIF");
        final JButton button = new JButton(icon);
        button.setMnemonic(mnemonic);
        button.addActionListener(listener);
        button.setPreferredSize(new Dimension(20, 20));
        buttonPanel.add(button);
    }
}
