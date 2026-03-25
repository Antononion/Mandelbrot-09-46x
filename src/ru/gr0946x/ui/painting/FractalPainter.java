package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Fractal;

import java.awt.*;

public class FractalPainter implements Painter{

    private final Fractal fractal;
    private final Converter conv;
    @Override
    public int getWidth() {
        return conv.getWidth();
    }

    @Override
    public int getHeight() {
        return conv.getHeight();
    }

    @Override
    public void setWidth(int width) {
        conv.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        conv.setHeight(height);
    }

    public FractalPainter(Fractal f, Converter conv){
        this.fractal = f;
        this.conv = conv;
    }

    @Override
    public void paint(Graphics g) {
        var w = getWidth();
        var h = getHeight();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                var x = conv.xScr2Crt(i);
                var y = conv.yScr2Crt(j);
                var res = fractal.inSetProbability(x, y);
                g.setColor((res == 1)? Color.BLACK : Color.WHITE);
                g.drawLine(i, j, i + 1, j);
            }
        }
    }
}
