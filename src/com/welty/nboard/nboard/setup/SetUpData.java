package com.welty.nboard.nboard.setup;

import com.welty.novello.core.Board;

class SetUpData {
    final char[] pieces = new char[64];

    SetUpData() {
        final String text = Board.START_BOARD.boardString("");
        final char[] chars = text.toCharArray();
        System.arraycopy(chars, 0, pieces, 0, chars.length);
    }
}
