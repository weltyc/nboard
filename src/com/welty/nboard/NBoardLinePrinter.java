package com.welty.nboard;

import com.welty.othello.lp.LinePrinter;
import com.welty.ntestj.CGameX;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 21, 2009
 * Time: 11:09:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class NBoardLinePrinter implements LinePrinter {

    public void println(Object msg) {
        try {
            final String line = msg.toString();
            if (line.length() > 65) {
                new RuntimeException().printStackTrace();
                JOptionPane.showConfirmDialog(null, "to NBoard: " + line);
            }
            CGameX.toNBoard.put(line);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
