package ru.gr0946x.ui.io;

import ru.gr0946x.ui.FunctionAndColorSchemesLists;
import ru.gr0946x.ui.MainWindow;

import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

public class MainMenuProvider implements MenuProvider {
    private final MainWindow mainWindow;
    private final FunctionAndColorSchemesLists lists;

    public MainMenuProvider(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.lists = new FunctionAndColorSchemesLists();
    }

    @Override
    public JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic('F');

        JMenuItem saveAsItem = new JMenuItem("Сохранить как...");
        saveAsItem.addActionListener(_ -> mainWindow.saveFractal());
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("control S"));

        JMenuItem openItem = new JMenuItem("Открыть...");
        openItem.addActionListener(_ -> mainWindow.openFile());
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));

        JMenuItem tourItem = new JMenuItem("Экскурсия по фракталу...");
        tourItem.addActionListener(_ -> mainWindow.openTourWindow());

        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(tourItem);

        return fileMenu;
    }

    @Override
    public JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Правка");
        editMenu.setMnemonic('E');

        JMenuItem undoItem = new JMenuItem("Отменить");
        undoItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));

        undoItem.setEnabled(mainWindow.canUndo());
        undoItem.addActionListener(_ -> mainWindow.triggerUndo());

        editMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                undoItem.setEnabled(mainWindow.canUndo());
            }

            @Override
            public void menuDeselected(MenuEvent e) {}

            @Override
            public void menuCanceled(MenuEvent e) {}
        });

        JCheckBoxMenuItem adaptiveIterationsItem = new JCheckBoxMenuItem("Адаптивное число итераций");
        adaptiveIterationsItem.addActionListener(_ -> mainWindow.setAdaptiveIterationsEnabled(adaptiveIterationsItem.isSelected()));
        adaptiveIterationsItem.setSelected(true);
        adaptiveIterationsItem.setAccelerator(KeyStroke.getKeyStroke("control I"));

        editMenu.add(undoItem);
        editMenu.addSeparator();
        editMenu.add(adaptiveIterationsItem);
        return editMenu;
    }

    @Override
    public JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("Вид");
        viewMenu.setMnemonic('V');


        JMenu setFractalFuncMenu = new JMenu("Задать функцию построения фрактала");
        ButtonGroup functionGroup = new ButtonGroup();
        JRadioButtonMenuItem fractalFunc1Item = new JRadioButtonMenuItem("Функция 1");
        fractalFunc1Item.setSelected(true);
        JRadioButtonMenuItem fractalFunc2Item = new JRadioButtonMenuItem("Функция 2");
        JRadioButtonMenuItem fractalFunc3Item = new JRadioButtonMenuItem("Функция 3");

        functionGroup.add(fractalFunc1Item);
        functionGroup.add(fractalFunc2Item);
        functionGroup.add(fractalFunc3Item);

        setFractalFuncMenu.add(fractalFunc1Item);
        setFractalFuncMenu.add(fractalFunc2Item);
        setFractalFuncMenu.add(fractalFunc3Item);


        JMenu setColorSchemeMenu = new JMenu("Задать цветовую схему");
        ButtonGroup colorSchemeGroup = new ButtonGroup();
        JRadioButtonMenuItem colorScheme1Item = new JRadioButtonMenuItem("Схема 1");
        colorScheme1Item.setSelected(true);
        JRadioButtonMenuItem colorScheme2Item = new JRadioButtonMenuItem("Схема 2");
        JRadioButtonMenuItem colorScheme3Item = new JRadioButtonMenuItem("Схема 3");

        colorSchemeGroup.add(colorScheme1Item);
        colorSchemeGroup.add(colorScheme2Item);
        colorSchemeGroup.add(colorScheme3Item);

        setColorSchemeMenu.add(colorScheme1Item);
        setColorSchemeMenu.add(colorScheme2Item);
        setColorSchemeMenu.add(colorScheme3Item);


        // Функции фракталов
        fractalFunc1Item.addActionListener(_ ->
                mainWindow.setCurrentFractal(lists.getFractalFunctions().getFirst()));
        fractalFunc2Item.addActionListener(_ ->
                mainWindow.setCurrentFractal(lists.getFractalFunctions().get(1)));
        fractalFunc3Item.addActionListener(_ ->
                mainWindow.setCurrentFractal(lists.getFractalFunctions().get(2)));

        // Цветовые схемы
        colorScheme1Item.addActionListener(_ ->
                mainWindow.setCurrentColorFunction(lists.getColorSchemes().getFirst()));
        colorScheme2Item.addActionListener(_ ->
                mainWindow.setCurrentColorFunction(lists.getColorSchemes().get(1)));
        colorScheme3Item.addActionListener(_ ->
                mainWindow.setCurrentColorFunction(lists.getColorSchemes().get(2)));

        viewMenu.add(setFractalFuncMenu);
        viewMenu.add(setColorSchemeMenu);

        return viewMenu;
    }
}
