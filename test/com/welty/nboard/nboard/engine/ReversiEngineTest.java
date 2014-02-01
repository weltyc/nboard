package com.welty.nboard.nboard.engine;

import com.welty.othello.gui.OpponentSelection;
import com.welty.othello.gui.OpponentSelector;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ReversiEngineTest extends TestCase {
    public void testParseErrors() throws IOException {
        final OpponentSelector opponentSelector = mockOpponentSelector();
        final NBoardEngine nbEngine = mock(NBoardEngine.class);

        final ReversiEngine reversiEngine = new ReversiEngine(opponentSelector, nbEngine);
        final ReversiEngine.Listener listener = mock(ReversiEngine.Listener.class);
        reversiEngine.addListener(listener);

        reversiEngine.onMessageReceived("pong 1");
        final String bookMessage = "book      +1    c5";
        reversiEngine.onMessageReceived(bookMessage);
        verify(listener).parseError(eq(bookMessage), anyString());
    }

    private static OpponentSelector mockOpponentSelector() {
        final OpponentSelector opponentSelector = mock(OpponentSelector.class);
        final OpponentSelection opponentSelection = mock(OpponentSelection.class);
        Mockito.when(opponentSelector.getOpponent()).thenReturn(opponentSelection);
        Mockito.when(opponentSelection.getMaxDepth()).thenReturn(4);
        return opponentSelector;
    }
}
