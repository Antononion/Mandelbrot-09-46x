package ru.gr0946x.ui.io;

import ru.gr0946x.ui.FunctionAndColorSchemesLists;
import ru.gr0946x.ui.julia.JuliaSetWindow;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class JuliaMenuProvider implements MenuProvider {
    private final JuliaSetWindow juliaWindow;
    private final FunctionAndColorSchemesLists lists;

    public JuliaMenuProvider(JuliaSetWindow juliaWindow) {
        this.juliaWindow = juliaWindow;
        this.lists = new FunctionAndColorSchemesLists();
    }

    @Override
    public JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic('F');

        JMenuItem saveAsItem = new JMenuItem("Сохранить как...");
        saveAsItem.addActionListener(_ -> juliaWindow.saveFractal());
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("control S"));

        JMenuItem openItem = new JMenuItem("Открыть...");
        openItem.addActionListener(_ -> juliaWindow.openFile());
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));

        JMenuItem createAnimationItem = new JMenuItem("Создать анимацию...");
        createAnimationItem.setAccelerator(KeyStroke.getKeyStroke("control N"));

        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(createAnimationItem);

        return fileMenu;
    }

    @Override
    public JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Правка");
        editMenu.setMnemonic('E');

        JMenuItem undoItem = new JMenuItem("Отменить");
        undoItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));

        undoItem.setEnabled(juliaWindow.canUndo());
        undoItem.addActionListener(_ -> juliaWindow.triggerUndo());

        editMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                undoItem.setEnabled(juliaWindow.canUndo());
            }

            @Override
            public void menuDeselected(MenuEvent e) {}

            @Override
            public void menuCanceled(MenuEvent e) {}
        });

        editMenu.add(undoItem);
        return editMenu;
    }

    @Override
    public JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("Вид");
        viewMenu.setMnemonic('V');

        JMenu setColorSchemeMenu = new JMenu("Задать цветовую схему");
        ButtonGroup colorSchemeGroup = new ButtonGroup();
        JRadioButtonMenuItem colorScheme1Item = new JRadioButtonMenuItem("Схема 1");
        colorScheme1Item.setSelected(true);
        JRadioButtonMenuItem colorScheme2Item = new JRadioButtonMenuItem("Схема 2");
        JRadioButtonMenuItem colorScheme3Item = new JRadioButtonMenuItem("Схема 3");

        colorScheme1Item.addActionListener(_ ->
                juliaWindow.setCurrentColorFunction(lists.getColorSchemes().getFirst()));
        colorScheme2Item.addActionListener(_ ->
                juliaWindow.setCurrentColorFunction(lists.getColorSchemes().get(1)));
        colorScheme3Item.addActionListener(_ ->
                juliaWindow.setCurrentColorFunction(lists.getColorSchemes().get(2)));

        colorSchemeGroup.add(colorScheme1Item);
        colorSchemeGroup.add(colorScheme2Item);
        colorSchemeGroup.add(colorScheme3Item);

        setColorSchemeMenu.add(colorScheme1Item);
        setColorSchemeMenu.add(colorScheme2Item);
        setColorSchemeMenu.add(colorScheme3Item);

        JCheckBoxMenuItem adaptiveIterationsItem = new JCheckBoxMenuItem("Адаптивное число итераций");
        adaptiveIterationsItem.addActionListener(_ -> juliaWindow.setAdaptiveIterationsEnabled(adaptiveIterationsItem.isSelected()));
        adaptiveIterationsItem.setSelected(true);
        adaptiveIterationsItem.setAccelerator(KeyStroke.getKeyStroke("control I"));

        viewMenu.add(setColorSchemeMenu);
        viewMenu.addSeparator();
        viewMenu.add(adaptiveIterationsItem);

        return viewMenu;
    }
}
