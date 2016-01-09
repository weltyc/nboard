/*
 * Copyright (c) 2014 Chris Welty.
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the license, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.welty.nboard.nboard.engine;

import com.welty.novello.external.api.NBoardState;
import com.welty.novello.external.api.PingPong;
import com.welty.novello.external.api.StatelessEngine;
import org.jetbrains.annotations.NotNull;

/**
 * A StatelessEngine that combines multiple PingEngines into one.
 * <p/>
 * No synchronization is performed by this object; instead the caller checks the ping state.
 */
public class MultiEngine implements StatelessEngine {
    private @NotNull StatelessEngine engine;

    /**
     * @param engine initial engine
     */
    public MultiEngine(@NotNull StatelessEngine engine) {
        this.engine = engine;
    }

    /**
     */
    public synchronized void setEngine(PingPong pingPong, StatelessEngine engine) {
        if (engine != this.engine) {
            this.engine = engine;
            pingPong.next(); // invalidate all previous engine responses
        }
    }

    @Override public synchronized void terminate() {
        throw new IllegalStateException("Not implemented");
    }

    @Override public synchronized void learn(PingPong pingPong, NBoardState state) {
        engine.learn(pingPong, state);
    }

    @Override public synchronized void analyze(PingPong pingPong, NBoardState state) {
        engine.analyze(pingPong, state);
    }

    @Override public synchronized void requestHints(PingPong pingPong, NBoardState state, int nMoves) {
        engine.requestHints(pingPong, state, nMoves);
    }

    @Override public synchronized void requestMove(PingPong pingPong, NBoardState state) {
        engine.requestMove(pingPong, state);
    }

    @NotNull @Override public synchronized String getName() {
        return engine.getName();
    }

    @NotNull @Override public synchronized String getStatus() {
        return engine.getStatus();
    }

    @Override public synchronized boolean isReady() {
        return engine.isReady();
    }
}
