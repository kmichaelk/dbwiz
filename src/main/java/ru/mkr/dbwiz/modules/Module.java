package ru.mkr.dbwiz.modules;

import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.handlers.ProjectExecutionHandler;
import ru.mkr.dbwiz.utilities.UIUtility;
import ru.mkr.dbwiz.exceptions.ModuleInitializationException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Module {

    protected final String name;
    protected final ProjectExecutionHandler handler;
    protected PrintStream printStream = System.out;

    public Module(String name, ProjectExecutionHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public abstract void inject(ScriptEngine engine);

    public abstract void unload();

    protected void print(String s, Object... args) {
        printStream.println("[" + name + "] " + String.format(s, args));
    }


    public static Class<? extends Module> EXCEL_MODULE;
    public static List<Class<? extends Module>> modules() {
        if(EXCEL_MODULE == null) {
            try {
                NativeExcelModule.checkAvailability();
                EXCEL_MODULE = NativeExcelModule.class;
            } catch (Throwable t) {
                EXCEL_MODULE = ExcelModule.class;
                System.out.println("Нативный модуль Excel недоступен: " + t.getMessage());
                t.printStackTrace();
            }
            // Принудительно переключиться на Java Excel Module
            // EXCEL_MODULE = ExcelModule.class;
        }

        List<Class<? extends Module>> modules = Arrays.asList(
                StandardLibrary.class,
                EXCEL_MODULE,
                WordModule.class
        );
        if(UIUtility.isUIAvailable()) {
            modules = new ArrayList<>(modules);
            modules.add(1, UIModule.class);
        }
        return modules;
    }

    public static Module instantiate(Class<? extends Module> clazz, ProjectExecutionHandler handler)
            throws ModuleInitializationException {
        try {
            return clazz.getDeclaredConstructor(ProjectExecutionHandler.class).newInstance(handler);
        } catch (ReflectiveOperationException ex) {
            throw new ModuleInitializationException("Failed to instantiate module " + clazz.getSimpleName() + ": " + ex.getMessage(), ex);
        }
    }

    public static Module instantiate(Class<? extends Module> clazz, ProjectExecutionHandler handler,
                                     PrintStream printStream) throws ModuleInitializationException {
        Module module = instantiate(clazz, handler);
        module.printStream = printStream;
        return module;
    }
}
