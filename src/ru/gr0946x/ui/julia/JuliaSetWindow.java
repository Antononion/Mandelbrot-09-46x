package ru.gr0946x.ui.julia;

import ru.smak.math.Complex;
import ru.gr0946x.Converter;
import ru.gr0946x.ui.SelectablePanel;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.FractalState;
import ru.gr0946x.ui.fractals.QuadraticFractal;
import ru.gr0946x.ui.functions.UndoManager;
import ru.gr0946x.ui.io.*;
import ru.gr0946x.ui.tour.TourWindow;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;
import ru.gr0946x.ui.FunctionAndColorSchemesLists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JuliaSetWindow extends JFrame {
    private final Converter converter;
    private final QuadraticFractal fractal;
    private final FracSerializer fracSerializer;
    private final FractalFileManager fileManager;
    private final ImageSerializer imageSerializer;
    private boolean adaptiveIterationsEnabled = true;
    private final UndoManager undoManager;
    private final FunctionAndColorSchemesLists lists;
    private final Painter painter;
    private final SelectablePanel fractalPanel;
    private TourWindow tourWindow;

    public JuliaSetWindow(Complex point) {
        setTitle("Множество Жюлиа: " + formatComplex(point));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 600);
        setMinimumSize(new Dimension(400, 400));

        converter = new Converter(-2.0, 2.0, -2.0, 2.0);
        fractal = new QuadraticFractal(point);
        fracSerializer = new FracSerializer();
        fileManager = new FractalFileManager(this, converter, fractal);
        imageSerializer = new ImageSerializer();
        undoManager = new UndoManager(this::restoreState);
        lists = new FunctionAndColorSchemesLists();
        ColorFunction firstColorScheme = lists.getColorSchemes().getFirst();
        painter = new FractalPainter(fractal, converter, firstColorScheme);
        fractalPanel = new SelectablePanel(painter, converter, imageSerializer);
        fractalPanel.setBackground(Color.BLACK);

        fractalPanel.addSelectListener(r -> {
            if (imageSerializer.isImageMode()) {
                JOptionPane.showMessageDialog(this,
                        "Масштабирование недоступно при просмотре изображения.\nОткройте фрактал (.frac) для масштабирования.",
                        "Предупреждение",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (r.width <= 2 || r.height <= 2)
                return;

            undoManager.push(new FractalState(
                    converter.getXMin(), converter.getXMax(),
                    converter.getYMin(), converter.getYMax(),
                    fractal.getMaxIterations()
            ));

            var xMin = converter.xScr2Crt(r.x);
            var xMax = converter.xScr2Crt(r.x + r.width);
            var yMin = converter.yScr2Crt(r.y + r.height);
            var yMax = converter.yScr2Crt(r.y);
            converter.setXShape(xMin, xMax);
            converter.setYShape(yMin, yMax);

            if (adaptiveIterationsEnabled) {
                double zoomFactor = 3.0 / (xMax - xMin);
                fractal.setMaxIterations(Math.max(100, (int)(100 * (1 + Math.log10(zoomFactor)))));
            }

            imageSerializer.clearImage();
            fractalPanel.repaint();
        });

        fractalPanel.setStateChangeListener(() -> {
            undoManager.push(new FractalState(
                    converter.getXMin(), converter.getXMax(),
                    converter.getYMin(), converter.getYMax(),
                    fractal.getMaxIterations()
            ));
        });

        // Клик для смены константы
        fractalPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    double x = converter.xScr2Crt(e.getX());
                    double y = converter.yScr2Crt(e.getY());
                    Complex newPoint = new Complex(x, y);
                    fractal.setConstant(newPoint);
                    setTitle("Множество Жюлиа: " + formatComplex(newPoint));
                    fractalPanel.repaint();
                }
            }
        });

        // Зум колёсиком
        fractalPanel.addMouseWheelListener(e -> {
            double zoomFactor = e.getWheelRotation() < 0 ? 0.9 : 1.1;
            double mouseX = converter.xScr2Crt(e.getX());
            double mouseY = converter.yScr2Crt(e.getY());

            double newWidth = (converter.getXMax() - converter.getXMin()) * zoomFactor;
            double newHeight = (converter.getYMax() - converter.getYMin()) * zoomFactor;

            double newXMin = mouseX - newWidth * (mouseX - converter.getXMin()) / (converter.getXMax() - converter.getXMin());
            double newXMax = newXMin + newWidth;
            double newYMin = mouseY - newHeight * (mouseY - converter.getYMin()) / (converter.getYMax() - converter.getYMin());
            double newYMax = newYMin + newHeight;

            converter.setXShape(newXMin, newXMax);
            converter.setYShape(newYMin, newYMax);
            fractalPanel.repaint();
        });

        new MenuManager(this, new JuliaMenuProvider(this));

        getRootPane().registerKeyboardAction(
                _ -> undoManager.undo(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Кнопка сброса
        JButton resetButton = new JButton("Сбросить вид");
        resetButton.addActionListener(_ -> {
            converter.setXShape(-2.0, 2.0);
            converter.setYShape(-2.0, 2.0);
            fractalPanel.repaint();
        });

        setLayout(new BorderLayout());
        add(fractalPanel, BorderLayout.CENTER);
        add(resetButton, BorderLayout.NORTH);
    }

    private String formatComplex(Complex c) {
        return String.format("%.4f %+.4fi", c.getReal(), c.getImaginary());
    }

    public void setCurrentColorFunction(ColorFunction colorFunction) {
        if (painter instanceof FractalPainter fp)
            fp.setColorFunction(colorFunction);

        fractalPanel.repaint();
    }

    private void restoreState(FractalState state) {
        converter.setXShape(state.xMin(), state.xMax());
        converter.setYShape(state.yMin(), state.yMax());
        fractal.setMaxIterations(state.maxIterations());
        fractalPanel.repaint();
    }

    public void triggerUndo() {
        if (undoManager.undo())
            fractalPanel.repaint();
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public void saveFractal() {
        fracSerializer.saveWithFormatChoice(this, converter, fractal, fractalPanel, imageSerializer);
    }

    public void openFile() {
        fracSerializer.openWithFormatChoice(this, converter, fractal, fractalPanel, imageSerializer);
    }

    public void setAdaptiveIterationsEnabled(boolean enabled) {
        this.adaptiveIterationsEnabled = enabled;
    }

    public FractalState captureState() {
        return new FractalState(
                converter.getXMin(), converter.getXMax(),
                converter.getYMin(), converter.getYMax(),
                fractal.getMaxIterations()
        );
    }

    public void openTourWindow() {
        ColorFunction cf = (painter instanceof FractalPainter fp)
                ? fp.getColorFunction()
                : lists.getColorSchemes().getFirst();
        if (tourWindow == null || !tourWindow.isDisplayable()) {
            tourWindow = new TourWindow(fractal, converter, cf);
            tourWindow.setVisible(true);
        } else {
            tourWindow.setColorFunction(cf);
            tourWindow.toFront();
        }
    }
}
