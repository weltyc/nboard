package com.welty.nboard;

import com.welty.othello.core.ProcessLogger;

import javax.swing.*;
import java.io.*;

/**
 * Class that controls communication with an engine.
 * <p/>
 * This class also includes functions to help ensure synchronization of the board state with
 * the ReversiWindow. To ensure synchronization of the board state:
 * - Call Ping() immediately before any command that changes the board state.
 * - The function IsReady() returns true if all pings have been accepted by the engine.
 * - If IsReady() returns true then messages from the engine relate to the current board state.
 * - If IsReady() returns false then messages from the engine relate to a previous board state and can be ignored.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 19, 2009
 * Time: 10:41:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReversiEngine {
    private final ReversiWindow reversiWindow;
    private int m_ping;
    private int m_pong;
    private String name = "ntest";
    private volatile boolean shutdown = false;
    private final ProcessLogger processLogger;

    public ReversiEngine(ReversiWindow reversiWindow) throws IOException {
        this.reversiWindow = reversiWindow;
        final String[] command = "./mEdax -nboard".split("\\s+");
        final File wd = new File("/Applications/edax/4.3.2/bin");
        final Process process = new ProcessBuilder(command).directory(wd).redirectErrorStream(true).start();
        processLogger = new ProcessLogger(process, true);


        new Thread("NBoard Feeder") {
            @Override public void run() {
                while (!shutdown) {
                    try {
                        final String line = processLogger.readLine();

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ReversiEngine.this.reversiWindow.OnMessageFromEngine(line);
                            }
                        });
                    } catch (IOException e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ReversiEngine.this.reversiWindow.OnMessageFromEngine("status Engine Terminated");
                            }
                        });
                    }
                }
            }
        }.start();
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
     * @return true if the engine is ready to accept commands.
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

}
