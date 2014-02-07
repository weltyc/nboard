package com.welty.nboard.nboard.engine;

import com.welty.othello.api.PingEngine;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

/**
 * A PingEngine that combines multiple PingEngines into one.
 * <p/>
 * No synchronization is performed; instead the caller checks the ping state.
 */
public class MultiEngine extends PingEngine implements PingEngine.Listener {
    private volatile @NotNull PingEngine engine;

    public MultiEngine(@NotNull PingEngine engine) {
        this.engine = engine;
        engine.addListener(this);
    }

    public void setEngine(int ping, PingEngine engine) {
        this.engine = engine;
        engine.addListener(this);
        ping(ping);
    }

    @Override public void terminate() {
        throw new IllegalStateException("Not implemented");
    }

    @Override public void setGame(int ping, COsGame game) {
        engine.setGame(ping, game);
    }

    @Override public void learn() {
        engine.learn();
    }

    @Override public void setContempt(int ping, int contempt) {
        engine.setContempt(ping, contempt);
    }

    @Override public void setMaxDepth(int ping, int maxDepth) {
        engine.setMaxDepth(ping, maxDepth);
    }

    @Override public void sendMove(int ping, OsMoveListItem mli) {
        engine.sendMove(ping, mli);
    }

    @Override public void requestHints(int nMoves) {
        engine.requestHints(nMoves);
    }

    @Override public void requestMove() {
        engine.requestMove();
    }

    @NotNull @Override public String getName() {
        return engine.getName();
    }

    @NotNull @Override public String getStatus() {
        return engine.getStatus();
    }

    @Override public void ping(int ping) {
        engine.ping(ping);
    }

    @Override public void statusChanged() {
        fireStatusChanged();
    }

    @Override public void engineMove(int pong, OsMoveListItem mli) {
        // if the engine has changed, pong will change. So don't worry about it.
        fireEngineMove(pong, mli);
    }

    @Override public void pong(int pong) {
        firePong(pong);
    }

    @Override public void hint(int pong, boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
        fireHint(pong, fromBook, pv, move, eval, nGames, depth, freeformText);
    }

    @Override public void parseError(int pong, String command, String errorMessage) {
        fireParseError(pong, command, errorMessage);
    }
}
