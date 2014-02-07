package com.welty.nboard.nboard.engine;

import com.welty.othello.api.OpponentSelector;
import com.welty.othello.api.PingApiEngine;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;

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
    private final PingApiEngine pingEngine;
    private final OpponentSelector opponentSelector;

    public EngineSynchronizer(PingApiEngine pingEngine, OpponentSelector opponentSelector) {
        this.pingEngine = pingEngine;
        this.opponentSelector = opponentSelector;
        opponentSelector.addListener(this);
        pingEngine.addListener(new MyListener());
        pingEngine.setMaxDepth(++m_ping, opponentSelector.getOpponent().getMaxDepth());
    }

    @Override public void sendMove(OsMoveListItem mli) {
        pingEngine.sendMove(++m_ping, mli);
    }

    @Override public void setGame(COsGame game) {
        pingEngine.setGame(++m_ping, game);
    }

    @Override public String getName() {
        return pingEngine.getName();
    }

    @Override public void setContempt(int contempt) {
        pingEngine.setContempt(++m_ping, contempt);
    }

    @Override public void learn() {
        pingEngine.learn();
    }

    @Override public boolean isReady() {
        return m_pong >= m_ping;
    }

    @Override public void requestHints(int nHints) {
        pingEngine.requestHints(nHints);
    }

    @Override public void requestMove() {
        pingEngine.requestMove();
    }

    @Override public void opponentChanged() {
        final int maxDepth = opponentSelector.getOpponent().getMaxDepth();
        pingEngine.setMaxDepth(++m_ping, maxDepth);
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


    private class MyListener implements PingApiEngine.Listener {
        @Override public void statusChanged() {
            fireStatus(pingEngine.getStatus());
        }

        @Override public void engineMove(int pong, OsMoveListItem mli) {
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
