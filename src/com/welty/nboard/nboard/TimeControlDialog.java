package com.welty.nboard.nboard;

import com.orbanova.common.date.Time;
import com.orbanova.common.jsb.Grid;
import com.orbanova.common.jsb.JSwingBuilder;
import com.welty.othello.gui.prefs.PrefInt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;

/**
 * Controls a dialog box that requests the game's time control.
 */
class TimeControlDialog {
    /**
     * The dialog box to be displayed
     */
    private final JFrame dialog;

    /**
     * The text field within the dialog box
     */
    private final JFormattedTextField jtf = createNumericTextField();

    /**
     * Time control, in minutes
     */
    private final PrefInt timeControl = new PrefInt(TimeControlDialog.class, "timeControl", 5);

    TimeControlDialog() {
        JButton ok = JSwingBuilder.button(new AbstractAction("Ok") {
            @Override public void actionPerformed(ActionEvent e) {
                timeControl.put(Integer.parseInt(jtf.getText()));
                hide();
            }
        });
        JButton cancel = JSwingBuilder.button(new AbstractAction("Cancel") {
            @Override public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        final Grid<Component> interior = JSwingBuilder.vBox(
                JSwingBuilder.hBox(
                        new JLabel("Time Control, in minutes:"), jtf
                ).spacing(5),
                JSwingBuilder.buttonBar(true, ok, cancel)
        ).spacing(5).border(5);
        dialog = JSwingBuilder.frame("Time Control", WindowConstants.HIDE_ON_CLOSE, false, interior);
        dialog.getRootPane().setDefaultButton(ok);
        dialog.pack();
    }

    private static JFormattedTextField createNumericTextField() {
        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        JFormattedTextField jtf = new JFormattedTextField(format);
        jtf.setColumns(5);
        jtf.setHorizontalAlignment(JTextField.RIGHT);
        return jtf;
    }

    private void hide() {
        dialog.setVisible(false);
    }

    void show() {
        jtf.setText("" + timeControl.get());
        jtf.selectAll();
        dialog.setVisible(true);
    }

    /**
     * Get the time control for the game.
     *
     * @return time control, in millis.
     */
    public long getMillis() {
        return timeControl.get() * Time.MINUTE;
    }
}
