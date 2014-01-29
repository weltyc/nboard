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
    private final ArrayList<SignalListener<T>> m_targets = new ArrayList<SignalListener<T>>();

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

