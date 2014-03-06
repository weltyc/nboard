package com.welty.nboard.nboard.setup;

import com.welty.novello.core.Position;

class SetUpData {
    final char[] pieces = new char[64];

    SetUpData() {
        final String text = Position.START_POSITION.boardString("");
        final char[] chars = text.toCharArray();
        System.arraycopy(chars, 0, pieces, 0, chars.length);
    }
}
