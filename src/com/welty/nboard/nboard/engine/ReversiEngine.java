package com.welty.nboard.nboard.engine;

import com.orbanova.common.misc.ListenerManager;
import com.welty.othello.c.CReader;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gui.OpponentSelector;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;

/**
 * Class that controls communication with an engine.
 * <p/>
 * This class also includes functions to help ensure synchronization of the board state with
 * the ReversiWindow. To ensure synchronization of the board state:
 * - Call Ping() immediately before any command that changes the board state.
 * - The function IsReady() returns true if all pings have been accepted by the engine.
 * - If IsReady() returns true then messages from the engine relate to the current board state.
 * - If IsReady() returns false then messages from the engine relate to a previous board state and can be ignored.
 */
public class ReversiEngine extends ListenerManager<ReversiEngine.Listener> implements OpponentSelector.Listener, NBoardEngine.Listener {
    private final OpponentSelector opponentSelector;
    private int m_ping;
    private int m_pong;
    private String name = "ntest";
    private final @NotNull NBoardEngine engine;

    /**
     * Construct with the default NBoardEngine
     *
     * @param opponentSelector selector for opponent depth
     * @throws IOException
     */
    public ReversiEngine(OpponentSelector opponentSelector) throws IOException {
        this(opponentSelector, new ExternalNBoardEngine());
    }

    /**
     * @param opponentSelector selector for opponent depth
     * @param engine           command-line engine
     */
    ReversiEngine(OpponentSelector opponentSelector, @NotNull final NBoardEngine engine) {
        this.opponentSelector = opponentSelector;
        this.engine = engine;
        SendCommand("nboard 1", false);
        SendCommand("set depth " + opponentSelector.getOpponent().getMaxDepth(), true);

        engine.addListener(this);
        opponentSelector.addListener(this);
    }


    /**
     * Send a command to the engine.
     * <p/>
     * \todo send this via a separate thread so it can't block the UI even if the pipe buffer overflows.
     *
     * @param sCommand   command to send to the engine
     * @param fPingFirst true if we should ping the engine before this command to ensure synchronization (e.g. if the command
     *                   affects the board state)
     */
    private void SendCommand(final String sCommand, boolean fPingFirst) {
        if (fPingFirst)
            Ping();

        engine.sendCommand(sCommand);
    }

    /**
     * @return true if the engine is ready to accept commands (it has responded to all pings)
     */
    public boolean IsReady() {
        return m_pong >= m_ping;
    }

    /**
     * Send the engine a ping command, engine will be ready when it sends the corresponding pong command
     */
    void Ping() {
        SendCommand("ping " + ++m_ping, false);
    }

    /**
     * Record the fact that the engine has sent a pong command.
     */
    void SetPong(int n) {
        m_pong = n;
    }

    /**
     * Terminate the thread that sends messages to the window.
     * <p/>
     * This is called when the OS copy of the window is about to be destroyed. Sending
     * additional messages to the window could result in crashes.
     */
    void Terminate() {
        SendCommand("quit", false);
    }

    public String GetName() {
        return name;
    }

    public void SetName(String name) {
        this.name = name;
    }

    @Override public void opponentChanged() {
        final int newLevel = opponentSelector.getOpponent().getMaxDepth();
        SendCommand("set depth " + newLevel, true);
    }

    private void fireHint(boolean book, String pv, CMove move, String eval, int nGames, String depth, String freeformText) {
        for (Listener l : getListeners()) {
            l.hint(book, pv, move, eval, nGames, depth, freeformText);
        }
    }

    /**
     * Notify listeners of a status update
     *
     * @param status status text
     */
    private void fireStatus(String status) {
        for (Listener l : getListeners()) {
            l.status(status);
        }
    }

    /**
     * Notify listeners that the engine moved
     *
     * @param mli move
     */
    private void fireEngineMove(COsMoveListItem mli) {
        for (Listener l : getListeners()) {
            l.engineMove(mli);
        }
    }

    /**
     * Notify listeners that the engine is ready to accept commands
     */
    private void fireEngineReady() {
        for (Listener l : getListeners()) {
            l.engineReady();
        }
    }


    private void fireParseError(String command, String errorMessage) {
        for (Listener l : getListeners()) {
            l.parseError(command, errorMessage);
        }
    }


    /**
     * Set the NBoard protocol's current game.
     *
     * @param game game to set.
     */
    public void setGame(COsGame game) {
        SendCommand("set game " + game, true);
    }

    /**
     * Tell the Engine to learn the current game.
     */
    public void learn() {
        SendCommand("learn", false);
    }

