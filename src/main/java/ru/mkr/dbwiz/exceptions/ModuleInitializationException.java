package ru.mkr.dbwiz.exceptions;

public class ModuleInitializationException extends Exception {

    public ModuleInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleInitializationException(String message) {
        super(message);
    }
}
