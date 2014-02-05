package com.welty.nboard.nboard.engine;

import com.welty.othello.api.ApiEngine;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;

public class MultiEngine extends ReversiWindowEngine {
    private final ParsedEngine parsedEngine;

    public MultiEngine(ParsedEngine parsedEngine) {
        this.parsedEngine = parsedEngine;
        parsedEngine.addListener(new MyListener(parsedEngine));
    }

    @Override public void sendMove(COsMoveListItem mli) {
        parsedEngine.sendMove(mli);
    }

    @Override public void setGame(COsGame game) {
        parsedEngine.setGame(game);
    }

    @Override public String getName() {
        return parsedEngine.getName();
    }

    @Override public void setContempt(int contempt) {
        parsedEngine.setContempt(contempt);
    }

    @Override public void learn() {
        parsedEngine.learn();
    }

    @Override public boolean isReady() {
        return parsedEngine.isReady();
    }

    @Override public void requestHints(int nHints) {
        parsedEngine.requestHints(nHints);
    }

    @Override public void requestMove() {
        parsedEngine.requestMove();
    }

    private class MyListener implements ApiEngine.Listener {
        private final ParsedEngine parsedEngine;

        public MyListener(ParsedEngine parsedEngine) {
            this.parsedEngine = parsedEngine;
        }

        @Override public void status(String status) {
            fireStatus(status);
        }

        @Override public void engineMove(COsMoveListItem mli) {
            fireEngineMove(mli);
        }

        @Override public void engineReady() {
            fireEngineReady();
        }

        @Override public void hint(boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
            fireHint(fromBook, pv, move, eval, nGames, depth, freeformText);
        }

        @Override public void parseError(String command, String errorMessage) {
            fireParseError(command, errorMessage);
        }
    }
}
