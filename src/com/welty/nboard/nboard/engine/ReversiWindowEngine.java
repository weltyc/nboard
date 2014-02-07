package com.welty.nboard.nboard.engine;

import com.orbanova.common.misc.ListenerManager;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;

/**
 * An engine using the interface expected by the ReversiWindow
 */
public abstract class ReversiWindowEngine extends ListenerManager<ReversiWindowEngine.Listener> {
    public abstract void sendMove(OsMoveListItem mli);

    public abstract void setGame(COsGame displayedGame);

    public abstract String getName();

    public abstract void setContempt(int contempt);

    public abstract void learn();

    public abstract boolean isReady();

    public abstract void requestHints(int nHints);

    public abstract void requestMove();

    public interface Listener {
        public void status(String status);

        public void engineMove(OsMoveListItem mli);

        public void engineReady();

        public void hint(boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText);

        public void parseError(String command, String errorMessage);
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
