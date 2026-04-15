package ru.gr0946x.ui;

import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;
import ru.smak.math.Complex;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class FunctionAndColorSchemesLists {
    private final List<Fractal> fractalFunctions = new ArrayList<>();
    private final List<ColorFunction> colorSchemes = new ArrayList<>();

    public FunctionAndColorSchemesLists() {
        initFractals();
        initColorSchemes();
    }

    private void initFractals() {
        // z² + c — классический Мандельброт
        fractalFunctions.add((x, y) -> {
            var c = new Complex(x, y);
            var z = new Complex();
            int i = 0;
            int maxIt = 100;
            while (z.getAbsoluteValue2() < 4 && ++i < maxIt) {
                z.timesAssign(z);
                z.plusAssign(c);
            }
            return (float) i / maxIt;
        });

        // z³ + c
        fractalFunctions.add((x, y) -> {
            var c = new Complex(x, y);
            var z = new Complex();
            int i = 0;
            int maxIt = 100;
            while (z.getAbsoluteValue2() < 4 && ++i < maxIt) {
                var zOld = new Complex(0, 0);
                zOld.plusAssign(z);
                z.timesAssign(z);
                z.timesAssign(zOld);
                z.plusAssign(c);
            }
            return (float) i / maxIt;
        });

        // z⁴ + c
        fractalFunctions.add((x, y) -> {
            var c = new Complex(x, y);
            var z = new Complex();
            int i = 0;
            int maxIt = 100;
            while (z.getAbsoluteValue2() < 4 && ++i < maxIt) {
                z.timesAssign(z);
                z.timesAssign(z);
                z.plusAssign(c);
            }
            return (float) i / maxIt;
        });
    }

    private void initColorSchemes() {
        // Схема 1
        colorSchemes.add(value -> {
            if (value == 1.0) return Color.BLACK;
            var r = (float)abs(sin(5 * value));
            var g = (float)abs(cos(8 * value) * sin (3 * value));
            var b = (float)abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });

        // Схема 2
        colorSchemes.add(value -> {
            if (value == 1.0) return Color.BLACK;
            var intensity = 1 - value;
            return new Color(intensity, intensity, intensity);
        });

        // Схема 3
        colorSchemes.add(value -> {
            if (value == 1.0) return Color.BLACK;
            var intensity = 1 - value;
            return new Color(0f, intensity * 0.4f, intensity);
        });
    }

    // Геттеры
    public List<Fractal> getFractalFunctions() {
        return fractalFunctions;
    }

    public List<ColorFunction> getColorSchemes() {
        return colorSchemes;
    }
}
