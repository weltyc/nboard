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

package com.welty.nboard.nboard.transcript;

import com.orbanova.common.feed.Feeds;

import javax.swing.*;

class ErrorLabel extends JLabel implements TranscriptData.Listener {
    private final TranscriptData data;

    ErrorLabel(TranscriptData data) {
        this.data = data;
        transcriptDataUpdated();
        data.addListener(this);
    }

    @Override public void transcriptDataUpdated() {
        setText(Feeds.of(data.getErrors()).join("\n"));
    }
}
