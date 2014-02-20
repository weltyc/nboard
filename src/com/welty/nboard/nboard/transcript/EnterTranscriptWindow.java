package com.welty.nboard.nboard.transcript;

import com.orbanova.common.jsb.JSwingBuilder;
import com.welty.othello.gdk.COsGame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class EnterTranscriptWindow {
    private final JFrame window;
    final TranscriptData data = new TranscriptData();
    private final EnterTranscriptPanel panel;

    public EnterTranscriptWindow(final EnterTranscriptWindow.Listener listener) {
        JButton ok = new JButton(new AbstractAction("OK") {
            @Override public void actionPerformed(ActionEvent e) {
                ok(listener);
            }
        });
        JButton cancel = new JButton(new AbstractAction("Cancel") {
            @Override public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        JButton clear = new JButton(new AbstractAction("Clear") {
            @Override public void actionPerformed(ActionEvent e) {
                data.clearBoard();
                panel.requestFocusInWindow();
            }
        });
        KeyListener keyListener = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        cancel();
                        break;
                    case KeyEvent.VK_ENTER:
                        ok(listener);
                        break;
                }
            }
        };
        panel = new EnterTranscriptPanel(data, keyListener);
        final JTextArea instructions = JSwingBuilder.textArea(false, "Enter the move number in the square.\n" +
                "Mouse click or arrow keys to move.\n" +
                "For single digit move numbers such as '4', type '04' or '4<space>'");

        window = JSwingBuilder.frame("Enter transcript", JFrame.HIDE_ON_CLOSE, false,
                JSwingBuilder.vBox(
                        instructions,
                        panel,
                        new ErrorLabel(data),
                        clear,
                        JSwingBuilder.buttonBar(true, ok, cancel)
                ).spacing(5).border(5)
        );
        instructions.setBackground(window.getBackground());
    }

    private void cancel() {
        window.setVisible(false);
    }

    private void ok(Listener listener) {
        listener.setGame(data.getGame());
        window.setVisible(false);
    }

    public void show() {
        window.setVisible(true);
        panel.requestFocusInWindow();

    }

    public interface Listener {
        void setGame(COsGame game);
    }

}