    /**
     * Set the engine's contempt factor (scoring of proven draws).
     *
     * @param contempt contempt, in centidisks.
     */
    public void setContempt(int contempt) {
        SendCommand("set contempt " + contempt, false);
    }

    /**
     * Append a move to the NBoard protocol's current game.
     *
     * @param mli the move to append to the protocol's current game.
     */
    public void sendMove(COsMoveListItem mli) {
        SendCommand("move " + mli, true);
    }

    /**
     * Request hints (evaluation of the top n moves) from the engine, for the current board
     *
     * @param nMoves number of moves to evaluate
     */
    public void requestHints(int nMoves) {
        SendCommand("hint " + nMoves, false);
    }

    /**
     * Request a valid move from the engine, for the current board.
     * <p/>
     * Unlike {@link #requestHints(int)}, the engine does not have to return an evaluation;
     * if it has only one legal move it may choose to return that move immediately without searching.
     */
    public void requestMove() {
        SendCommand("go", false);
    }

    /**
     * Parse a command received from the engine and notify listeners.
     * <p/>
     * Pong is handled internally by updating m_pong.
     */
    @Override public void onMessageReceived(String message) {
        final CReader is = new CReader(message);
        String sCommand = is.readString();
        is.ignoreWhitespace();

        if (sCommand.equals("pong")) {
            int n;
            try {
                n = is.readInt();
            } catch (EOFException e) {
                throw new IllegalStateException("Engine response is garbage : " + message);
            }
            m_pong = n;
            if (IsReady()) {
                fireStatus("");
                fireEngineReady();
            }
        } else if (sCommand.equals("status")) {
            // the engine is busy and is telling the user why
            fireStatus(is.readLine());
        } else if (sCommand.equals("set")) {
            String variable = is.readString();
            if (variable.equals("myname")) {
                String sName = is.readString();
                SetName(sName);
            }
        }
        // These commands are only used if the computer is up-to-date
        else if (IsReady()) {
            switch (sCommand) {
                case "===":
                    fireStatus("");
                    // now update the move list

                    // Edax produces the mli with spaces between components rather than slashes.
                    // Translate to normal form if there are spaces.
                    final String mliText = is.readLine().trim().replaceAll("\\s+", "/");
                    final COsMoveListItem mli = new COsMoveListItem(mliText);

                    fireEngineMove(mli);
                    break;
                // computer giving hints
                // search [pv] [eval] 0 [depth] [freeform text]
                // book [pv] [eval] [# games] [depth] [freeform text]
                case "book":
                case "search":
                    try {
                        final boolean isBook = sCommand.equals("book");

                        final String pv = is.readString();
                        final CMove move;
                        try {
                            move = new CMove(pv.substring(0, 2));
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Can't create move from first two characters of pv (" + pv + ")");
                        }
                        final String eval = is.readString();
                        final int nGames = is.readInt();
                        final String depth = is.readString();
                        final String freeformText = is.readLine();
                        fireHint(isBook, pv, move, eval, nGames, depth, freeformText);
                    } catch (EOFException | IllegalArgumentException e) {
                        fireParseError(message, e.toString());
                    }
                    break;
                case "learn":
                    fireStatus("");
                    break;
            }
        }
    }

    @Override public void onEngineTerminated() {
        fireStatus("The engine (" + name + ") has terminated.");
    }

    /**
     * Listens to responses from the Engine
     */
    public interface Listener {
        /**
         * The Engine updated its status
         *
         * @param status status text
         */
        public void status(String status);

        /**
         * The engine moved.
         * <p/>
         * The engine only sends this message if it relates to the current board position (ping = pong).
         * Otherwise it discards the message.
         *
         * @param mli engine move
         */
        void engineMove(COsMoveListItem mli);

        /**
         * The engine is ready to accept commands (ping=pong).
         */
        void engineReady();

        /**
         * The engine's evaluation of a move.
         * <p/>
         * The engine only sends this message if it relates to the current board position (ping = pong).
         * Otherwise it discards the message.
         *
         * @param fromBook     if true, hint comes from the book
         * @param pv           principal variation - the first two characters are the evaluated move.
         * @param move         the evaluated move
         * @param eval         evaluation of the move.
         * @param nGames       # of games (for book moves only)
         * @param depth        search depth reached when evaluating this move
         * @param freeformText optional extra text relating to the move
         */
        void hint(boolean fromBook, String pv, CMove move, String eval, int nGames, String depth, String freeformText);

        /**
         * The engine sent a message which appears to be an nboard protocol message but can't be parsed correctly.
         *
         * @param command      command from engine
         * @param errorMessage error message from parser
         */
        void parseError(String command, String errorMessage);
    }
}
