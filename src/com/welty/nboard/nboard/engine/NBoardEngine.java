package com.welty.nboard.nboard.engine;

import com.orbanova.common.misc.ListenerManager;

public abstract class NBoardEngine extends ListenerManager<NBoardEngine.Listener> {
    /**
     * Send a single line of text to the Engine
     *
     * @param command the text of the command
     */
    public abstract void sendCommand(String command);

    public interface Listener {
        /**
         * Handle a message received from the engine
         *
         * @param message one line of text from the message.
         */
        void onMessageReceived(String message);

        /**
         * The engine's process terminated.
         */
        void onEngineTerminated();
    }
}
