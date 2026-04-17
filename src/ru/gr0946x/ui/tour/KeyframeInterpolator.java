package ru.gr0946x.ui.tour;

import ru.gr0946x.ui.fractals.FractalState;

public final class KeyframeInterpolator {

    private KeyframeInterpolator() {}

    /**
     * Interpolates between two FractalStates at parameter t ∈ [0, 1].
     * Zoom uses logarithmic interpolation; position uses linear.
     * A smoothstep easing is applied to t before calculations.
     * The y-extent is derived from the canvas aspect ratio.
     */
    public static FractalState interpolate(FractalState a, FractalState b,
                                           double t, int canvasWidth, int canvasHeight) {
        double ts = smoothstep(t);

        double cxA = (a.xMin() + a.xMax()) / 2.0;
        double cyA = (a.yMin() + a.yMax()) / 2.0;
        double cxB = (b.xMin() + b.xMax()) / 2.0;
        double cyB = (b.yMin() + b.yMax()) / 2.0;

        double cx = lerp(cxA, cxB, ts);
        double cy = lerp(cyA, cyB, ts);

        double wA = Math.max(a.xMax() - a.xMin(), 1e-15);
        double wB = Math.max(b.xMax() - b.xMin(), 1e-15);
        double w = Math.exp(lerp(Math.log(wA), Math.log(wB), ts));
        double h = w * (double) canvasHeight / canvasWidth;

        int iter = (int) Math.round(lerp(a.maxIterations(), b.maxIterations(), ts));

        return new FractalState(cx - w / 2, cx + w / 2, cy - h / 2, cy + h / 2, iter);
    }

    private static double smoothstep(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
