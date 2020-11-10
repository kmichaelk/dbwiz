package ru.mkr.dbwiz.modules;

import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.exceptions.ModuleInitializationException;
import ru.mkr.dbwiz.handlers.ProjectExecutionHandler;
import ru.mkr.dbwiz.utilities.ModuleUtility;

/*
 * Унифицированный модуль Excel,
 * всегда соотвествует документации вызовов JS
 *
 * Created on 27/10/20
 * (c) Mikhail K., 2020
 */
public abstract class AbstractExcelModule extends Module {

    private static final String JS_LIBRARY = ModuleUtility.loadBundledScript("/modules/excel.js");

    protected int BOOK_COUNTER = 0;

    public AbstractExcelModule(String name, ProjectExecutionHandler handler) throws ModuleInitializationException {
        super(name, handler);
        if(JS_LIBRARY == null) {
            throw ModuleUtility.newScriptInitializationException();
        }
    }

    @Override
    public void inject(ScriptEngine engine) {
        engine.loadLibrary(JS_LIBRARY);
        engine.bindFunction("_excelReadBook", (args) -> {
            String path = ((String) args[0]);
            int id = (int) args[1];

            if(path == null) {
                throw new IllegalArgumentException("Путь к книге не указан");
            }

            try {
                return readBook(engine, path, id, ((Object[]) args[2]));
            } catch (Exception ex) {
                print("Ошибка при чтении книги: %s", ex.getMessage());
                ex.printStackTrace();
                handler.onProjectExecutionException(ex);
                return null;
            }
        }, 3);
        engine.bindFunction("_excelObtainId", (args -> BOOK_COUNTER++));
        engine.bindFunction("_excelWriteCell", (args -> {
            int id = (int) args[0];
            String sheetName = String.valueOf(args[1]);
            int rowId = (int) args[2];
            int columnId = (int) args[3];
            String value = args[4] == null ? "" : String.valueOf(args[4]);

            if(sheetName == null) {
                throw new IllegalArgumentException("Название листа не указано");
            }
            return writeCell(id, sheetName, rowId, columnId, value);
        }), 5);
        engine.bindFunction("_excelSaveBook", (args -> {
            try {
                int id = (int) args[0];
                String targetPath = ((String) args[1]);
                if(targetPath == null) {
                    throw new IllegalArgumentException("Путь для сохранения не указан");
                }
                return saveBook(id, targetPath);
            } catch (Exception ex) {
                print("Ошибка при сохранении книги: %s", ex.getMessage());
                handler.onProjectExecutionException(ex);
                return false;
            }
        }), 2);
        engine.bindFunction("_excelCloseBook", (args -> {
            int id = (int) args[0];
            return closeBook(id);
        }), 1);
    }

    protected abstract Object readBook(ScriptEngine engine, String path, int id, Object[] sheetsToLoadArray) throws Exception;
    protected abstract boolean writeCell(int id, String sheetName, int rowId, int columnId, String value);
    protected abstract boolean saveBook(int id, String targetPath) throws Exception;
    protected abstract boolean closeBook(int id);
}
