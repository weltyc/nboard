package com.welty.nboard.nboard.engine;

import com.welty.othello.api.ApiEngine;
import com.welty.othello.api.NBoardEngine;
import com.welty.othello.api.OpponentSelector;
import com.welty.othello.c.CReader;
import com.welty.othello.core.CMove;
import com.welty.othello.engine.ExternalNBoardEngine;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;

/**
 * Class that controls communication with an engine.
 * <p/>
 * This class also includes functions to help ensure synchronization of the board state with
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
public class ParsedEngine extends ApiEngine implements OpponentSelector.Listener, NBoardEngine.Listener {
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
    public ParsedEngine(OpponentSelector opponentSelector) throws IOException {
        this(opponentSelector, new ExternalNBoardEngine());
    }

    /**
     * @param opponentSelector selector for opponent depth
     * @param engine           command-line engine
     */
    ParsedEngine(OpponentSelector opponentSelector, @NotNull final NBoardEngine engine) {
        this.opponentSelector = opponentSelector;
        this.engine = engine;
        SendCommand("nboard 1", false);
        final int maxDepth = opponentSelector.getOpponent().getMaxDepth();
        setMaxDepth(maxDepth);

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
            ping();

        engine.sendCommand(sCommand);
    }

    /**
     * @return true if the engine is ready to accept commands (it has responded to all pings)
     */
    public boolean isReady() {
        return m_pong >= m_ping;
    }

    /**
     * Send the engine a ping command, engine will be ready when it sends the corresponding pong command
     */
    @Override public void ping() {
        SendCommand("ping " + ++m_ping, false);
    }

    /**
     * Terminate the thread that sends messages to the window.
     * <p/>
     * This is called when the OS copy of the window is about to be destroyed. Sending
     * additional messages to the window could result in crashes.
     */
    @Override public void terminate() {
        SendCommand("quit", false);
    }

    public String getName() {
        return name;
    }

    private void SetName(String name) {
        this.name = name;
    }

    @Override public void opponentChanged() {
        final int maxDepth = opponentSelector.getOpponent().getMaxDepth();
        setMaxDepth(maxDepth);
    }


    /**
     * Set the NBoard protocol's current game.
     *
     * @param game game to set.
     */
    @Override public void setGame(COsGame game) {
        SendCommand("set game " + game, true);
    }

    /**
     * Tell the Engine to learn the current game.
     */
    @Override public void learn() {
        SendCommand("learn", false);
    }

    /**
     * Set the engine's contempt factor (scoring of proven draws).
     *
     * @param contempt contempt, in centidisks.
     */
    @Override public void setContempt(int contempt) {
        SendCommand("set contempt " + contempt, false);
    }


    @Override public void setMaxDepth(int maxDepth) {
        SendCommand("set depth " + maxDepth, true);
    }

    /**
     * Append a move to the NBoard protocol's current game.
     *
     * @param mli the move to append to the protocol's current game.
     */
    @Override public void sendMove(COsMoveListItem mli) {
        SendCommand("move " + mli, true);
    }

    /**
     * Request hints (evaluation of the top n moves) from the engine, for the current board
     *
     * @param nMoves number of moves to evaluate
     */
    @Override public void requestHints(int nMoves) {
        SendCommand("hint " + nMoves, false);
    }

    /**
     * Request a valid move from the engine, for the current board.
     * <p/>
     * Unlike {@link #requestHints(int)}, the engine does not have to return an evaluation;
     * if it has only one legal move it may choose to return that move immediately without searching.
     */
    @Override public void requestMove() {
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
            if (isReady()) {
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
        else if (isReady()) {
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
}
