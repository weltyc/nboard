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
