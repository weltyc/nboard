package com.welty.nboard.nboard.engine;

import com.welty.nboard.nboard.selector.GuiOpponentSelector;
import com.welty.othello.api.OpponentSelection;
import com.welty.othello.api.OpponentSelector;
import com.welty.othello.api.PingEngine;
import com.welty.othello.gdk.COsGame;
import com.welty.othello.gdk.OsMoveListItem;
import com.welty.othello.gui.selector.EngineSelector;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

public class EngineSynchronizerTest extends TestCase {
    private static final List<EngineSelector> selectors = GuiOpponentSelector.internalOpponentSelectors();

    public void testSingleEngine() throws IOException {
        final PingEngine engine0 = selectors.get(0).createPingEngine(3);
        assertNotNull(engine0);

        // set the game with the first engine
        OpponentSelector selector = Mockito.mock(OpponentSelector.class);
        stubSelector(selector, 0, 1);
        final EngineSynchronizer sync = new EngineSynchronizer(selector);
        setupGame(sync);

        // now request a move.
        final EngineSynchronizer.Listener listener = Mockito.mock(EngineSynchronizer.Listener.class);
        sync.addListener(listener);
        sync.requestMove();

        // this test assumes that the engine returned its move in the same thread this method is
        // running in. This is not required by the EngineSynchronizer spec, it's just something
        // that currently happens for internal engines.
        //
        // If this behaviour changes, stub a Listener and use wait/notify to verify that we received the message.
        Mockito.verify(listener).engineMove(Mockito.any(OsMoveListItem.class));
    }

    private static void stubSelector(OpponentSelector selector, int engineSelectorIndex, int maxDepth) {
        Mockito.stub(selector.getOpponent()).toReturn(new OpponentSelection(selectors.get(engineSelectorIndex), maxDepth));
    }

    public void testSwitchingEngines() throws IOException {
        if (selectors.size() < 2) {
            throw new IllegalStateException("Need at least two opponents for this test");
        }
        final OpponentSelector stub = Mockito.mock(OpponentSelector.class);
        stubSelector(stub, 0, 1);


        // set the game with the first engine
        final EngineSynchronizer sync = new EngineSynchronizer(stub);
        setupGame(sync);

        // now switch engines and request a move. The second engine should have received a position.
        stubSelector(stub, 1, 2);
        sync.opponentChanged();
        final EngineSynchronizer.Listener listener = Mockito.mock(EngineSynchronizer.Listener.class);
        sync.addListener(listener);
        sync.requestMove();

        // this test assumes that the engine returned its move in the same thread this method is
        // running in. This is not required by the EngineSynchronizer spec, it's just something
        // that currently happens for internal engines.
        //
        // If this behaviour changes, stub a Listener and use wait/notify to verify that we received the message.
        Mockito.verify(listener).engineMove(Mockito.any(OsMoveListItem.class));
    }

    private static void setupGame(EngineSynchronizer sync) {
        final COsGame game = new COsGame();
        game.Initialize("8");
        sync.setGame(game);
    }
}
