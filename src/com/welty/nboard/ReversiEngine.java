package com.welty.nboard;

import com.welty.othello.c.CWriter;
import com.welty.othello.lp.LinePrinter;

import javax.swing.*;
import java.io.*;
import java.nio.file.Paths;

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
    private PrintWriter out;
    private BufferedReader in;

    public ReversiEngine(ReversiWindow reversiWindow) throws IOException {
        this.reversiWindow = reversiWindow;
        StartupNtest();
    }

    private void StartupNtest() throws IOException {
        final String[] command = "./mEdax -nboard".split("\\s+");
        final File wd = new File("/Applications/edax/4.3.2/bin");
        final Process process = new ProcessBuilder(command).directory(wd).redirectErrorStream(true).start();
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
        in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        new Thread("NBoard Feeder") {
            @Override public void run() {
                while (!shutdown) {
                    try {
                        final String line = in.readLine();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                reversiWindow.OnMessageFromEngine(line);
                            }
                        });
                    } catch (IOException e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                reversiWindow.OnMessageFromEngine("status Engine Terminated");
                            }
                        });
                    }
                }
            }
        }.start();
    }


    /**
     * All messages to and from the engine are written to this file for debugging
     */
    static final CWriter g_debugLog = new CWriter("debugLog.txt", false);


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

        g_debugLog.println(sCommand);
        out.println(sCommand);
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
