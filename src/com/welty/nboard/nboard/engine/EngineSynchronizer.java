package com.welty.nboard.nboard.engine;

import com.welty.othello.api.*;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
    private final PingPong pingPong = new PingPong();

    private final @NotNull MultiEngine multiEngine;
    private final OpponentSelector opponentSelector;

    public EngineSynchronizer(OpponentSelector opponentSelector) throws IOException {
        final StatelessEngine firstEngine = opponentSelector.getOpponent().getOrCreateEngine();
        this.multiEngine = new MultiEngine(firstEngine);
        this.opponentSelector = opponentSelector;
        opponentSelector.addListener(this);
        multiEngine.addListener(new MyListener());
    }

    @Override public synchronized String getName() {
        return multiEngine.getName();
    }

    @Override public synchronized void learn(@NotNull SearchState state) {
        multiEngine.learn(pingPong, state);
    }

    @Override public synchronized boolean isReady() {
        return multiEngine.isReady();
    }

    @Override public synchronized void requestHints(@NotNull SearchState state, int nHints) {
        multiEngine.requestHints(pingPong, state, nHints);
    }

    @Override public synchronized void requestMove(@NotNull SearchState state) {
        multiEngine.requestMove(pingPong, state);
    }

    @Override public synchronized void opponentChanged() {
        final OpponentSelection opponent = opponentSelector.getOpponent();
        try {
            final StatelessEngine newEngine = opponent.getOrCreateEngine();
            multiEngine.setEngine(pingPong, newEngine);
        } catch (IOException e) {
            // keep using the existing engine.
            fireEngineError("Unable to start up " + opponent + ": " + e);
        }
    }

    /**
     * Update the pong.
     *
     * @return true if this pong is current
     */
    public boolean isCurrent(int pong) {
        return pong == pingPong.get();
    }


    private class MyListener implements StatelessEngine.Listener {
        @Override public void statusChanged() {
            synchronized (EngineSynchronizer.this) {
                fireStatus(multiEngine.getStatus());
            }
        }

        @Override public void engineMove(int pong, OsMoveListItem mli) {
            synchronized (EngineSynchronizer.this) {
                if (isCurrent(pong)) {
                    fireEngineMove(mli);
                }
            }
        }

        @Override public void engineReady(int pong) {
            synchronized (EngineSynchronizer.this) {
                if (isCurrent(pong)) {
                    fireStatus("");
                    fireEngineReady();
                }
            }
        }

        @Override public void hint(int pong, boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
            synchronized (EngineSynchronizer.this) {
                if (isCurrent(pong)) {
                    fireHint(fromBook, pv, move, eval, nGames, depth, freeformText);
                }
            }
        }

        @Override public void parseError(int pong, String command, String errorMessage) {
            synchronized (EngineSynchronizer.this) {
                if (isCurrent(pong)) {
                    fireParseError(command, errorMessage);
                }
            }
        }
    }


    /**
     * Notify listeners of a status update
     *
     * @param status status text
     */
    protected void fireStatus(String status) {
        for (Listener l : getListeners()) {
            l.status(status);
        }
    }

    /**
     * Notify listeners that the engine moved
     *
     * @param mli move
     */
    protected void fireEngineMove(OsMoveListItem mli) {
        for (Listener l : getListeners()) {
            l.engineMove(mli);
        }
    }

    /**
     * Notify listeners of an error.
     *
     * @param message error message.
     */
    protected void fireEngineError(String message) {
        for (Listener l : getListeners()) {
            l.engineError(message);
        }
    }

    /**
     * Notify listeners that the engine is ready to accept commands
     */
    protected void fireEngineReady() {
        for (Listener l : getListeners()) {
            l.engineReady();
        }
    }

    protected void fireHint(boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
        for (Listener l : getListeners()) {
            l.hint(fromBook, pv, move, eval, nGames, depth, freeformText);
        }
    }

    protected void fireParseError(String command, String errorMessage) {
        for (Listener l : getListeners()) {
            l.parseError(command, errorMessage);
        }
    }
}
