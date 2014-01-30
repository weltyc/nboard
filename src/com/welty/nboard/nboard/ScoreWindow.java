package com.welty.nboard.nboard;

import com.welty.othello.gdk.OsBoard;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gdk.PieceCounts;
import static com.welty.nboard.nboard.GraphicsUtils.setPlainFont;
import com.welty.nboard.gui.SignalListener;

import javax.swing.*;
import java.awt.*;

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
        final PlayerPanel blackPanel = new PlayerPanel(height, "2", "smallBlack.GIF");
        add(blackPanel, BorderLayout.LINE_START);
        final PlayerPanel whitePanel = new PlayerPanel(height, "2", "smallWhite.GIF");
        add(whitePanel, BorderLayout.LINE_END);
        final PlayerPanel emptiesPanel = new PlayerPanel(height, "60", "smallEmpty.PNG");
        add(emptiesPanel, BorderLayout.CENTER);

        final Dimension size = new Dimension(ReversiWindow.boardFrameSize, height);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        m_pd.AddListener(new SignalListener<COsMoveListItem>() {
            public void handleSignal(COsMoveListItem data) {
                OsBoard board = m_pd.DisplayedPosition().board;
                final PieceCounts pieceCounts = board.getPieceCounts();
                blackPanel.player.setText(m_pd.Game().pis[1].sName);
                blackPanel.score.setText(Integer.toString(pieceCounts.nBlack));
                whitePanel.player.setText(m_pd.Game().pis[0].sName);
                whitePanel.score.setText(Integer.toString(pieceCounts.nWhite));
                emptiesPanel.score.setText(Integer.toString(pieceCounts.nEmpty));
                if (board.GameOver()) {
                    blackPanel.setBackground(null);
                    whitePanel.setBackground(null);
                } else if (board.blackMove()) {
                    blackPanel.setBackground(Color.RED);
                    whitePanel.setBackground(null);
                } else {
                    whitePanel.setBackground(Color.RED);
                    blackPanel.setBackground(null);
                }
                repaint();
            }
        });
    }

    private class PlayerPanel extends JPanel {
        final JLabel score = new JLabel();
        final JLabel player;

        PlayerPanel(int height, String scoreText, String iconName) {
            ImageIcon icon = NBoard.getImage(iconName);
            if (iconName.equals("smallEmpty.PNG")) {
                player = new JLabel(icon, SwingConstants.LEFT);
                player.setPreferredSize(new Dimension(20, height));
            } else {
                player = new JLabel("", icon, SwingConstants.LEFT);
                player.setPreferredSize(new Dimension(140, height));
            }

            score.setText(scoreText);
            setPlainFont(score);
            score.getPreferredSize();
            score.setPreferredSize(new Dimension(20, height));
            score.setHorizontalAlignment(SwingConstants.TRAILING);
            score.setVerticalAlignment(SwingConstants.TOP);
            add(score);

            player.setVerticalAlignment(SwingConstants.TOP);
            setPlainFont(player);
            add(player);

            setVisible(true);
        }
    }
}
