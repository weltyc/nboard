package com.welty.nboard.nboard.engine;

import com.orbanova.common.misc.ListenerManager;
import com.welty.othello.api.SearchState;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

/**
 * An engine using the interface expected by the ReversiWindow
 */
public abstract class ReversiWindowEngine extends ListenerManager<ReversiWindowEngine.Listener> {

    public abstract String getName();

    /**
     * @return true if the engine is up-to-date (it has responded to all pings).
     */
    public abstract boolean isReady();

    public abstract void learn(@NotNull SearchState state);

    public abstract void requestHints(@NotNull SearchState state, int nHints);

    public abstract void requestMove(@NotNull SearchState state);

    public interface Listener {
        void status(String status);

        void engineMove(OsMoveListItem mli);

        /**
         * This message is sent when the engine is ready to accept new commands.
         * <p/>
         * For efficiency reasons, the gui should not send commands to an engine that is not ready,
         * so that commands don't just "stack up" on their way to the engine.
         */
        void engineReady();

        void hint(boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText);

        void parseError(String command, String errorMessage);

        void engineError(String message);
    }
}
