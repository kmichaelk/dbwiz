package ru.mkr.dbwiz.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

public class UIUtility {

    private static boolean IS_UI_INITIALIZED = false;

    public static void initUI() {
        IS_UI_INITIALIZED = true;
        try {
            String lf = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lf);
        } catch (Exception ex) {
            System.out.println("Failed to set native Look and Feel: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static boolean isUIAvailable() {
        return IS_UI_INITIALIZED;
    }

    public static void showMessage(String title, String message, int type, Component parent) {
        JOptionPane.showMessageDialog(
                parent,
                "<html><body style=\"width: 200px\">"
                        + message.replace("\n", "<br>")
                        + "</body></html>",
                title,
                type
        );
    }

    public static void showMessage(String title, String message, int type) {
        showMessage(title, message, type, null);
    }

    public static JMenu getMenu(String title) {
        return new JMenu(title);
    }

    public static JMenu getMenu(String title, int keyCode) {
        JMenu item = getMenu(title);
        item.setMnemonic(keyCode);
        return item;
    }

    public static JMenuItem getMenuItem(String title, ActionListener actionListener) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(actionListener);
        return item;
    }

    public static JMenuItem getMenuItem(String title, int keyCode, ActionListener actionListener) {
        JMenuItem item = getMenuItem(title, actionListener);
        item.setMnemonic(keyCode);
        item.setAccelerator(KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_MASK));
        return item;
    }
}
