package ru.mkr.dbwiz.modules;

import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.handlers.ProjectExecutionHandler;
import ru.mkr.dbwiz.utilities.UIUtility;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

public class UIModule extends Module {

    public UIModule(ProjectExecutionHandler handler) {
        super("UI", handler);
    }

    @Override
    public void inject(ScriptEngine engine) {

        engine.bindFunction("openFilePrompt", (args -> openFileChooser(args, JFileChooser.FILES_ONLY)), 1);
        engine.bindFunction("openDirectoryPrompt", (args -> openFileChooser(args, JFileChooser.DIRECTORIES_ONLY)), 1);

        // todo: сократить код
        engine.bindFunction("msg", (args -> {
            showMessage(JOptionPane.INFORMATION_MESSAGE, args);
            return null;
        }), 1);
        engine.bindFunction("warn", (args -> {
            showMessage(JOptionPane.WARNING_MESSAGE, args);
            return null;
        }), 1);
        engine.bindFunction("err", (args -> {
            showMessage(JOptionPane.ERROR_MESSAGE, args);
            return null;
        }), 1);

        engine.bindFunction("openInputPrompt", (args -> {
            String message = "Проект запросил ввод данных:";
            String title = handler.getProjectName() + ": Ввод данных";
            String defaultValue = null;
            if(args.length > 0 && args.length <= 3) {
                message = String.valueOf(args[0]);
                if(args.length > 1) {
                    title = String.valueOf(args[1]);
                }
                if(args.length == 3) {
                    defaultValue = String.valueOf(args[2]);
                }
            }
            return JOptionPane.showInputDialog(
                    null,
                    message,
                    title,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    defaultValue
            );
        }));

        engine.bindFunction("openConfirmPrompt", (args -> {
            String message = "Проект запросил подтверждение";
            String title = handler.getProjectName() + ": Подтверждение";
            if(args.length > 0 && args.length <= 2) {
                message = String.valueOf(args[0]);
                if(args.length == 2) {
                    title = String.valueOf(args[1]);
                }
            }

            int result = JOptionPane.showConfirmDialog(
                    null,
                    message,
                    title,
                    JOptionPane.YES_NO_OPTION
            );

            return result == JOptionPane.YES_OPTION;
        }));

        engine.bindFunction("openSelectPrompt", (args -> {
            String message = String.valueOf(args[0]);
            String title = handler.getProjectName() + ": Выбор данных";

            Object[] values = null;
            if(args.length < 3) {
                if(args.length == 2) {
                    Object o = args[1];
                    if(o instanceof Object[]) {
                        values = ((Object[]) o);
                    } else {
                        values = new Object[] { o };
                    }
                }
            } else {
                values = Arrays.copyOfRange(args, 1, args.length);
            }
            if(values == null) {
                throw new IllegalArgumentException("Требуется как минимум один вариант для выбора");
            }

            return JOptionPane.showInputDialog(
                    null,
                    message,
                    title,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    values,
                    null);
        }), 2);
    }

    @Override
    public void unload() {}

    private File lastDirectory = null;
    private String openFileChooser(Object[] args, int mode) {
        String title = handler.getProjectName() + ": Выбор файла";
        if(args.length == 1) {
            title = String.valueOf(args[0]);
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setApproveButtonText("Выбрать");
        chooser.setFileSelectionMode(mode);
        if(lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selection = chooser.getSelectedFile();
            lastDirectory = mode == JFileChooser.DIRECTORIES_ONLY ? selection : selection.getParentFile();
            return selection.getAbsolutePath();
        }
        return null;
    }

    private void showMessage(int type, Object... args) {
        String message = String.valueOf(args[0]);
        String title = handler.getProjectName() + ": ";
        switch (type) {
            case JOptionPane.INFORMATION_MESSAGE: {
                title += "Информация";
                break;
            }
            case JOptionPane.WARNING_MESSAGE: {
                title += "Предупреждение";
                break;
            }
            case JOptionPane.ERROR_MESSAGE: {
                title += "Ошибка";
                break;
            }
            default: {
                title += "Сообщение";
                break;
            }
        }
        if(args.length == 2) {
            title = String.valueOf(args[1]);
        }
        UIUtility.showMessage(title, message, type);
    }
}
