package com.welty.nboard.nboard.engine;

import com.welty.nboard.nboard.selector.GuiOpponentSelector;
import com.welty.othello.api.NBoardState;
import com.welty.othello.api.OpponentSelection;
import com.welty.othello.api.OpponentSelector;
import com.welty.othello.core.CMove;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsClock;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.gui.selector.EngineSelector;
import com.welty.othello.protocol.Depth;
import com.welty.othello.protocol.Value;
import junit.framework.TestCase;
import org.mockito.Mockito;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class EngineSynchronizerTest extends TestCase {
    private static final List<EngineSelector> selectors = GuiOpponentSelector.internalOpponentSelectors();

    public void testSingleEngine() throws Throwable {
        /**
         * This thread waits for notification.notify() to know when the test is complete
         */
        final Object notification = new Object();

        final NotifyingListener rwl = new NotifyingListener(notification);

        // Request a move from the Engine Synchronizer.
        testInEdt(new Runnable() {
            @Override public void run() {
                // set the game with the first engine
                OpponentSelector selector = Mockito.mock(OpponentSelector.class);
                stubSelector(selector, 0, 1);


                final EngineSynchronizer sync = new EngineSynchronizer(selector, rwl);

                // now request a move.
                sync.requestMove(createState());
            }
        });

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (notification) {
            notification.wait(1000);
            if (rwl.mli == null) {
                fail("Should have received an mli");
            }
        }
    }

    /**
     * Runs a runnable in the EDT and waits for it to complete.
     * <p/>
     * If the runnable throws an exception in the EDT, this rethrows it in the calling thread.
     *
     * @param runnable code to execute.
     * @throws Throwable anything that the runnable might have thrown.
     */
    private static void testInEdt(final Runnable runnable) throws Throwable {
        final AtomicReference<Throwable> failed = new AtomicReference<>();

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override public void run() {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    failed.set(e);
                }
            }
        });

        final Throwable t = failed.get();
        if (t != null) {
            throw t;
        }
    }

    private static void stubSelector(OpponentSelector selector, int engineSelectorIndex, int maxDepth) {
        Mockito.stub(selector.getOpponent()).toReturn(new OpponentSelection(selectors.get(engineSelectorIndex), maxDepth));
    }

    private static NBoardState createState() {
        final COsGame game = new COsGame();
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);
        return new NBoardState(game, 1, 0);
    }

    private static class NotifyingListener implements ReversiWindowEngine.Listener {
        private final Object notification;
        private volatile OsMoveListItem mli;

        public NotifyingListener(Object notification) {
            this.notification = notification;
        }

        @Override public void status(String status) {
        }

        @Override public void engineMove(OsMoveListItem mli) {
            synchronized (notification) {
                this.mli = mli;
                notification.notify();
            }
        }

        @Override public void engineReady() {
        }

        @Override public void hint(boolean fromBook, String pv, CMove move, Value eval, int nGames, Depth depth, String freeformText) {
        }

        @Override public void engineError(String message, String comment) {
        }

        @Override public void nameChanged(String name) {
        }

        @Override public void nodeStats(long nNodes, double tElapsed) {
        }
    }
}
