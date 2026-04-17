package ru.gr0946x.ui.tour;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TourKeyframeTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"№", "Re [min, max]", "Im [min, max]", "Итераций", "Длит. (с)"};
    private final List<TourKeyframe> keyframes = new ArrayList<>();

    public void addKeyframe(TourKeyframe kf) {
        keyframes.add(kf);
        fireTableRowsInserted(keyframes.size() - 1, keyframes.size() - 1);
    }

    public void removeKeyframe(int row) {
        if (row < 0 || row >= keyframes.size()) return;
        keyframes.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void moveUp(int row) {
        if (row <= 0 || row >= keyframes.size()) return;
        TourKeyframe kf = keyframes.remove(row);
        keyframes.add(row - 1, kf);
        fireTableRowsUpdated(row - 1, row);
    }

    public void moveDown(int row) {
        if (row < 0 || row >= keyframes.size() - 1) return;
        TourKeyframe kf = keyframes.remove(row);
        keyframes.add(row + 1, kf);
        fireTableRowsUpdated(row, row + 1);
    }

    public List<TourKeyframe> getKeyframes() {
        return keyframes;
    }

    @Override public int getRowCount() { return keyframes.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Class<?> getColumnClass(int col) {
        return switch (col) {
            case 0, 3 -> Integer.class;
            case 4 -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int row, int col) { return col == 4; }

    @Override
    public Object getValueAt(int row, int col) {
        TourKeyframe kf = keyframes.get(row);
        return switch (col) {
            case 0 -> row + 1;
            case 1 -> String.format("[%.4f, %.4f]", kf.getState().xMin(), kf.getState().xMax());
            case 2 -> String.format("[%.4f, %.4f]", kf.getState().yMin(), kf.getState().yMax());
            case 3 -> kf.getState().maxIterations();
            case 4 -> kf.getDurationSeconds();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col != 4) return;
        try {
            double d = Double.parseDouble(value.toString());
            d = Math.max(0.1, Math.min(3600.0, d));
            keyframes.get(row).setDurationSeconds(d);
            fireTableCellUpdated(row, col);
        } catch (NumberFormatException ignored) {}
    }
}
