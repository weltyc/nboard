package com.welty.nboard.nboard.selector;

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JsbTextField;
import com.welty.othello.core.OperatingSystem;
import com.welty.othello.gui.ExternalEngineManager;
import com.welty.othello.gui.selector.ExternalEngineSelector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;

import static com.orbanova.common.jsb.JSwingBuilder.*;

class AddEngineDialog extends JDialog {
    AddEngineDialog(Window parent) {
        super(parent, "Add Engine", ModalityType.APPLICATION_MODAL);
        final JsbTextField nameField = textField();
        final JsbTextField wdField = textField();
        wdField.setPreferredSize(new Dimension(300, wdField.getPreferredSize().height));
        final JsbTextField commandField = textField();
        final Grid<Component> controls = controlGrid(
                control("Name", nameField),
                control("Working Directory", wdField),
                control("Command", commandField)
        );
        final JButton ok = button(new AbstractAction("OK") {
            @Override public void actionPerformed(ActionEvent e) {
                final String name = nameField.getText();
                if (!name.matches("[a-zA-Z0-9]+")) {
                    JOptionPane.showMessageDialog(AddEngineDialog.this, "Engine name must be alphanumeric (all characters must be a-z, A-Z, or 0-9)");
                    return;
                }
                final String wd = wdField.getText();
                if (wd.contains(";")) {
                    JOptionPane.showMessageDialog(AddEngineDialog.this, "Working directory cannot contain a semicolon (;)");
                    return;
                }
                if (wd.isEmpty()) {
                    JOptionPane.showMessageDialog(AddEngineDialog.this, "Working directory must not be empty");
                    return;
                }
                final String command = commandField.getText().trim();
                if (command.isEmpty()) {
                    JOptionPane.showMessageDialog(AddEngineDialog.this, "Command must not be empty");
                    return;
                }
                ExternalEngineManager.add(name, wd, command);
                GuiOpponentSelector.engineListModel.put(new ExternalEngineSelector(name, wd, command));
                AddEngineDialog.this.setVisible(false);
                AddEngineDialog.this.dispose();
            }
        });

        final JButton cancel = button(new AbstractAction("Cancel") {
            @Override public void actionPerformed(ActionEvent e) {
                AddEngineDialog.this.setVisible(false);
                AddEngineDialog.this.dispose();
            }
        });

        final String osName = (OperatingSystem.os == OperatingSystem.MACINTOSH) ? "Mac" : "Win";
        final String helpFile = "OpponentSelectionWindow_" + osName + ".html";
        final InputStream in = GuiOpponentSelector.class.getResourceAsStream(helpFile);
        final String helpHtml = Feeds.ofLines(in).join("\n");

        add(vBox(
                controls,
                label(helpHtml),
                buttonBar(true, ok, cancel)
        ));

        getRootPane().setDefaultButton(ok);
        pack();
        setVisible(true);
    }
}
