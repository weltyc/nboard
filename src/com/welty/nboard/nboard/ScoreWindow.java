package com.welty.nboard.nboard;

import com.orbanova.common.jsb.Grid;
import com.welty.nboard.gui.SignalListener;
import com.welty.othello.gdk.*;

import javax.swing.*;
import java.awt.*;

import static com.welty.nboard.nboard.GraphicsUtils.setPlainFont;

/**
 * A score window is where the piece counts/player names/clocks are displayed
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 17, 2009
 * Time: 1:53:22 AM
 * To change this template use File | Settings | File Templates.
 */
class ScoreWindow extends JPanel {
    private final ReversiData m_pd;
    private static final int height = 24;

    //     \todo time remaining
    ScoreWindow(ReversiData pd) {
        m_pd = pd;
        setLayout(new BorderLayout());
        final COsPosition pos = pd.getGame().getPos();
        final PlayerPanel blackPanel = new PlayerPanel(height, "2", "smallBlack.GIF", pos.getBlackClock());
        add(blackPanel, BorderLayout.LINE_START);
        final PlayerPanel whitePanel = new PlayerPanel(height, "2", "smallWhite.GIF", pos.getWhiteClock());
        add(whitePanel, BorderLayout.LINE_END);
        final EmptiesPanel emptiesPanel = new EmptiesPanel(height);
        add(emptiesPanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

//        final Dimension size = new Dimension(ReversiWindow.boardFrameSize, height);
//        setPreferredSize(size);
//        setMaximumSize(size);
//        setMinimumSize(size);
        m_pd.addListener(new SignalListener<OsMoveListItem>() {
            public void handleSignal(OsMoveListItem data) {
                final COsPosition pos = m_pd.DisplayedPosition();
                OsBoard board = pos.board;
                final PieceCounts pieceCounts = board.getPieceCounts();
                blackPanel.player.setText(m_pd.getGame().pis[1].sName);
                blackPanel.score.setText(Integer.toString(pieceCounts.nBlack));
                whitePanel.player.setText(m_pd.getGame().pis[0].sName);
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
    }

    private static JLabel createScoreLabel(int height, String scoreText) {
        JLabel score = new JLabel();
        score.setText(scoreText);
        setPlainFont(score);
        score.getPreferredSize();
        score.setPreferredSize(new Dimension(20, height));
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

            this.clock = new JLabel(clock.toString());
            add(this.clock);

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
            this.clock.setText(clock.toString());
        }
    }
}
