package com.welty.nboard.nboard;

import com.orbanova.common.jsb.Grid;
import com.orbanova.common.misc.Engineering;

import javax.swing.*;
import java.awt.*;

public class NodeCountPanel extends Grid<JComponent> {
    final JLabel nodeCount;
    final JLabel nodeTime;
    final JLabel nps;

    private NodeCountPanel(JLabel nodeCount, JLabel nodeTime, JLabel nps) {
        super(3, nodeCount, nodeTime, nps);
        this.nodeCount = nodeCount;
        this.nodeTime = nodeTime;
        this.nps = nps;
        setBorder(BorderFactory.createLoweredBevelBorder());
    }

    public static NodeCountPanel of() {
        final JLabel nodeCount = NBoard.createLabel(70, SwingConstants.RIGHT);
        final JLabel nodeTime = NBoard.createLabel(60, SwingConstants.RIGHT);
        final JLabel nps = NBoard.createLabel(70, SwingConstants.RIGHT);
        final Font nodeCountFont = nodeCount.getFont().deriveFont(Font.PLAIN, 10.f);
        setup(nodeCount, nodeCountFont);
        setup(nodeTime, nodeCountFont);
        setup(nps, nodeCountFont);

        return new NodeCountPanel(nodeCount, nodeTime, nps);
    }

    private static void setup(JLabel label, Font nodeCountFont) {
        label.setFont(nodeCountFont);
        label.setAlignmentX(1.0f);
    }

    public void nodeStats(long nNodes, double tElapsed) {
        nodeCount.setText(Engineering.compactFormat(nNodes) + "n");
        nodeTime.setText(Engineering.compactFormat(tElapsed) + "s");
        final String npsText = tElapsed == 0 ? "" : Engineering.compactFormat(nNodes / tElapsed) + "n/s";
        nps.setText(npsText);
    }
}
