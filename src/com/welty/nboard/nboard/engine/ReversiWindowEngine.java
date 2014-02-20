package com.welty.nboard.nboard.engine;

import com.welty.othello.api.NBoardState;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.protocol.Depth;
import com.welty.othello.protocol.Value;
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

    void learn(@NotNull NBoardState state);

    void requestHints(@NotNull NBoardState state, int nHints);

    void requestMove(@NotNull NBoardState state);

    public interface Listener {
        /**
         * The engine has updated its status
         *
         * @param status text of status message
         */
        void status(String status);

        /**
         * The engine has chosen a move
         *
         * @param mli engine's move
         */
        void engineMove(OsMoveListItem mli);

        /**
         * This message is sent when the engine is ready to accept new commands.
         * <p/>
         * For efficiency reasons, the gui should not send commands to an engine that is not ready,
         * so that commands don't just "stack up" on their way to the engine.
         */
        void engineReady();

        /**
         * The engine is giving a move hint (in analysis mode)
         *
         * @param fromBook     if true, the hint comes from book; otherwise it comes from a search
         * @param pv           principal variation analyzed
         * @param move         move that is hinted
         * @param eval         evaluation
         * @param nGames       number of games in book, or 0 for searches
         * @param depth        depth searched to
         * @param freeformText engine comment
         */
        void hint(boolean fromBook, String pv, CMove move, Value eval, int nGames, Depth depth, String freeformText);

        /**
         * The engine has produced a line of text that cannot be parsed by the NBoard format.
         *
         * @param message the line of text
         * @param comment information on why the text is invalid
         */
        void engineError(String message, String comment);

        /**
         * The engine's name has changed
         *
         * @param name new engine name
         */
        void nameChanged(String name);

        /**
         * The engine's node stats are updated
         *
         * @param nNodes   number of nodes searched
         * @param tElapsed time taken to search
         */
        void nodeStats(long nNodes, double tElapsed);
    }
}
