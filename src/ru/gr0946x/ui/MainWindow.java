package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.*;
import ru.gr0946x.ui.functions.UndoManager;
import ru.gr0946x.ui.io.*;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private final Converter converter;
    private final QuadraticFractal fractal;
    private final FracSerializer fracSerializer;
    private final FractalFileManager fileManager;
    private final ImageSerializer imageSerializer;
    private boolean adaptiveIterationsEnabled = true;
    private final UndoManager undoManager;
    private final FunctionAndColorSchemesLists lists;
    private final Painter painter;
    private final SelectablePanel mainPanel;

    public MainWindow() {
        setTitle("Фрактал");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));

        converter = new Converter(-2.0, 1.0, -1.0, 1.0);
        fractal = new QuadraticFractal();
        fracSerializer = new FracSerializer();
        fileManager = new FractalFileManager(this, converter, fractal);
        imageSerializer = new ImageSerializer();
        undoManager = new UndoManager(this::restoreState);
        lists = new FunctionAndColorSchemesLists();
        ColorFunction firstColorScheme = lists.getColorSchemes().getFirst();
        painter = new FractalPainter(fractal, converter, firstColorScheme);
        mainPanel = new SelectablePanel(painter, converter, imageSerializer);
        mainPanel.setBackground(Color.WHITE);

        mainPanel.addSelectListener(r -> {
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
            mainPanel.repaint();
        });

        new MenuManager(this, new MainMenuProvider(this));

        getRootPane().registerKeyboardAction(
                e -> undoManager.undo(),
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setContent();
    }

    private void setContent() {
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

    public void setCurrentFractal(Fractal fractal) {
        if (painter instanceof FractalPainter fp)
            fp.setFractal(fractal);

        mainPanel.repaint();
    }

    public void setCurrentColorFunction(ColorFunction colorFunction) {
        if (painter instanceof FractalPainter fp)
            fp.setColorFunction(colorFunction);

        mainPanel.repaint();
    }

    private void restoreState(FractalState state) {
        converter.setXShape(state.xMin(), state.xMax());
        converter.setYShape(state.yMin(), state.yMax());
        fractal.setMaxIterations(state.maxIterations());
        mainPanel.repaint();
    }

    public void triggerUndo() {
        if (undoManager.undo())
            mainPanel.repaint();
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public void saveFractal() {
        fracSerializer.saveWithFormatChoice(this, converter, fractal, mainPanel, imageSerializer);
    }

    public void openFile() {
        fracSerializer.openWithFormatChoice(this, converter, fractal, mainPanel, imageSerializer);
    }

    public void setAdaptiveIterationsEnabled(boolean enabled) {
        this.adaptiveIterationsEnabled = enabled;
    }
}
