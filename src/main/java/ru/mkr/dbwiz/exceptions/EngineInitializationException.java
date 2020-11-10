package ru.mkr.dbwiz.exceptions;

public class EngineInitializationException extends Exception {

    public EngineInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EngineInitializationException(String message) {
        super(message);
    }
}
