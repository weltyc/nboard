package com.welty.nboard.nboard.engine;

import com.welty.othello.api.ApiEngine;
import com.welty.othello.api.OpponentSelector;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;

/**
 * This class includes functions to help ensure synchronization of the board state with
 * the ReversiWindow. To ensure synchronization of the board state:
 * <p/>
 * - Call ping() immediately before any command that changes the board state.
 * <p/>
 * - The function isReady() returns true if all pings have been accepted by the engine.
 * <p/>
 * - If isReady() returns true then messages from the engine relate to the current board state.
 * <p/>
 * - If isReady() returns false then messages from the engine relate to a previous board state and can be ignored.
 */
public class EngineSynchronizer extends ReversiWindowEngine implements OpponentSelector.Listener {
    private int m_ping;
    private int m_pong;
    private final ParsedEngine parsedEngine;
    private final OpponentSelector opponentSelector;

    public EngineSynchronizer(ParsedEngine parsedEngine, OpponentSelector opponentSelector) {
        this.parsedEngine = parsedEngine;
        this.opponentSelector = opponentSelector;
        opponentSelector.addListener(this);
        parsedEngine.addListener(new MyListener());
        parsedEngine.setMaxDepth(++m_ping, opponentSelector.getOpponent().getMaxDepth());
    }

    @Override public void sendMove(COsMoveListItem mli) {
        parsedEngine.sendMove(++m_ping, mli);
    }

    @Override public void setGame(COsGame game) {
        parsedEngine.setGame(++m_ping, game);
    }

    @Override public String getName() {
        return parsedEngine.getName();
    }

    @Override public void setContempt(int contempt) {
        parsedEngine.setContempt(++m_ping, contempt);
    }

    @Override public void learn() {
        parsedEngine.learn();
    }

    @Override public boolean isReady() {
        return m_pong >= m_ping;
    }

    @Override public void requestHints(int nHints) {
        parsedEngine.requestHints(nHints);
    }

    @Override public void requestMove() {
        parsedEngine.requestMove();
    }

    @Override public void opponentChanged() {
        final int maxDepth = opponentSelector.getOpponent().getMaxDepth();
        parsedEngine.setMaxDepth(++m_ping, maxDepth);
    }

    /**
     * Update the pong.
     *
     * @return true if the engine is ready to accept commands (it has responded to all pings)
     */
    public boolean update(int pong) {
        m_pong = pong;
        return isReady();
    }


    private class MyListener implements ApiEngine.Listener {
        @Override public void status(int pong, String status) {
            fireStatus(status);
        }

        @Override public void engineMove(int pong, COsMoveListItem mli) {
            if (update(pong)) {
                fireEngineMove(mli);
            }
        }

        @Override public void pong(int pong) {
            if (update(pong)) {
                fireStatus("");
                fireEngineReady();
            }
        }

        @Override public void hint(int pong, boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
            if (update(pong)) {
                fireHint(fromBook, pv, move, eval, nGames, depth, freeformText);
            }
        }

        @Override public void parseError(int pong, String command, String errorMessage) {
            if (update(pong)) {
                fireParseError(command, errorMessage);
            }
        }
    }
}
