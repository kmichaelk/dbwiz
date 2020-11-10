package ru.mkr.dbwiz;

import ru.mkr.dbwiz.data.DataFile;
import ru.mkr.dbwiz.data.ScriptFile;
import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.engine.impl.V8ScriptEngine;
import ru.mkr.dbwiz.enums.ProjectExecutionState;
import ru.mkr.dbwiz.exceptions.EngineInitializationException;
import ru.mkr.dbwiz.exceptions.ModuleInitializationException;
import ru.mkr.dbwiz.handlers.ProjectExecutionHandler;
import ru.mkr.dbwiz.modules.Module;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Created on 18/10/20
 * (c) Mikhail K., 2020
 */
public class ProjectExecutor {

    public static final String OUTPUT_TEXT_SEPARATOR = "-----------------";

    public static void runProject(String fileName) {
        ProjectExecutionHandler.DummyProjectExecutionHandler handler
                = new ProjectExecutionHandler.DummyProjectExecutionHandler();
        //Thread thread = new Thread(() -> {
        //    try {
        //        runProject(fileName, handler, System.out);
        //    } catch (Exception ex) {
        //        handler.onProjectExecutionException(ex);
        //    }
        //});
        //handler.setThread(thread);
        try {
            runProject(fileName, handler, System.out);
        } catch (Exception ex) {
            handler.onProjectExecutionException(ex);
        }
    }

    public static void runProject(String fileName, ProjectExecutionHandler handler, PrintStream out)
            throws IOException, EngineInitializationException {
        handler.onProjectStateChange(ProjectExecutionState.LOADING);
        out.print("Загрузка проекта... ");
        DataFile script = new ScriptFile(fileName);
        script.load();
        out.println("ОК");

        handler.onProjectStateChange(ProjectExecutionState.ENGINE_INITIALIZING);
        out.print("Инициализация движка... ");
        ScriptEngine engine = new V8ScriptEngine();
        try {
            engine.initialize();
        } catch (Exception ex) {
            throw new EngineInitializationException(ex.getMessage(), ex);
        }
        handler.onProjectEngineInitialized(engine);
        out.println("ОК");

        handler.onProjectStateChange(ProjectExecutionState.MODULES_LOADING);
        Module.modules().forEach(module -> {
            try {
                out.print("Загрузка модуля " + module.getSimpleName() + "... ");
                engine.loadModule(Module.instantiate(module, handler, out));
                out.println("ОК");
            } catch (ModuleInitializationException ex) {
                out.println("Ошибка: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        handler.onProjectStateChange(ProjectExecutionState.STARTING);
        long start = System.currentTimeMillis();
        out.println();
        out.println("Запуск проекта...");
        out.println(OUTPUT_TEXT_SEPARATOR);
        String project = script.inline();

        handler.onProjectStateChange(ProjectExecutionState.EXECUTING);
        Object result = engine.execute(project);
        //if(result instanceof Integer) {
        //    int exitCode = ((Integer) result);
        //    handler.onProjectExecutionInterruptRequest(exitCode);
        //    return;
        //}

        handler.onProjectStateChange(ProjectExecutionState.FINISHED);

        engine.shutdown();
        if(engine.isRunning()) {
            engine.kill();
        }

        long elapsed = System.currentTimeMillis()-start;
        handler.onProjectExecutionTimeCalculated(elapsed);

        out.println(OUTPUT_TEXT_SEPARATOR);
        out.println("Выполнение проекта завершено (" + elapsed + " ms)");
    }
}
