package com.welty.nboard.nboard.engine;

import com.welty.othello.api.PingPong;
import com.welty.othello.api.SearchState;
import com.welty.othello.api.StatelessEngine;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

/**
 * A StatelessEngine that combines multiple PingEngines into one.
 * <p/>
 * No synchronization is performed; instead the caller checks the ping state.
 */
public class MultiEngine extends StatelessEngine implements StatelessEngine.Listener {
    private volatile @NotNull StatelessEngine engine;

    public MultiEngine(@NotNull StatelessEngine engine) {
        this.engine = engine;
        engine.addListener(this);
    }

    public void setEngine(PingPong pingPong, StatelessEngine engine) {
        if (engine != this.engine) {
            // need to check engine has changed, otherwise can get a race condition when removing/adding listeners
            this.engine.removeListener(this);
            this.engine = engine;
            engine.addListener(this);
            pingPong.next(); // invalidate all previous engine responses
        }
    }

    @Override public void terminate() {
        throw new IllegalStateException("Not implemented");
    }

    @Override public void learn(PingPong pingPong, SearchState state) {
        engine.learn(pingPong, state);
    }

    @Override public void requestHints(PingPong pingPong, SearchState state, int nMoves) {
        engine.requestHints(pingPong, state, nMoves);
    }

    @Override public void requestMove(PingPong pingPong, SearchState state) {
        engine.requestMove(pingPong, state);
    }

    @NotNull @Override public String getName() {
        return engine.getName();
    }

    @NotNull @Override public String getStatus() {
        return engine.getStatus();
    }

    @Override public boolean isReady() {
        return engine.isReady();
    }

    @Override public void statusChanged() {
        fireStatusChanged();
    }

    @Override public void engineMove(int pong, OsMoveListItem mli) {
        fireEngineMove(pong, mli);
    }

    @Override public void engineReady(int pong) {
        fireEngineReady(pong);
    }

    @Override public void hint(int pong, boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
        fireHint(pong, fromBook, pv, move, eval, nGames, depth, freeformText);
    }

    @Override public void parseError(int pong, String command, String errorMessage) {
        fireParseError(pong, command, errorMessage);
    }
}
