package ru.gr0946x.ui.io;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.QuadraticFractal;

import java.awt.*;

public class FractalFileManager {

    private final Component parent;
    private final Converter conv;
    private final QuadraticFractal fractal;

    public FractalFileManager(Component parent, Converter conv, QuadraticFractal fractal) {
        this.parent = parent;
        this.conv = conv;
        this.fractal = fractal;
    }

    public void save(FractalSerializer serializer) {
        serializer.save(parent, conv, fractal);
    }

    public void open(FractalSerializer serializer, Runnable onSuccess) {
        serializer.open(parent, conv, fractal, onSuccess);
    }
}
