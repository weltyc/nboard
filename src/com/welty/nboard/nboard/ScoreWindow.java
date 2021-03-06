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

import com.orbanova.common.jsb.Grid;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.welty.nboard.nboard.GraphicsUtils.setPlainFont;

/**
 * A score window is where the piece counts/player names/clocks are displayed
 */
class ScoreWindow extends JPanel {
    private final ReversiData reversiData;
    private final OptionSource optionSource;
    private static final int height = 24;
    private final PlayerPanel blackPanel;
    private final PlayerPanel whitePanel;

    ScoreWindow(ReversiData reversiData, OptionSource optionSource) {
        this.reversiData = reversiData;
        this.optionSource = optionSource;
        setLayout(new BorderLayout());
        final COsPosition pos = reversiData.getGame().getPos();
        blackPanel = new PlayerPanel(height, "2", "smallBlack.GIF", pos.getBlackClock());
        add(blackPanel, BorderLayout.LINE_START);
        whitePanel = new PlayerPanel(height, "2", "smallWhite.GIF", pos.getWhiteClock());
        add(whitePanel, BorderLayout.LINE_END);
        final EmptiesPanel emptiesPanel = new EmptiesPanel(height);
        add(emptiesPanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        this.reversiData.addListener(new SignalListener<OsMoveListItem>() {
            public void handleSignal(OsMoveListItem data) {
                final COsPosition pos = ScoreWindow.this.reversiData.DisplayedPosition();
                COsBoard board = pos.board;
                final PieceCounts pieceCounts = board.getPieceCounts();
                blackPanel.player.setText(ScoreWindow.this.reversiData.getGame().getBlackPlayer().name);
                blackPanel.score.setText(Integer.toString(pieceCounts.nBlack));
                whitePanel.player.setText(ScoreWindow.this.reversiData.getGame().getWhitePlayer().name);
                whitePanel.score.setText(Integer.toString(pieceCounts.nWhite));
                emptiesPanel.score.setText(Integer.toString(pieceCounts.nEmpty));
                if (board.isGameOver()) {
                    blackPanel.setBackground(null);
                    whitePanel.setBackground(null);
                } else if (board.isBlackMove()) {
                    blackPanel.setBackground(Color.RED);
                    whitePanel.setBackground(null);
                } else {
                    whitePanel.setBackground(Color.RED);
                    blackPanel.setBackground(null);
                }
                blackPanel.setClock(pos.getBlackClock());
                whitePanel.setClock(pos.getWhiteClock());
                repaint();
            }
        });

        new Timer(1000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                updateClocks();
            }
        }).start();
    }

    private void updateClocks() {
        if (!reversiData.isReviewing() && !reversiData.getGame().isOver() && !optionSource.isAnalyzing()) {
            final double tElapsed = reversiData.secondsSinceLastMove();
            final COsPosition pos = reversiData.getGame().getPos();
            if (pos.board.isBlackMove()) {
                blackPanel.setClock(pos.getBlackClock().update(tElapsed));
            } else {
                whitePanel.setClock(pos.getWhiteClock().update(tElapsed));
            }
        }
    }

    private static JLabel createScoreLabel(int height, String scoreText) {
        JLabel score = new JLabel();
        score.setText(scoreText);
        setPlainFont(score);
        score.getPreferredSize();
        score.setPreferredSize(new Dimension(20, height));
        score.setAlignmentX(1.0f);
        score.setHorizontalAlignment(SwingConstants.TRAILING);
        score.setVerticalAlignment(SwingConstants.TOP);
        return score;
    }

    private class EmptiesPanel extends Grid<JComponent> {
        final JLabel score;

        EmptiesPanel(int height) {
            super(2);
            this.score = createScoreLabel(height, "60");
            add(score);

            ImageIcon icon = NBoard.getImage("smallEmpty.PNG");
            JLabel player = new JLabel(icon, SwingConstants.LEFT);
            player.setPreferredSize(new Dimension(20, height));
            setPlainFont(player);
            add(player);
            spacing(3);
        }
    }

    private class PlayerPanel extends Grid<JComponent> {
        final JLabel score;
        final JLabel player;
        final JLabel clock;

        PlayerPanel(int height, String scoreText, String iconName, OsClock clock) {
            super(2);
            this.score = createScoreLabel(height, scoreText);
            add(score);

            this.player = createPlayerNameLabel(height, iconName);
            add(player);

            // spacer
            add(new JLabel(""));

            this.clock = new JLabel(clock.toDisplayString());
            add(this.clock);

            spacing(3, 0);
            setVisible(true);
        }

        private JLabel createPlayerNameLabel(int height, String iconName) {
            ImageIcon icon = NBoard.getImage(iconName);
            JLabel player;
            player = new JLabel("", icon, SwingConstants.LEFT);
            player.setPreferredSize(new Dimension(140, height));
            player.setVerticalAlignment(SwingConstants.TOP);
            setPlainFont(player);

            return player;
        }

        public void setClock(OsClock clock) {
            this.clock.setText(clock.toDisplayString());
        }
    }
}
