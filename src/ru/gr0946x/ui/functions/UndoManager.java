package ru.gr0946x.ui.functions;

import ru.gr0946x.ui.fractals.FractalState;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class UndoManager {
    private static final int MAX_STEPS = 100;
    private final Deque<FractalState> history = new ArrayDeque<>(MAX_STEPS);
    private final Consumer<FractalState> restoreCallback;

    public UndoManager(Consumer<FractalState> restoreCallback) {
        this.restoreCallback = restoreCallback;
    }

    public void push(FractalState state) {
        if (history.size() >= MAX_STEPS) {
            history.pollFirst();
        }
        history.offerLast(state);
    }

    public boolean undo() {
        FractalState previous = history.pollLast();
        if (previous != null) {
            restoreCallback.accept(previous);
            return true;
        }
        return false;
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }
}