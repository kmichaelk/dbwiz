package ru.mkr.dbwiz.modules;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.exceptions.ModuleInitializationException;
import ru.mkr.dbwiz.handlers.ProjectExecutionHandler;
import ru.mkr.dbwiz.utilities.ModuleUtility;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Powered by Apache POI
 *
 * Created on 13/10/20
 * (c) Mikhail K., 2020
 */
public class WordModule extends Module {

    private static final String JS_LIBRARY = ModuleUtility.loadBundledScript("/modules/word.js");

    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);
    private int DOCUMENT_COUNTER = 0;
    private final Map<Integer, XWPFDocument> LOADED_DOCUMENTS = new HashMap<>();

    public WordModule(ProjectExecutionHandler handler) throws ModuleInitializationException {
        super("Word", handler);
        if(JS_LIBRARY == null) {
            throw ModuleUtility.newScriptInitializationException();
        }
    }

    @Override
    public void inject(ScriptEngine engine) {
        engine.loadLibrary(JS_LIBRARY);
        engine.bindFunction("_wordReadDocument", (args -> {
            String path = ((String) args[0]);
            int id = DOCUMENT_COUNTER++;
            try {
                XWPFDocument document = new XWPFDocument(new FileInputStream(new File(path)));
                LOADED_DOCUMENTS.put(id, document);
                return id;
            } catch (Exception ex) {
                handler.onProjectExecutionException(ex);
                return -1; // код -1 - ошибка при чтении файла
            }
        }), 1);
        engine.bindFunction("_wordReplaceInDocument", (args -> {
            int id = (int)args[0];
            String toReplace = String.valueOf(args[1]);
            String replaceWith = String.valueOf(args[2]);
            assert LOADED_DOCUMENTS.containsKey(id);
            XWPFDocument document = LOADED_DOCUMENTS.get(id);
            //replaceInParagraphs(document.getParagraphs(), toReplace, replaceWith);
            //for (XWPFTable tables : document.getTables()) {
            //    for (XWPFTableRow row : tables.getRows()) {
            //        for (XWPFTableCell cell : row.getTableCells()) {
            //            replaceInParagraphs(cell.getParagraphs(), toReplace, replaceWith);
            //        }
            //    }
            //}
            for (IBodyElement element : document.getBodyElements()) {
                replaceInParagraphs(element.getBody().getParagraphs(), toReplace, replaceWith);
            }
            return null;
        }), 3);
        engine.bindFunction("_wordSaveDocument", (args -> {
            int id = (int)args[0];
            String targetPath = ((String) args[1]);
            XWPFDocument document = getDocument(id);
            try {
                OutputStream out = new FileOutputStream(new File(targetPath));
                document.write(out);
                out.close();
                return true;
            } catch (Exception ex) {
                print("Ошибка при сохранении документа: %s", ex.getMessage());
                handler.onProjectExecutionException(ex);
                return false;
            }
        }), 2);
        engine.bindFunction("_wordCloseDocument", (args -> {
            int id = (int) args[0];
            XWPFDocument document = getDocument(id);
            LOADED_DOCUMENTS.remove(id);
            EXECUTOR.execute(() -> closeDocument(document));
            if(DOCUMENT_COUNTER % 10 == 0) {
                System.gc();
            }
            return null;
        }), 1);
    }

    @Override
    public void unload() {
        EXECUTOR.shutdown();

        LOADED_DOCUMENTS.values().forEach(this::closeDocument);
        LOADED_DOCUMENTS.clear();
    }

    private static void replaceInParagraphs(List<XWPFParagraph> paragraphs, String toReplace, String replaceWith) {
        for (XWPFParagraph paragraph : paragraphs) {
            replaceInTextBoxes(paragraph, toReplace, replaceWith);
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if(text == null) continue;
                if(!text.contains(toReplace)) continue;

                text = text.replace(toReplace, replaceWith);

                if (text.contains("\n")) {
                    String[] lines = text.split("\n");
                    run.setText(lines[0], 0);
                    for (int i = 1; i < lines.length; i++) {
                        run.addBreak();
                        run.setText(lines[i]);
                    }
                } else {
                    run.setText(text, 0);
                }
            }
        }
    }

    // Замена в надписях (TextBox)
    // https://stackoverflow.com/questions/46802369/replace-text-in-text-box-of-docx-by-using-apache-poi
    private static void replaceInTextBoxes(XWPFParagraph paragraph, String toReplace, String replaceWith) {
        XmlCursor cursor = paragraph.getCTP().newCursor();
        cursor.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//*/w:txbxContent/w:p/w:r");

        List<XmlObject> ctrsInTextBox = new ArrayList<>();

        while (cursor.hasNextSelection()) {
            cursor.toNextSelection();
            XmlObject obj = cursor.getObject();
            ctrsInTextBox.add(obj);
        }
        for (XmlObject obj : ctrsInTextBox) {
            CTR ctr;
            try {
                ctr = CTR.Factory.parse(obj.xmlText());
            } catch (XmlException ex) {
                ex.printStackTrace();
                continue;
            }
            //CTR ctr = CTR.Factory.parse(obj.newInputStream());
            XWPFRun bufferRun = new XWPFRun(ctr, (IRunBody) paragraph);
            String text = bufferRun.getText(0);
            if (text != null && text.contains(toReplace)) {
                text = text.replace(toReplace, replaceWith);
                bufferRun.setText(text, 0);
                obj.set(bufferRun.getCTR());
            }
        }
    }

    private void closeDocument(XWPFDocument document) {
        try {
            document.close();
        } catch (IOException ex) {
            print("Ошибка при закрытии документа: %s", ex.getMessage());
            handler.onProjectExecutionException(ex);
        }
    }

    private XWPFDocument getDocument(int id) {
        assert LOADED_DOCUMENTS.containsKey(id);
        return LOADED_DOCUMENTS.get(id);
    }
}
