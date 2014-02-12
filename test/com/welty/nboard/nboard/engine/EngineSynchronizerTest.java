package com.welty.nboard.nboard.engine;

import com.orbanova.common.misc.Utils;
import com.welty.nboard.nboard.selector.GuiOpponentSelector;
import com.welty.othello.api.OpponentSelection;
import com.welty.othello.api.OpponentSelector;
import com.welty.othello.api.SearchState;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.gui.selector.EngineSelector;
import junit.framework.TestCase;
import org.mockito.Mockito;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class EngineSynchronizerTest extends TestCase {
    private static final List<EngineSelector> selectors = GuiOpponentSelector.internalOpponentSelectors();

    public void testSingleEngine() throws Throwable {
        final ReversiWindowEngine.Listener rwl = Mockito.mock(ReversiWindowEngine.Listener.class);

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

        checkMoveReceived(rwl);
    }

    private static void checkMoveReceived(final ReversiWindowEngine.Listener rwl) throws Throwable {
        // Wait 50 ms, this should be plenty of time for the Engine to respond
        Utils.sleep(50);

        // make sure that the engine responded.
        testInEdt(new Runnable() {
            @Override public void run() {
                Mockito.verify(rwl).engineMove(Mockito.any(OsMoveListItem.class));

            }
        });
    }

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

    private static SearchState createState() {
        final COsGame game = new COsGame();
        game.Initialize("8");
        return new SearchState(game, 1, 0);
    }
}
