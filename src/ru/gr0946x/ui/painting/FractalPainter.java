package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;


import java.awt.image.BufferedImage;
import java.util.concurrent.*;

import java.awt.*;

public class FractalPainter implements Painter {
    private static final int Thread_Count = Runtime.getRuntime().availableProcessors();

    private final Converter converter;
    private Fractal fractal;
    private ColorFunction colorFunction;
    private final ExecutorService executor = Executors.newFixedThreadPool(Thread_Count);

    public FractalPainter(Fractal f, Converter converter, ColorFunction cf) {
        this.fractal = f;
        this.converter = converter;
        this.colorFunction = cf;
    }

    public void setFractal(Fractal fractal) {
        this.fractal = fractal;
    }

    public void setColorFunction(ColorFunction colorFunction) {
        this.colorFunction = colorFunction;
    }

    @Override
    public int getWidth() {
        return converter.getWidth();
    }

    @Override
    public int getHeight() {
        return converter.getHeight();
    }

    @Override
    public void setWidth(int width) {
        converter.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        converter.setHeight(height);
    }

    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int stripHeight = (int) Math.ceil((double) h / Thread_Count);
        CountDownLatch latch = new CountDownLatch(Thread_Count);

        for (int t = 0; t < Thread_Count; t++) {
            final int rowStart = t * stripHeight;
            final int rowEnd = Math.min(rowStart + stripHeight, h);

            executor.submit(() -> {
                try {
                    for (int j = rowStart; j < rowEnd; j++)
                        for (int i = 0; i < w; i++) {
                            double x = converter.xScr2Crt(i);
                            double y = converter.yScr2Crt(j);
                            float res = fractal.inSetProbability(x, y);
                            image.setRGB(i, j, colorFunction.getColor(res).getRGB());
                        }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        g.drawImage(image, 0, 0, null);
    }
}
