package ru.mkr.dbwiz;

import ru.mkr.dbwiz.frames.MainFrame;
import ru.mkr.dbwiz.utilities.UIUtility;
import ru.mkr.dbwiz.modules.ExcelModule;
import ru.mkr.dbwiz.modules.Module;
import ru.mkr.dbwiz.modules.NativeExcelModule;

// Для того, чтобы сборщик мусора возвращал память
// кучи операционной системе, нужно запускать
// JVM со сборщиком мусора G1GC:
// -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC
public class Main {

    public static final String NAME = "DBWIZ";
    public static final String VERSION = "0.5-preview";

    public static final String PROJECTS_DIRECTORY = "projects";

    public static void main(String[] args) {
        System.out.println(NAME + " version " + VERSION);
        System.out.println("(c) Mikhail K., 2020");
        System.out.println();

        if(args.length == 1) {
            switch (args[0]) {
                case "--check-native-excel-availability": {
                    try {
                        NativeExcelModule.checkAvailability();
                        System.out.println("Available");
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    return;
                }
                case "--disable-native-excel-availability": {
                    System.out.println("Native Excel Module has been disabled");
                    Module.EXCEL_MODULE = ExcelModule.class;
                    break;
                }
            }
        }

        UIUtility.initUI();
        new MainFrame();
    }
}
