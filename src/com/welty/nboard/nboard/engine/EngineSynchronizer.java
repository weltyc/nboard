package com.welty.nboard.nboard.engine;

import com.orbanova.common.misc.Require;
import com.welty.othello.api.*;
import com.welty.othello.core.CMove;
import com.welty.othello.protocol.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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
public class EngineSynchronizer implements ReversiWindowEngine, OpponentSelector.Listener {
    private final PingPong pingPong = new PingPong();

    private final @NotNull MultiEngine multiEngine;
    private final OpponentSelector opponentSelector;
    private final ReversiWindowEngine.Listener listener;
    private final ResponseHandler responseHandler;

    public EngineSynchronizer(OpponentSelector opponentSelector, ReversiWindowEngine.Listener listener) throws IOException {
        verifyEdt();
        this.listener = listener;
        responseHandler = new MyResponder();
        final StatelessEngine firstEngine = opponentSelector.getOpponent().getOrCreateEngine(responseHandler);
        this.multiEngine = new MultiEngine(firstEngine);
        this.opponentSelector = opponentSelector;
        opponentSelector.addListener(this);
    }

    @Override public String getName() {
        verifyEdt();
        return multiEngine.getName();
    }

    @Override public void learn(@NotNull SearchState state) {
        verifyEdt();
        multiEngine.learn(pingPong, state);
    }

    @Override public boolean isReady() {
        verifyEdt();
        return multiEngine.isReady();
    }

    @Override public void requestHints(@NotNull SearchState state, int nHints) {
        verifyEdt();
        multiEngine.requestHints(pingPong, state, nHints);
    }

    @Override public void requestMove(@NotNull SearchState state) {
        verifyEdt();
        multiEngine.requestMove(pingPong, state);
    }

    @Override public void opponentChanged() {
        verifyEdt();
        final OpponentSelection opponent = opponentSelector.getOpponent();
        try {
            final StatelessEngine newEngine = opponent.getOrCreateEngine(responseHandler);
            multiEngine.setEngine(pingPong, newEngine);
        } catch (IOException e) {
            // keep using the existing engine.
            listener.engineError("Unable to start up " + opponent + ": " + e);
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

    private static void verifyEdt() {
        Require.isTrue(SwingUtilities.isEventDispatchThread(), "is on EDT");
    }

    private class MyResponder implements ResponseHandler {
        @Override public void handle(@NotNull NBoardResponse nBoardResponse) {
            SwingUtilities.invokeLater(new ResponseRunner(nBoardResponse));
        }
    }

    /**
     * The action that gets run on the EDT
     */
    private class ResponseRunner implements Runnable {
        private final NBoardResponse response;

        public ResponseRunner(NBoardResponse response) {
            this.response = response;
        }

        @Override public void run() {
            verifyEdt();
            final Class<? extends NBoardResponse> c = response.getClass();

            if (c == StatusChangedResponse.class) {
                // Get the status from the current engine so it's guaranteed correct,
                // even if the message came from a different engine.
                listener.status(multiEngine.getStatus());

            } else if (c == MoveResponse.class) {
                final MoveResponse r = (MoveResponse) response;
                if (isCurrent(r.pong)) {
                    listener.engineMove(r.mli);
                }

            } else if (c == HintResponse.class) {
                final HintResponse r = (HintResponse) response;
                if (isCurrent(r.pong)) {
                    final CMove mv = new CMove(r.move);
                    listener.hint(r.book, r.pv, mv, r.eval, r.nGames, r.depth, r.freeformText);
                }

            } else if (c == ErrorResponse.class) {
                final ErrorResponse r = (ErrorResponse) response;
                listener.engineError(r.message);

            } else if (c == PongResponse.class) {
                final PongResponse r = (PongResponse) response;
                if (isCurrent(r.pong)) {
                    // We update ping every time we change the engine, so if the pong
                    // is current we know the current engine sent the message.
                    listener.engineReady();
                }

            } else {
                throw new IllegalArgumentException("Unknown message : " + response);
            }
        }
    }
}
