package ru.gr0946x.ui.tour;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.FractalState;
import ru.gr0946x.ui.fractals.QuadraticFractal;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

public class TourWindow extends JFrame {

    private final QuadraticFractal sourceFractal;
    private final Converter liveConverter;
    private ColorFunction colorFunction;

    private final TourKeyframeTableModel tableModel;
    private final JTable table;
    private final JSpinner widthSpinner;
    private final JSpinner heightSpinner;
    private final JSpinner fpsSpinner;

    public TourWindow(QuadraticFractal fractal, Converter converter, ColorFunction colorFunction) {
        this.sourceFractal = fractal;
        this.liveConverter = converter;
        this.colorFunction = colorFunction;

        setTitle("Экскурсия по фракталу");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 400);
        setMinimumSize(new Dimension(560, 300));

        tableModel = new TourKeyframeTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(3).setMaxWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        widthSpinner  = new JSpinner(new SpinnerNumberModel(1280, 320, 7680, 16));
        heightSpinner = new JSpinner(new SpinnerNumberModel(720,  240, 4320, 16));
        fpsSpinner    = new JSpinner(new SpinnerNumberModel(30,   1,   60,   1));

        setLayout(new BorderLayout(4, 4));
        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildSettingsBar(), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    public void setColorFunction(ColorFunction cf) {
        this.colorFunction = cf;
    }

    // ─── Toolbar ───────────────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JButton addBtn    = new JButton("Добавить кадр");
        JButton removeBtn = new JButton("Удалить");
        JButton upBtn     = new JButton("▲");
        JButton downBtn   = new JButton("▼");
        JButton exportBtn = new JButton("Экспорт...");

        addBtn.addActionListener(_    -> addCurrentView());
        removeBtn.addActionListener(_ -> removeSelected());
        upBtn.addActionListener(_     -> moveSelected(-1));
        downBtn.addActionListener(_   -> moveSelected(+1));
        exportBtn.addActionListener(_ -> startExport());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panel.add(addBtn);
        panel.add(removeBtn);
        panel.add(upBtn);
        panel.add(downBtn);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(exportBtn);
        return panel;
    }

    private JPanel buildSettingsBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        panel.add(new JLabel("Ширина:"));
        panel.add(widthSpinner);
        panel.add(new JLabel("Высота:"));
        panel.add(heightSpinner);
        panel.add(new JLabel("FPS:"));
        panel.add(fpsSpinner);
        return panel;
    }

    // ─── Actions ───────────────────────────────────────────────────────────────

    private void addCurrentView() {
        FractalState state = new FractalState(
                liveConverter.getXMin(), liveConverter.getXMax(),
                liveConverter.getYMin(), liveConverter.getYMax(),
                sourceFractal.getMaxIterations()
        );
        tableModel.addKeyframe(new TourKeyframe(state, 3.0));
        int last = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(last, last);
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row >= 0) tableModel.removeKeyframe(row);
    }

    private void moveSelected(int delta) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        if (delta < 0) tableModel.moveUp(row);
        else           tableModel.moveDown(row);
        int newRow = Math.max(0, Math.min(tableModel.getRowCount() - 1, row + delta));
        table.setRowSelectionInterval(newRow, newRow);
    }

    private void startExport() {
        if (tableModel.getRowCount() < 2) {
            JOptionPane.showMessageDialog(this,
                    "Нужно минимум 2 ключевых кадра.", "Экспорт", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("AVI-видео (*.avi)", "avi"));
        fc.setDialogTitle("Сохранить видео");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = enforceExtension(fc.getSelectedFile(), "avi");
        launchExportWorker(file);
    }

    private void launchExportWorker(File file) {
        int w   = (Integer) widthSpinner.getValue();
        int h   = (Integer) heightSpinner.getValue();
        int fps = (Integer) fpsSpinner.getValue();

        // Deep copy so the worker snapshot is independent of UI changes
        List<TourKeyframe> snapshot = deepCopy(tableModel.getKeyframes());

        // Progress dialog (modal)
        JDialog progressDialog = new JDialog(this, "Экспорт видео...", true);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        JButton cancelBtn = new JButton("Отмена");
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(new JLabel("Рендеринг кадров..."), BorderLayout.NORTH);
        content.add(bar, BorderLayout.CENTER);
        content.add(cancelBtn, BorderLayout.SOUTH);
        progressDialog.setContentPane(content);
        progressDialog.setSize(340, 130);
        progressDialog.setResizable(false);
        progressDialog.setLocationRelativeTo(this);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                TourExporter exporter = new TourExporter(
                        sourceFractal, colorFunction, snapshot, fps, w, h);
                exporter.export(file, this::publish, this::isCancelled);
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                bar.setValue(chunks.getLast());
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    get();
                    JOptionPane.showMessageDialog(TourWindow.this,
                            "Видео сохранено:\n" + file.getAbsolutePath(),
                            "Экспорт завершён", JOptionPane.INFORMATION_MESSAGE);
                } catch (CancellationException ignored) {
                    // user cancelled — silently close
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(TourWindow.this,
                            "Ошибка экспорта:\n" + cause.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        cancelBtn.addActionListener(_ -> worker.cancel(true));
        worker.execute();
        progressDialog.setVisible(true); // blocks EDT until done() calls dispose()
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private static File enforceExtension(File file, String ext) {
        String name = file.getName();
        if (!name.toLowerCase().endsWith("." + ext))
            return new File(file.getParentFile(), name + "." + ext);
        return file;
    }

    private static List<TourKeyframe> deepCopy(List<TourKeyframe> src) {
        List<TourKeyframe> copy = new ArrayList<>(src.size());
        for (TourKeyframe kf : src)
            copy.add(new TourKeyframe(kf.getState(), kf.getDurationSeconds()));
        return copy;
    }
}
