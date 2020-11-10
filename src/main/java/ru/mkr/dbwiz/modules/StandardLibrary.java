package ru.mkr.dbwiz.modules;

import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.exceptions.ModuleInitializationException;
import ru.mkr.dbwiz.handlers.ProjectExecutionHandler;
import ru.mkr.dbwiz.utilities.ModuleUtility;

import java.io.File;
import java.util.Arrays;

/**
 * Standard library
 * for DBWIZ scripts
 *
 * Callbacks:
 * - print
 * - printf [x]
 *
 * - openFilePrompt
 * - openDirectoryPrompt
 *
 * - gc
 *
 * - setProjectState
 * - setProjectStatus
 *
 * - exit
 *
 * Created on 11/10/20
 * (c) Mikhail K., 2020
 */
public class StandardLibrary extends Module {

    private static final String JS_LIBRARY = ModuleUtility.loadBundledScript("/modules/std.js");

    public StandardLibrary(ProjectExecutionHandler handler) throws ModuleInitializationException {
        super("Стандартная библиотека", handler);
        if(JS_LIBRARY == null) {
            throw ModuleUtility.newScriptInitializationException();
        }
    }

    @Override
    public void inject(ScriptEngine engine) {
        engine.loadLibrary(JS_LIBRARY);
        engine.bindFunction("print", (args -> {
            for (Object arg : args) {
                print(arg.toString());
            }
            return null;
        }));
        // лучше не дергать лишний раз все эти объекты туда-сюда
        // todo: printf -> std.js
        engine.bindFunction("printf", (args -> {
            print(String.format((String.valueOf(args[0])), Arrays.copyOfRange(args, 1, args.length)));
            return null;
        }));

        engine.bindFunction("cwd", args -> System.getProperty("user.dir") + File.separator);

        engine.bindFunction("gc", args -> {
            System.gc();
            return null;
        });

        engine.bindFunction("sleep", args -> {
            long ms;
            try {
                ms = Long.parseLong(String.valueOf(args[0]));
            } catch (ClassCastException ex) {
                throw new RuntimeException("Некорректное время для приостановки потока", ex);
            }
            assert ms >= 0;
            try {
                Thread.sleep(ms);
            } catch (InterruptedException ex) {
                throw new RuntimeException("Во время ожидание возобновления работы потока произошла ошибка", ex);
            }
            return null;
        }, 1);

        engine.bindFunction("setProjectStatus", args -> {
            System.gc(); // обычно вся память уже засрана к концу первого этапа
            if(args.length == 1) {
                handler.onProjectStatusChange(((String) args[0]));
            }
            return null;
        }, 1);

        engine.bindFunction("setProjectProgress", args -> {
            if(args.length == 1) {
                handler.onProjectProgressChange(Integer.parseInt(String.valueOf(args[0])));
            }
            return null;
        }, 1);

        engine.bindFunction("exit", (args -> {
            int exitCode = 0;
            if(args.length == 1 && args[0] instanceof Integer) {
                exitCode = ((int) args[0]);
            }
            handler.onProjectExecutionInterruptRequest(exitCode);
            return null;
        }));
    }

    @Override
    public void unload() {

    }

    @Override
    protected void print(String s, Object... args) {
        printStream.println(String.format(s, args));
    }
}
