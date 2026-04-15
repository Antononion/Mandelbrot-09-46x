package ru.gr0946x.ui.io;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.QuadraticFractal;

import java.awt.*;

public interface FractalSerializer {
    void save(Component parent, Converter conv, QuadraticFractal fractal);
    void open(Component parent, Converter conv, QuadraticFractal fractal, Runnable onSuccess);
}
