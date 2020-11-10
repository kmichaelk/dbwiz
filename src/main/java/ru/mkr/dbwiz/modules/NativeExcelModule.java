package ru.mkr.dbwiz.modules;

import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.engine.ScriptEngineCompat;
import ru.mkr.dbwiz.exceptions.ModuleInitializationException;
import ru.mkr.dbwiz.handlers.ProjectExecutionHandler;
import ru.mkr.dbwiz.utilities.NativeUtility;

import java.util.*;
import java.util.stream.Collectors;

/*
 * Powered by xlnt
 * https://github.com/tfussell/xlnt
 * MIT License
 *
 * Вся логика имплементирована
 * в нативной JNI-библиотеке
 *
 * Created on 24/10/20
 * (c) Mikhail K., 2020
 */
// 01/11/20: для запуска .dll на Windows требуется установка vc_redist
public class NativeExcelModule extends AbstractExcelModule {

    public NativeExcelModule(ProjectExecutionHandler handler) throws ModuleInitializationException {
        super("Excel C/C++", handler);
        //_setExceptionHandlingEnabled(false); // по умолчанию - true
    }

    @Override
    protected Object readBook(ScriptEngine engine, String path, int id, Object[] sheetsToLoadArray) throws Exception {
        List<String> sheetsToLoad = Arrays.stream(sheetsToLoadArray)
                .map(String::valueOf).collect(Collectors.toList());

        boolean filterSheets = !sheetsToLoad.isEmpty();

        // C/C++ -> Java
        Map<String, String[][]> _sheetMap = new HashMap<>();
        _readExcelBook(
                id,
                path,
                _sheetMap,
                (filterSheets ? sheetsToLoad.toArray(new String[0]) : null),
                filterSheets
        );
        if (_sheetMap.isEmpty()) {
            return -2;
        }

        Map<String, List<List<String>>> sheetMap = new HashMap<>();
        for (Map.Entry<String, String[][]> entry : _sheetMap.entrySet()) {
            List<List<String>> val2D = new ArrayList<>();
            for (String[] arr2D : entry.getValue()) {
                val2D.add(Arrays.asList(arr2D));
            }
            sheetMap.put(entry.getKey(), val2D);
        }

        _sheetMap.clear();
        _sheetMap = null;

        Object runtimeObject = ScriptEngineCompat.convertMapToRuntimeFormat(engine, sheetMap);

        sheetMap.clear();
        sheetMap = null;

        System.gc();
        return runtimeObject;
    }

    @Override
    protected boolean writeCell(int id, String sheetName, int rowId, int columnId, String value) {
        return _writeCell(id, sheetName, rowId, columnId, value);
    }

    @Override
    protected boolean saveBook(int id, String targetPath) throws Exception {
        return _saveBook(id, targetPath);
    }

    @Override
    protected boolean closeBook(int id) {
        try {
            assert this._closeBook(id);
            return true;
        } catch (Throwable t) {
            print("Ошибка при закрытии книги: %s",
                    (t instanceof AssertionError ? "не удалось закрыть книгу" : t.getMessage()));
            handler.onProjectExecutionException(new RuntimeException("Ошибка при закрытии книги", t));
            return false;
        }
    }

    @Override
    public void unload() {
        this._dispose();
    }

    private native boolean _setExceptionHandlingEnabled(boolean b); // true - исключения C/C++ обертываются в NativeLibraryException, false - краш JVM
    private native boolean _readExcelBook(int id, String filePath, Map<String, String[][]> sheetMap, String[] sheetsToLoad, boolean filterSheets);
    private native boolean _writeCell(int id, String sheetName, int rowId, int columnId, String value);
    private native boolean _saveBook(int id, String targetPath);
    private native boolean _closeBook(int id);
    private native boolean _dispose();
    static {
        NativeUtility.loadLibrary("excelm");
    }
    public static void checkAvailability() {} // выбросит исключение при ошибке загрузки библиотеки в static-блоке
}
