package com.welty.nboard.nboard.engine;

import com.welty.othello.api.NBoardEngine;
import com.welty.othello.api.OpponentSelection;
import com.welty.othello.api.OpponentSelector;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ParsedEngineTest extends TestCase {
    public void testParseErrors() throws IOException {
        final OpponentSelector opponentSelector = mockOpponentSelector();
        final NBoardEngine nbEngine = mock(NBoardEngine.class);

        final ParsedEngine parsedEngine = new ParsedEngine(opponentSelector, nbEngine);
        final ParsedEngine.Listener listener = mock(ParsedEngine.Listener.class);
        parsedEngine.addListener(listener);

        parsedEngine.onMessageReceived("pong 1");
        final String bookMessage = "book      +1    c5";
        parsedEngine.onMessageReceived(bookMessage);
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
