package ru.mkr.dbwiz.modules;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.engine.ScriptEngineCompat;
import ru.mkr.dbwiz.handlers.ProjectExecutionHandler;
import ru.mkr.dbwiz.exceptions.ModuleInitializationException;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Powered by Apache POI
 *
 * Created on 11/10/20
 * (c) Mikhail K., 2020
 */
public class ExcelModule extends AbstractExcelModule {

    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);
    private final Map<Integer, Workbook> LOADED_BOOKS = new HashMap<>();

    public ExcelModule(ProjectExecutionHandler handler) throws ModuleInitializationException {
        super("Excel", handler);
    }

    @Override
    protected Object readBook(ScriptEngine engine, String path, int id, Object[] sheetsToLoadArray) throws Exception {
        List<Object> sheetsToLoad = Arrays.asList(sheetsToLoadArray);

        boolean readonly = id == -1;
        boolean filterSheets = !sheetsToLoad.isEmpty();

        Workbook book = null;
        File file = new File(path);
        try {
            book = new XSSFWorkbook(file);
            FormulaEvaluator evaluator = null;
            try {
                evaluator = book.getCreationHelper().createFormulaEvaluator();
            } catch (Exception ignore) {
                print("Поддержка формул недоступна");
            }

            Map<String, List<List<String>>> sheetMap = new HashMap<>();
            for (Sheet sheet : book) {
                String sheetName = sheet.getSheetName();
                if(filterSheets && !sheetsToLoad.contains(sheetName)) {
                    continue;
                }

                List<List<String>> sheetRows = new ArrayList<>();

                int rowStart = Math.min(0, sheet.getFirstRowNum());
                int rowEnd = Math.max(0, sheet.getLastRowNum()) + 1;

                for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
                    List<String> cells = new ArrayList<>();
                    Row row = sheet.getRow(rowNum);
                    if (row != null) {
                        int lastColumn = Math.max(row.getLastCellNum(), 0);
                        for (int columnNum = 0; columnNum < lastColumn; columnNum++) {
                            Cell cell = row.getCell(columnNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            Object value = "";
                            if (cell != null) {
                                switch (cell.getCellType()) {
                                    case FORMULA: {
                                        if(evaluator == null) {
                                            break;
                                        }
                                        CellValue val = evaluator.evaluate(cell);
                                        switch (val.getCellType()) {
                                            case STRING: {
                                                value = val.getStringValue();
                                                break;
                                            }
                                            case NUMERIC: {
                                                value = readBigDecimal(cell.getNumericCellValue());
                                                break;
                                            }
                                            case BOOLEAN: {
                                                value = val.getBooleanValue();
                                                break;
                                            }
                                            case ERROR: {
                                                value = val.getErrorValue();
                                                break;
                                            }
                                            case BLANK: {
                                                value = "";
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                    case STRING: {
                                        value = cell.getStringCellValue();
                                        break;
                                    }
                                    case NUMERIC: {
                                        value = readBigDecimal(cell.getNumericCellValue());
                                        break;
                                    }
                                    case BOOLEAN: {
                                        value = cell.getBooleanCellValue();
                                        break;
                                    }
                                    case ERROR: {
                                        value = cell.getErrorCellValue();
                                        break;
                                    }
                                    case BLANK: {
                                        value = "";
                                        break;
                                    }
                                }
                            }
                            cells.add(String.valueOf(value));
                        }
                        if((rowNum % 5000) == 0) {
                            EXECUTOR.execute(System::gc);
                        }
                    }
                    sheetRows.add(cells);
                }

                sheetMap.put(sheetName, sheetRows);
                System.gc();
            }

            if (readonly) {
                closeBook(book);
                book = null;
            } else {
                LOADED_BOOKS.put(id, book);
            }

            if(sheetMap.isEmpty()) {
                return -2;
            }

            Object runtimeObject = ScriptEngineCompat.convertMapToRuntimeFormat(engine, sheetMap);
            sheetMap.clear();
            sheetMap = null;
            return runtimeObject;
        } finally {
            if(book != null) {
                closeBook(book);
            }
            System.gc();
        }
    }

    @Override
    protected boolean writeCell(int id, String sheetName, int rowId, int columnId, String value) {
        Workbook book = getBook(id);
        Sheet sheet = book.getSheet(sheetName);

        Row row = sheet.getRow(rowId);
        if (row == null) {
            row = sheet.createRow(rowId);
        }
        Cell cell = row.getCell(columnId);
        if (cell == null) {
            cell = row.createCell(columnId);
        }

        cell.setCellValue(value);
        return true;
    }

    @Override
    protected boolean saveBook(int id, String targetPath) throws Exception {
        Workbook book = getBook(id);
        OutputStream out = new FileOutputStream(new File(targetPath));
        book.write(out);
        out.close();
        return true;
    }

    @Override
    protected boolean closeBook(int id) {
        Workbook book = getBook(id);
        LOADED_BOOKS.remove(id);
        EXECUTOR.execute(() -> closeBook(book));
        return true;
    }

    @Override
    public void unload() {
        EXECUTOR.shutdown();

        LOADED_BOOKS.values().forEach(this::closeBook);
        LOADED_BOOKS.clear();
    }

    private Workbook getBook(int id) {
        assert LOADED_BOOKS.containsKey(id);
        return LOADED_BOOKS.get(id);
    }

    private void closeBook(Workbook book) {
        try {
            book.close();
        } catch (IOException ex) {
            print("Ошибка при закрытии книги: %s", ex.getMessage());
            handler.onProjectExecutionException(ex);
        }
    }

    // todo: узкое место
    private String readBigDecimal(double input) {
        //return Double.valueOf(input).toString();
        return new BigDecimal(input, new MathContext(10 , RoundingMode.CEILING)).toPlainString();
    }
}
