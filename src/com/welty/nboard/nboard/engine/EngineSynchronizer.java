package com.welty.nboard.nboard.engine;

import com.welty.othello.api.OpponentSelector;
import com.welty.othello.api.PingEngine;
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
    private final PingEngine pingEngine;
    private final OpponentSelector opponentSelector;

    public EngineSynchronizer(PingEngine pingEngine, OpponentSelector opponentSelector) {
        this.pingEngine = pingEngine;
        this.opponentSelector = opponentSelector;
        opponentSelector.addListener(this);
        pingEngine.addListener(new MyListener());
        pingEngine.setMaxDepth(++m_ping, opponentSelector.getOpponent().getMaxDepth());
    }

    @Override public synchronized void sendMove(OsMoveListItem mli) {
        pingEngine.sendMove(++m_ping, mli);
    }

    @Override public synchronized void setGame(COsGame game) {
        pingEngine.setGame(++m_ping, game);
    }

    @Override public synchronized String getName() {
        return pingEngine.getName();
    }

    @Override public synchronized void setContempt(int contempt) {
        pingEngine.setContempt(++m_ping, contempt);
    }

    @Override public synchronized void learn() {
        pingEngine.learn();
    }

    @Override public synchronized boolean isReady() {
        return m_pong >= m_ping;
    }

    @Override public synchronized void requestHints(int nHints) {
        pingEngine.requestHints(nHints);
    }

    @Override public synchronized void requestMove() {
        pingEngine.requestMove();
    }

    @Override public synchronized void opponentChanged() {
        final int maxDepth = opponentSelector.getOpponent().getMaxDepth();
        pingEngine.setMaxDepth(++m_ping, maxDepth);
    }

    /**
     * Update the pong.
     *
     * @return true if the engine is ready to accept commands (it has responded to all pings)
     */
    public synchronized boolean update(int pong) {
        m_pong = pong;
        return isReady();
    }


    private class MyListener implements PingEngine.Listener {
        @Override public void statusChanged() {
            synchronized (EngineSynchronizer.this) {
                fireStatus(pingEngine.getStatus());
            }
        }

        @Override public void engineMove(int pong, OsMoveListItem mli) {
            synchronized (EngineSynchronizer.this) {
                if (update(pong)) {
                    fireEngineMove(mli);
                }
            }
        }

        @Override public void pong(int pong) {
            synchronized (EngineSynchronizer.this) {
                if (update(pong)) {
                    fireStatus("");
                    fireEngineReady();
                }
            }
        }

        @Override public void hint(int pong, boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
            synchronized (EngineSynchronizer.this) {
                if (update(pong)) {
                    fireHint(fromBook, pv, move, eval, nGames, depth, freeformText);
                }
            }
        }

        @Override public void parseError(int pong, String command, String errorMessage) {
            synchronized (EngineSynchronizer.this) {
                if (update(pong)) {
                    fireParseError(command, errorMessage);
                }
            }
        }
    }
}
