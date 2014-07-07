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

package com.welty.nboard.gui;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Class that describes an event that can be signalled
 * <p/>
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 17, 2009
 * Time: 3:07:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class SignalEvent<T> {
    private final ArrayList<SignalListener<T>> m_targets = new ArrayList<>();

    public void Add(@NotNull SignalListener<T> listener) {
        m_targets.add(listener);
    }

    public void Raise() {
        Raise(null);
    }

    public void Raise(T data) {
        for (SignalListener<T> listener : m_targets) {
            listener.handleSignal(data);
        }
    }
}

