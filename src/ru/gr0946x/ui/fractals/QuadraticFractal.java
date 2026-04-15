package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

public class QuadraticFractal implements Fractal {
    private Complex constant;     // Для Mandelbrot = 0, для Julia = фиксированное C
    private int maxIterations = 100;
    private final boolean isJulia;

    public QuadraticFractal() {
        this(new Complex(0, 0), false);
    }

    public QuadraticFractal(Complex constant) {
        this(constant, true);
    }

    private QuadraticFractal(Complex constant, boolean isJulia) {
        this.constant = constant;
        this.isJulia = isJulia;
    }

    public void setMaxIterations(int n) {
        this.maxIterations = n;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setConstant(Complex constant) {
        // Можно разрешить менять только для Julia, или сделать копию
        this.constant = constant; // если нужно менять
    }

    public Complex getConstant() {
        return constant;
    }

    public boolean isJulia() {
        return isJulia;
    }

    @Override
    public float inSetProbability(double x, double y) {
        final double R2 = 4.0;
        Complex z = isJulia ? new Complex(x, y) : new Complex(0, 0);
        Complex c = isJulia ? constant : new Complex(x, y);

        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations) {
            z.timesAssign(z);
            z.plusAssign(c);
        }

        return (float) i / maxIterations;
    }
}
