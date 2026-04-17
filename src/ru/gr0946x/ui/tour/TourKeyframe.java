package ru.gr0946x.ui.tour;

import ru.gr0946x.ui.fractals.FractalState;

public final class TourKeyframe {
    private final FractalState state;
    private double durationSeconds;

    public TourKeyframe(FractalState state, double durationSeconds) {
        this.state = state;
        this.durationSeconds = durationSeconds;
    }

    public FractalState getState() { return state; }
    public double getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(double s) { this.durationSeconds = s; }
}
