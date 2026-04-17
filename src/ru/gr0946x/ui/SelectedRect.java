package ru.gr0946x.ui;

import java.awt.*;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class SelectedRect {
    private final int x1;
    private int x2;
    private final int y1;
    private int y2;

    public Point getUpperLeft() {
        return new Point(min(x1, x2), min(y1, y2));
    }

    public int getWidth() {
        return abs(x1 - x2);
    }

    public int getHeight() {
        return abs(y1 - y2);
    }

    public SelectedRect(int x, int y) {
        x1 = x2 = x;
        y1 = y2 = y;
    }

    public void setLastPoint(int x, int y) {
        x2 = x;
        y2 = y;
    }

    /**
     * Like setLastPoint, but constrains the height so that
     * width/height == panelWidth/panelHeight (preserves aspect ratio).
     */
    public void setLastPointConstrained(int x, int y, int panelWidth, int panelHeight) {
        x2 = x;
        int absWidth = Math.abs(x2 - x1);
        int constrainedHeight = (int) Math.round((double) absWidth * panelHeight / panelWidth);
        y2 = y1 + (y >= y1 ? constrainedHeight : -constrainedHeight);
    }
}
