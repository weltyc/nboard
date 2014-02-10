package com.welty.nboard.nboard.engine;

import com.welty.othello.api.SearchState;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMoveListItem;
import org.jetbrains.annotations.NotNull;

/**
 * An engine using the interface expected by the ReversiWindow
 */
public interface ReversiWindowEngine {

    String getName();

    /**
     * @return true if the engine is up-to-date (it has responded to all pings).
     */
    boolean isReady();

    void learn(@NotNull SearchState state);

    void requestHints(@NotNull SearchState state, int nHints);

    void requestMove(@NotNull SearchState state);

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

        /**
         * The engine has produced a line of text that cannot be parsed by the NBoard format.
         *
         * @param message the line of text
         * @param comment information on why the text is invalid
         */
        void engineError(String message, String comment);

        /**
         * Notify the ReversiWindow that the engine's name has changed
         *
         * @param name new engine name
         */
        void nameChanged(String name);

        /**
         * Notify the ReversiWindow that the engine's node stats are updated
         *
         * @param nNodes   number of nodes searched
         * @param tElapsed time taken to search
         */
        void nodeStats(double nNodes, double tElapsed);
    }
}
