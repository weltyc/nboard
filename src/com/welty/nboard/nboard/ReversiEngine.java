package com.welty.nboard.nboard;

import com.orbanova.common.misc.ListenerManager;
import com.welty.othello.c.CReader;
import com.welty.othello.core.ProcessLogger;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.COsMoveListItem;
import com.welty.othello.gui.OpponentSelector;

import javax.swing.*;
import java.io.EOFException;
import java.io.File;
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
class ReversiEngine extends ListenerManager<ReversiEngine.Listener> implements OpponentSelector.Listener {
    private final OpponentSelector opponentSelector;
    private int m_ping;
    private int m_pong;
    private String name = "ntest";
    private volatile boolean shutdown = false;
    private final ProcessLogger processLogger;

    /**
     * Start up an engine in an external process and initialize it
     *
     * @param opponentSelector selector for opponent depth
     * @throws IOException
     */
    public ReversiEngine(OpponentSelector opponentSelector) throws IOException {
        this.opponentSelector = opponentSelector;
        final String[] command = "./mEdax -nboard".split("\\s+");
        final File wd = new File("/Applications/edax/4.3.2/bin");
        final Process process = new ProcessBuilder(command).directory(wd).redirectErrorStream(true).start();
        processLogger = new ProcessLogger(process, true);
        SendCommand("nboard 1", false);
        SendCommand("set depth " + opponentSelector.getOpponent().getMaxDepth(), true);

        new Thread("NBoard Feeder") {
            @Override public void run() {
                while (!shutdown) {
                    try {
                        final String line = processLogger.readLine();

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                parseAndSend(line);
                            }
                        });
                    } catch (IOException e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                fireStatus("The engine (" + name + ") has terminated.");
                            }
                        });
                    }
                }
            }
        }.start();

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
    void SendCommand(final String sCommand, boolean fPingFirst) {
        if (fPingFirst)
            Ping();

        processLogger.println(sCommand);
    }

    /**
     * @return true if the engine is ready to accept commands (it has responded to all pings)
     */
    boolean IsReady() {
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

    /**
     * Parse a command received from the engine.
     * <p/>
     * Pong is handled internally by updating m_pong.
     */
    private void parseAndSend(String string) {
        final CReader is = new CReader(string);
        String sCommand = is.readString();
        is.ignoreWhitespace();

        if (sCommand.equals("pong")) {
            int n;
            try {
                n = is.readInt();
            } catch (EOFException e) {
                throw new IllegalStateException("Engine response is garbage : " + string);
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
                case "book":
                case "search":
                    fireHint(sCommand.equals("book"), is.readString(), is);
                    break;
                case "learn":
                    fireStatus("");
                    break;
            }
        }
    }

    private void fireHint(boolean fromBook, String pv, CReader rest) {
        for (Listener l : getListeners()) {
            l.hint(fromBook, pv, rest);
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
         * @param fromBook if true, this value is from the engine's book; otherwise it is from a search.
         * @param pv       principal variation - the first two characters are the evaluated move.
         * @param rest     rest of the move hint - this should be parsed further in future versions.
         *                 See {@link Hints#Add(String, CReader, boolean, boolean)} for the format.
         */
        void hint(boolean fromBook, String pv, CReader rest);
    }
}
