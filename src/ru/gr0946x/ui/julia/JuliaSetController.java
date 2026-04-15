package ru.gr0946x.ui.julia;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.MainWindow;
import ru.gr0946x.ui.SelectablePanel;
import ru.smak.math.Complex;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

public class JuliaSetController {
    public static void attachTo(MainWindow mainWindow) {
        try {
            Field mainPanelField = MainWindow.class.getDeclaredField("mainPanel");
            mainPanelField.setAccessible(true);
            SelectablePanel mainPanel = (SelectablePanel) mainPanelField.get(mainWindow);

            Field convField = MainWindow.class.getDeclaredField("converter");
            convField.setAccessible(true);
            Converter converter = (Converter) convField.get(mainWindow);

            mainPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        double x = converter.xScr2Crt(e.getX());
                        double y = converter.yScr2Crt(e.getY());
                        Complex point = new Complex(x, y);
                        JuliaSetWindow window = new JuliaSetWindow(point);
                        window.setLocationRelativeTo(mainWindow);
                        window.setVisible(true);
                    }
                }
            });

        } catch (NoSuchFieldException | IllegalAccessException ex) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Ошибка: не удалось подключить множество Жюлиа.\n" +
                            "Сообщение: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
