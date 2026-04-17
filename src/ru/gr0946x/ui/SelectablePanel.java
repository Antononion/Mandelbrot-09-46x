package ru.gr0946x.ui;

import ru.gr0946x.ui.functions.StateChangeListener;
import ru.gr0946x.ui.io.ImageSerializer;
import ru.gr0946x.Converter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class SelectablePanel extends PaintPanel{
    private SelectedRect rect = null;
    private Graphics g;
    private boolean isRightDragging = false;
    private int lastDragX;
    private int lastDragY;
    private final Converter converter;
    private StateChangeListener stateChangeListener;
    private final ArrayList<SelectListener> selectHandlers = new ArrayList<>();

    public SelectablePanel(Painter painter, Converter converter, ImageSerializer imageSerializer) {
        super(painter, imageSerializer);
        this.converter = converter;
        g = getGraphics();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    rect = new SelectedRect(e.getX(), e.getY());
                    paintSelectedRect();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (stateChangeListener != null) {
                        stateChangeListener.onStateChange();
                    }
                    isRightDragging = true;
                    lastDragX = e.getX();
                    lastDragY = e.getY();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (SwingUtilities.isLeftMouseButton(e) && rect != null) {
                    paintSelectedRect();
                    for (var handler : selectHandlers) {
                        handler.onSelect(new Rectangle(
                                        rect.getUpperLeft().x,
                                        rect.getUpperLeft().y,
                                        rect.getWidth(),
                                        rect.getHeight()
                                )
                        );
                    }
                    rect = null;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    isRightDragging = false;
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (SwingUtilities.isLeftMouseButton(e) && rect != null) {
                    paintSelectedRect();
                    rect.setLastPointConstrained(e.getX(), e.getY(), getWidth(), getHeight());
                    paintSelectedRect();
                } else if (SwingUtilities.isRightMouseButton(e) && isRightDragging) {
                    int deltaX = e.getX() - lastDragX;
                    int deltaY = e.getY() - lastDragY;

                    double xShift = converter.xScr2Crt(deltaX) - converter.xScr2Crt(0);
                    double yShift = converter.yScr2Crt(deltaY) - converter.yScr2Crt(0);

                    double xMin = converter.getXMin();
                    double xMax = converter.getXMax();
                    double yMin = converter.getYMin();
                    double yMax = converter.getYMax();

                    converter.setXShape(xMin - xShift, xMax - xShift);
                    converter.setYShape(yMin - yShift, yMax - yShift);

                    lastDragX = e.getX();
                    lastDragY = e.getY();

                    repaint();
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                g = getGraphics();
                adjustAspectRatio();
            }
        });
    }

    private void adjustAspectRatio() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        double cx = (converter.getXMin() + converter.getXMax()) / 2.0;
        double cy = (converter.getYMin() + converter.getYMax()) / 2.0;
        double xRange = converter.getXMax() - converter.getXMin();
        double newYRange = xRange * h / w;
        converter.setYShape(cy - newYRange / 2.0, cy + newYRange / 2.0);
    }

    public void addSelectListener(SelectListener listener) {
        selectHandlers.add(listener);
    }

    public void removeSelectListener(SelectListener listener) {
        selectHandlers.remove(listener);
    }

    public void setStateChangeListener(StateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    private void paintSelectedRect() {
        if (g != null) {
            g.setXORMode(Color.WHITE);
            g.setColor(Color.BLACK);
            g.drawRect(
                    rect.getUpperLeft().x,
                    rect.getUpperLeft().y,
                    rect.getWidth(),
                    rect.getHeight()
            );
            g.setPaintMode();
        }
    }
}
