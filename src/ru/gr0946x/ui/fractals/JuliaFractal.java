package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

public class JuliaFractal implements Fractal {
    private int maxIterations = 100;
    private final double R2 = 4;
    private Complex constant; // Фиксированная константа C для множества Жюлия

    public JuliaFractal(Complex constant) {
        this.constant = constant;
    }

    public void setConstant(Complex constant) {
        this.constant = constant;
    }

    /* Не использовалось
     public Complex getConstant() {
        return constant;
    }
    public void setMaxIterations(int n) {
        maxIterations = n;
    }
    public int getMaxIterations() {
    return maxIterations;
    }
    */

    @Override
    public float inSetProbability(double x, double y) {
        // Для Жюлия: Z_{n+1} = Z_n^2 + C
        // Z начинается с координат пикселя (x, y)
        var z = new Complex(x, y);
        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations) {
            z.timesAssign(z);
            z.plusAssign(constant);
        }
        return (float) i / maxIterations;
    }
}