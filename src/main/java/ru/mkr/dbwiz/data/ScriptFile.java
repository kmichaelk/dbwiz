package ru.mkr.dbwiz.data;

public class ScriptFile extends DataFile {

    public ScriptFile(String fileName) {
        super(fileName);
    }

    @Override
    public String getFileExtension() {
        return "js";
    }
}
