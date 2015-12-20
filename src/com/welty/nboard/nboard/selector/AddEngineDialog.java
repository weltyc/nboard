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

import com.orbanova.common.feed.Feeds;
import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JsbTextField;
import com.welty.othello.core.OperatingSystem;
import com.welty.othello.gui.ExternalEngineManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
                final Path executable = Paths.get(wd).resolve(command.split("\\s+")[0]);
                if (!Files.exists(executable)) {
                    JOptionPane.showMessageDialog(AddEngineDialog.this, "Executable does not exist: " + executable);
                    return;
                }
                ExternalEngineManager.instance.add(name, wd, command);
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

        final String helpFile = "OpponentSelectionWindow_" + OperatingSystem.os + ".html";
        final InputStream in = AddEngineDialog.class.getResourceAsStream(helpFile);
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
