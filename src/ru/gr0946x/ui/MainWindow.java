package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.FractalState;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

import static java.lang.Math.*;

public class MainWindow extends JFrame {
    private final SelectablePanel mainPanel;
    private final Painter painter;
    private final Mandelbrot mandelbrot;
    private final Converter conv;

    public MainWindow() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);

        painter = new FractalPainter(mandelbrot, conv, (value) -> {
            if (value == 1.0) return Color.BLACK;
            var r = (float)abs(sin(5 * value));
            var g = (float)abs(cos(8 * value) * sin (3 * value));
            var b = (float)abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });

        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);

        mainPanel.addSelectListener((r) -> {
            if (r.width <= 2 || r.height <= 2) {
                return;
            }
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            double zoomFactor = 3.0 / (xMax - xMin);
            mandelbrot.setMaxIterations(Math.max(100, (int)(100 * (1 + Math.log10(zoomFactor)))));
            mainPanel.repaint();
        });

        new Menu(this, conv, mandelbrot);

        setContent();
    }
    private void setContent(){
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );

        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
    }
}

