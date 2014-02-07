package com.welty.nboard.nboard.engine;

import com.welty.othello.api.NBoardEngine;
import com.welty.othello.api.ParsedEngine;
import junit.framework.TestCase;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ParsedEngineTest extends TestCase {
    public void testParseErrors() throws IOException {
        final NBoardEngine nbEngine = mock(NBoardEngine.class);

        final ParsedEngine parsedEngine = new ParsedEngine(nbEngine);
        final ParsedEngine.Listener listener = mock(ParsedEngine.Listener.class);
        parsedEngine.addListener(listener);

        parsedEngine.onMessageReceived("pong 1");
        final String bookMessage = "book      +1    c5";
        parsedEngine.onMessageReceived(bookMessage);
        verify(listener).parseError(eq(1), eq(bookMessage), anyString());
    }
}
