package com.welty.nboard.gui;

import com.welty.nboard.nboard.NBoard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class MenuItemBuilder {
    private @NotNull final String text;
    private final int iMnemonic;
    private @Nullable String accelerator;
    private @Nullable String icon;

    private static final int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    /**
     * Create a MenuItemBuilder.
     * <p/>
     * textAndAccelerator contains
     * (a) the text of the menu item <br/>
     * (b) optionally, the character '&' which denotes the following character is the mnemonic <br/>
     * (c) optionally, '\t' followed by the accelerator. <br/>
     * <p/>
     * Valid formats for the accelerator are:
     * Ctrl+{upper char}, which is transformed to Ctrl+char on non-Macs, and Command+char on Macs
     * "up arrow", "down arrow", "left arrow", "right arrow" which are transformed to the appropriate arrow key
     *
     * @param textAndAccelerator as described above
     * @return a MenuItemBuilder
     */
    public static MenuItemBuilder menuItem(String textAndAccelerator) {
        return new MenuItemBuilder(textAndAccelerator);
    }

    /**
     * Add an icon to the menu item
     *
     * @param iconImageName icon image as loaded by {@link com.welty.nboard.nboard.NBoard#getImage(String)}
     * @return this, for chaining
     */
    public MenuItemBuilder icon(String iconImageName) {
        icon = iconImageName;
        return this;
    }

    /**
     * Build a standard JMenuItem (no check box, no radio button)
     *
     * @param listeners listeners for the menu item
     * @return the JMenuItem
     */
    public JMenuItem build(ActionListener... listeners) {
        return update(new JMenuItem(), listeners);
    }

    private <T extends JMenuItem> T update(T menuItem, ActionListener[] actionListeners) {
        menuItem.setText(text);
        if (iMnemonic >= 0) {
            final char mnemonic = Character.toUpperCase(text.charAt(iMnemonic));
            menuItem.setMnemonic(mnemonic);
        }
        if (accelerator != null) {
            if (accelerator.contains("+")) {
                final String[] parts = accelerator.split("\\+");

                int mask = 0;
                for (int i = 0; i < parts.length - 1; i++) {
                    switch (parts[i].toLowerCase()) {
                        case "ctrl":
                            mask |= shortcutKeyMask;
                            break;
                        case "shift":
                            mask |= Event.SHIFT_MASK;
                    }
                }
                final char accChar = Character.toUpperCase(parts[parts.length - 1].charAt(0));
                //noinspection MagicConstant
                menuItem.setAccelerator(KeyStroke.getKeyStroke(accChar, mask));
            } else if (accelerator.equals("up arrow")) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
            } else if (accelerator.equals("down arrow")) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
            } else if (accelerator.equals("left arrow")) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
            } else if (accelerator.equals("right arrow")) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
            } else {
                throw new IllegalArgumentException("bad accelerator : " + accelerator);
            }
        }

        if (icon != null) {
            menuItem.setIcon(NBoard.getImage(icon));
        }

        for (ActionListener actionListener : actionListeners) {
            menuItem.addActionListener(actionListener);
        }

        return menuItem;
    }

    /**
     * See {@link #menuItem(String)} for a description of textAndAccelerator
     */
    private MenuItemBuilder(@NotNull String textAndAccelerator) {
        String[] parts = textAndAccelerator.split("\t");
        iMnemonic = parts[0].indexOf('&');
        text = parts[0].replace("&", "");
        if (parts.length > 1) {
            accelerator = parts[1];
        }
    }

    /**
     * Build a JCheckBoxMenuItem
     *
     * @param listeners listeners for the menu item
     * @return the JMenuItem
     */
    public JCheckBoxMenuItem buildCheckBox(ActionListener... listeners) {
        return update(new JCheckBoxMenuItem(), listeners);
    }

    /**
     * Build a JRadioButtonMenuItem
     *
     * @param listeners listeners for the menu item
     * @return the JMenuItem
     */
    public JRadioButtonMenuItem buildRadioButton(ActionListener... listeners) {
        return update(new JRadioButtonMenuItem(), listeners);
    }
}
