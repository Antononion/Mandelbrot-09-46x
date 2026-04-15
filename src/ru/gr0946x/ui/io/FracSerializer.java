package ru.gr0946x.ui.io;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.FractalState;
import ru.gr0946x.ui.fractals.QuadraticFractal;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

public class FracSerializer implements FractalSerializer {

    @Override
    public void save(Component parent, Converter conv, QuadraticFractal fractal) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Файлы фракталов (*.frac)", "frac"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            if (!file.getName().toLowerCase().endsWith(".frac")) {
                file = new File(file.getParentFile(), file.getName() + ".frac");
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                FractalState state = new FractalState(
                        conv.getXMin(),
                        conv.getXMax(),
                        conv.getYMin(),
                        conv.getYMax(),
                        fractal.getMaxIterations()
                );
                oos.writeObject(state);
                JOptionPane.showMessageDialog(parent, "Фрактал успешно сохранен!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Ошибка сохранения: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void open(Component parent, Converter conv, QuadraticFractal fractal, Runnable onSuccess) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Файлы фракталов (*.frac)", "frac"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                FractalState state = (FractalState) ois.readObject();

                conv.setXShape(state.xMin(), state.xMax());
                conv.setYShape(state.yMin(), state.yMax());
                fractal.setMaxIterations(state.maxIterations());

                onSuccess.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Ошибка загрузки файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveWithFormatChoice(Component parent, Converter conv, QuadraticFractal fractal, JPanel paintPanel, ImageSerializer imageSerializer) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить фрактал");

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Файлы фракталов (*.frac)", "frac"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG изображения (*.png)", "png"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG изображения (*.jpg)", "jpg"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());

        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String extension = "frac";

            if (fileChooser.getFileFilter() instanceof FileNameExtensionFilter) {
                extension = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
            }

            if (extension.equals("frac")) {
                String path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".frac")) {
                    int dot = path.lastIndexOf(".");
                    path = (dot > 0 ? path.substring(0, dot) : path) + ".frac";
                    file = new File(path);
                }
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(new FractalState(conv.getXMin(), conv.getXMax(), conv.getYMin(), conv.getYMax(), fractal.getMaxIterations()));
                    JOptionPane.showMessageDialog(parent, "Фрактал сохранён!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else if (extension.equals("png")) {
                imageSerializer.saveImage(parent, conv, paintPanel, file, "png");
            } else if (extension.equals("jpg")) {
                imageSerializer.saveImage(parent, conv, paintPanel, file, "jpg");
            }
        }
    }

    public void openWithFormatChoice(Component parent, Converter conv, QuadraticFractal fractal, JPanel paintPanel, ImageSerializer imageSerializer) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Открыть файл");

        FileNameExtensionFilter fracFilter = new FileNameExtensionFilter("Файлы фракталов (*.frac)", "frac");
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG изображения (*.png)", "png");
        FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG изображения (*.jpg)", "jpg");

        fileChooser.addChoosableFileFilter(fracFilter);
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.addChoosableFileFilter(jpgFilter);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String name = file.getName().toLowerCase();

            if (name.endsWith(".frac")) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    FractalState state = (FractalState) ois.readObject();
                    conv.setXShape(state.xMin(), state.xMax());
                    conv.setYShape(state.yMin(), state.yMax());
                    fractal.setMaxIterations(state.maxIterations());

                    // ОЧИЩАЕМ ИЗОБРАЖЕНИЕ ПЕРЕД ОТКРЫТИЕМ ФРАКТАЛА
                    imageSerializer.clearImage();

                    ((JFrame) parent).repaint();
                    JOptionPane.showMessageDialog(parent, "Фрактал загружен!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                imageSerializer.openImage(parent, paintPanel, file);
            } else {
                JOptionPane.showMessageDialog(parent, "Неподдерживаемый формат", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
