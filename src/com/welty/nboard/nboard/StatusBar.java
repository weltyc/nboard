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
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 20, 2009
 * Time: 9:33:29 AM
 * To change this template use File | Settings | File Templates.
 */
class StatusBar extends JPanel {
    private final ReversiData m_pd;
    private final JLabel statusField = new JLabel();
    private final JLabel openingField = new JLabel();

    private static final int preferredHeight = 24;
    private static final int openingWidth = 120;
    private static final int statusWidth = 100;

    StatusBar(ReversiData d) {
        m_pd = d;
        m_pd.addListener(new SignalListener<OsMoveListItem>() {

            public void handleSignal(OsMoveListItem data) {
                final String sGgfGame = m_pd.Game().toString();
                final int openingCode = OpeningCodeFromGgf(sGgfGame);
                openingField.setText(OpeningName(openingCode));
            }
        });

        setLayout(new BorderLayout());

        add(createButtonPanel(), BorderLayout.LINE_START);

        createOpeningField();
        add(openingField, BorderLayout.LINE_END);

        createStatusField();
        add(statusField, BorderLayout.CENTER);
    }

    private void createStatusField() {
        statusField.setPreferredSize(new Dimension(statusWidth, preferredHeight));
        setPlainFont(statusField);
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
                m_pd.First();
            }
        });
        addButton(buttonPanel, "undo", KeyEvent.VK_LEFT, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Undo();
            }
        });
        addButton(buttonPanel, "redo", KeyEvent.VK_RIGHT, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Redo();
            }
        });
        addButton(buttonPanel, "last", KeyEvent.VK_DOWN, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                m_pd.Last();
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

    void SetStatus(String status) {
        statusField.setText(status);
    }
}
