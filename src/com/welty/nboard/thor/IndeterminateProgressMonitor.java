package com.welty.nboard.thor;

import com.orbanova.common.misc.Logger;
import com.welty.othello.core.Engineering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Window where progress information is displayed.
 *
 * Progress is displayed as a number (in Engineering format) followed by a suffix.
 * For example, "12,345k games loaded". In this case, " games loaded" is the suffix,
 * and "12,345k" is the Engineering representation of the number of games.
 */
public class IndeterminateProgressMonitor implements IndeterminateProgressTracker {
    private static final Logger log = Logger.logger(IndeterminateProgressMonitor.class);

    private final Timer timer;
    private final AtomicLong progress = new AtomicLong();
    private final JLabel label;
    private final JFrame frame;

    public IndeterminateProgressMonitor(final String suffix) {
        frame = new JFrame("Loading Games");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        label = new JLabel("0 " + suffix);
        label.setPreferredSize(new Dimension(200, 100));
        frame.add(label);
        frame.pack();

        timer = new Timer(1000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                label.setText(Engineering.engineeringLong(progress.get())+suffix);
                frame.setVisible(true);
                log.info("Timer updated");
            }
        });
        timer.setInitialDelay(1000);
        timer.start();
    }

    @Override public void increment() {
        progress.incrementAndGet();
    }

    @Override public void close() {
        timer.stop();
        frame.setVisible(false);
        frame.dispose();
    }
}
