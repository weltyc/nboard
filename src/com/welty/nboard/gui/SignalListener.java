package com.welty.nboard.gui;

/**
 * Created by IntelliJ IDEA.
 * User: HP_Administrator
 * Date: Jun 26, 2009
 * Time: 7:30:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SignalListener<T> {
    void handleSignal(T data);
}
