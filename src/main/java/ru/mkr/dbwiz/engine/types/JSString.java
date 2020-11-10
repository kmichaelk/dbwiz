package ru.mkr.dbwiz.engine.types;

public class JSString implements JSType {

    private final String value;

    public JSString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
}