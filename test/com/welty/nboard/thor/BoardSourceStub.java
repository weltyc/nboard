package com.welty.nboard.thor;

import com.welty.nboard.gui.SignalListener;
import com.welty.nboard.nboard.BoardSource;
import com.welty.othello.c.CReader;
import com.welty.othello.gdk.*;
import org.jetbrains.annotations.NotNull;

class BoardSourceStub implements BoardSource {
    private COsGame game;
    private SignalListener<OsMoveListItem> listener;

    BoardSourceStub(String ggsGame) {
        game = new COsGame(new CReader(ggsGame));
    }

    BoardSourceStub() {
        game = new COsGame();
        game.Initialize("8", OsClock.DEFAULT, OsClock.DEFAULT);
    }

    @NotNull @Override public COsPosition DisplayedPosition() {
        return game.getPos();
    }

    @Override public int IMove() {
        return game.nMoves();
    }

    @Override public void addListener(SignalListener<OsMoveListItem> signalListener) {
        if (listener != null) {
            throw new UnsupportedOperationException("multiple listeners not implemented");
        }
        listener = signalListener;
    }

    void append(OsMoveListItem mli) {
        game.append(mli);
        if (listener != null) {
            listener.handleSignal(mli);
        }
    }

    @Override public int nMoves() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override public void SetIMove(int iMove) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override public COsGame getGame() {
        return game;
    }

    @Override public boolean isReviewing() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override public void update(OsMoveListItem mli, boolean fUserMove) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override public void Undo() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override public OsMove NextMove() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override public double secondsSinceLastMove() {
        throw new UnsupportedOperationException("not implemented");
    }
}
